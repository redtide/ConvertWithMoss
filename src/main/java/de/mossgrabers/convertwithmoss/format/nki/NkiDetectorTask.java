// Written by Jürgen Moßgraber - mossgrabers.de
// (c) 2019-2023
// Licensed under LGPLv3 - http://www.gnu.org/licenses/lgpl-3.0.txt

package de.mossgrabers.convertwithmoss.format.nki;

import de.mossgrabers.convertwithmoss.core.IMultisampleSource;
import de.mossgrabers.convertwithmoss.core.INotifier;
import de.mossgrabers.convertwithmoss.core.detector.AbstractDetectorTask;
import de.mossgrabers.convertwithmoss.file.StreamUtils;
import de.mossgrabers.convertwithmoss.format.nki.type.IKontaktType;
import de.mossgrabers.convertwithmoss.format.nki.type.Kontakt1Type;
import de.mossgrabers.convertwithmoss.format.nki.type.Kontakt2Type;
import de.mossgrabers.convertwithmoss.ui.IMetadataConfig;
import de.mossgrabers.tools.ui.Functions;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;


/**
 * Detector for Native Instruments Kontakt Instrument (NKI) files. Currently, only the format of the
 * versions before Kontakt 4.2.2 are supported.
 *
 * @author J&uuml;rgen Mo&szlig;graber
 * @author Philip Stolz
 */
public class NkiDetectorTask extends AbstractDetectorTask
{
    private static final Integer             ID_KONTAKT1               = Integer.valueOf (0x5EE56EB3);
    private static final Integer             ID_KONTAKT2_LITTLE_ENDIAN = Integer.valueOf (0x1290A87F);
    private static final Integer             ID_KONTAKT2_BIG_ENDIAN    = Integer.valueOf (0x7FA89012);

    private static final Integer             ID_KONTAKT5_MONOLITH      = Integer.valueOf (0x2F5C204E);

    private final Map<Integer, IKontaktType> kontaktTypes              = new HashMap<> (2);


    /**
     * Constructor.
     *
     * @param notifier The notifier
     * @param consumer The consumer that handles the detected multisample sources
     * @param sourceFolder The top source folder for the detection
     * @param metadata Additional metadata configuration parameters
     */
    public NkiDetectorTask (final INotifier notifier, final Consumer<IMultisampleSource> consumer, final File sourceFolder, final IMetadataConfig metadata)
    {
        super (notifier, consumer, sourceFolder, metadata, ".nki");

        this.kontaktTypes.put (ID_KONTAKT1, new Kontakt1Type (metadata, notifier));
        this.kontaktTypes.put (ID_KONTAKT2_LITTLE_ENDIAN, new Kontakt2Type (metadata, notifier, false));
        this.kontaktTypes.put (ID_KONTAKT2_BIG_ENDIAN, new Kontakt2Type (metadata, notifier, true));
    }


    /** {@inheritDoc} */
    @Override
    protected List<IMultisampleSource> readFile (final File sourceFile)
    {
        if (this.waitForDelivery ())
            return Collections.emptyList ();

        try (final RandomAccessFile fileAccess = new RandomAccessFile (sourceFile, "r"))
        {
            fileAccess.seek (12);
            if ("hsin".equals (StreamUtils.readASCII (fileAccess, 4)))
            {
                this.notifier.logError ("IDS_NKI_KONTAKT5_NOT_SUPPORTED");
                return Collections.emptyList ();
            }

            fileAccess.seek (0);

            final int typeID = fileAccess.readInt ();
            if (ID_KONTAKT5_MONOLITH.intValue () == typeID)
            {
                this.notifier.logError ("IDS_NKI_KONTAKT5_MONOLITH_NOT_SUPPORTED");
                return Collections.emptyList ();
            }

            final IKontaktType kontaktType = this.kontaktTypes.get (Integer.valueOf (typeID));
            if (kontaktType == null)
                throw new IOException (Functions.getMessage ("IDS_NKI_UNKNOWN_FILE_ID", Integer.toHexString (typeID).toUpperCase ()));
            final List<IMultisampleSource> result = kontaktType.parse (this.sourceFolder, sourceFile, fileAccess);
            if (result.isEmpty ())
                this.notifier.logError ("IDS_NKI_COULD_NOT_DETECT_LAYERS");
            return result;
        }
        catch (final IOException ex)
        {
            this.notifier.logError ("IDS_NKI_UNSUPPORTED_FILE_FORMAT", ex);
        }
        return Collections.emptyList ();
    }
}
