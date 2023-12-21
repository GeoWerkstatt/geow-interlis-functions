package ch.geowerkstatt.ilivalidator.extensions.functions;

import ch.interlis.iom.IomObject;
import ch.interlis.iom_j.xtf.XtfReader;
import ch.interlis.iom_j.xtf.XtfStartTransferEvent;
import ch.interlis.iox.IoxEvent;
import ch.interlis.iox.IoxException;
import ch.interlis.iox.IoxReader;
import ch.interlis.iox.ObjectEvent;

import java.io.InputStream;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public final class XtfHelper {
    private XtfHelper() {
        // Utility class
    }

    /**
     * Retrieve the models from the header section of an XTF.
     */
    public static Collection<String> getModelsInXTF(InputStream xtfInputStream) throws IoxException {
        IoxReader reader = null;
        try {
            reader = new XtfReader(xtfInputStream);
            IoxEvent event;

            while ((event = reader.read()) != null) {
                if (event instanceof XtfStartTransferEvent) {
                    XtfStartTransferEvent xtfStart = (XtfStartTransferEvent) event;
                    Map<String, IomObject> headerObjects = xtfStart.getHeaderObjects();
                    if (headerObjects != null) {
                        return headerObjects.values().stream()
                                .filter(obj -> "iom04.metamodel.ModelEntry".equals(obj.getobjecttag()))
                                .map(obj -> obj.getattrvalue("model"))
                                .distinct()
                                .collect(Collectors.toList());
                    }
                }
            }
        } finally {
            if (reader != null) {
                reader.close();
            }
        }

        return Collections.emptyList();
    }

    /**
     * Search an XTF file for objects with the specified tag and transfer IDs.
     */
    public static List<IomObject> getObjectsFromXTF(InputStream xtfInputStream, String objectTag, String[] transferIds) {
        Set<String> transferIdsToFind = new HashSet<>(Arrays.asList(transferIds));
        List<IomObject> foundObjects = new ArrayList<>();
        try {
            IoxReader reader = null;
            try {
                reader = new XtfReader(xtfInputStream);
                IoxEvent event;

                while ((event = reader.read()) != null && !transferIdsToFind.isEmpty()) {
                    if (event instanceof ObjectEvent) {
                        IomObject iomObject = ((ObjectEvent) event).getIomObject();
                        String transferId = iomObject.getobjectoid();
                        if (iomObject.getobjecttag().equals(objectTag) && transferIdsToFind.contains(transferId)) {
                            transferIdsToFind.remove(transferId);
                            foundObjects.add(iomObject);
                        }
                    }
                }
            } finally {
                if (reader != null) {
                    reader.close();
                }
            }

            if (!transferIdsToFind.isEmpty()) {
                throw new IllegalStateException(MessageFormat.format("Could not find objects with TID <{0}> in transfer file", String.join(", ", transferIdsToFind)));
            }
        } catch (IoxException e) {
            throw new IllegalStateException(MessageFormat.format("Could not read objects from XTF. {0}", e.getLocalizedMessage()), e);
        }

        return foundObjects;
    }

}
