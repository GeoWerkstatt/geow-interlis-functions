package ch.geowerkstatt.ilivalidator.extensions.functions;

import ch.ehi.basics.settings.Settings;
import ch.ehi.basics.types.OutParam;
import ch.interlis.ili2c.metamodel.TransferDescription;
import ch.interlis.iom.IomObject;
import ch.interlis.iom_j.Iom_jObject;
import ch.interlis.iom_j.itf.impl.jtsext.geom.CompoundCurve;
import ch.interlis.iox.*;
import ch.interlis.iox_j.jts.Iox2jtsext;
import ch.interlis.iox_j.logging.LogEventFactory;
import ch.interlis.iox_j.utility.IoxUtility;
import ch.interlis.iox_j.utility.ReaderFactory;
import ch.interlis.iox_j.validator.ObjectPool;
import ch.interlis.iox_j.validator.Value;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Polygon;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.MessageFormat;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

public class IsInsideExternalDatasetIoxPlugin extends BaseInterlisFunction {
    private static File dataDirectory;
    private static Map<String, Set<File>> modelFiles;
    private static final Map<ValidAreaKey, Geometry> validAreaCache = new HashMap<>();
    private static final String qualifiedIliName = "GeoW_FunctionsExt.IsInsideExternalDataset";

    @Override
    public String getQualifiedIliName() {
        return qualifiedIliName;
    }

    @Override
    public void init(TransferDescription td, Settings settings, IoxValidationConfig validationConfig, ObjectPool objectPool, LogEventFactory logEventFactory) {
        super.init(td, settings, validationConfig, objectPool, logEventFactory);

        if (dataDirectory == null) {
            dataDirectory = getDataDirectory();
        }
        if (dataDirectory != null && modelFiles == null) {
            modelFiles = analyzeDataDirectory(logger, settings);
        }
    }

    @Override
    protected Value evaluateInternal(String validationKind, String usageScope, IomObject mainObj, Value[] arguments) {
        Value argDatasetName = arguments[0]; // TEXT
        Value argObjects = arguments[1]; // TEXT
        Value argTestObject = arguments[2]; // OBJECT OF ANYCLASS
        Value argTestObjectgeometry = arguments[3]; // TEXT

        if (argDatasetName.isUndefined() || argObjects.isUndefined() || argTestObject.isUndefined() || argTestObjectgeometry.isUndefined()) {
            return Value.createSkipEvaluation();
        }

        try {
            String transferIds = argObjects.getValue();
            String[] splitTransferIds = transferIds.split("\\s*,\\s*");
            String datasetName = argDatasetName.getValue();

            ValidAreaKey key = new ValidAreaKey(datasetName, splitTransferIds);
            Geometry validArea = validAreaCache.computeIfAbsent(key, this::getValidArea);

            if (validArea == null) {
                return Value.createUndefined();
            }

            String testObjectgeometryAttribute = argTestObjectgeometry.getValue();

            boolean allInsideValidArea = argTestObject.getComplexObjects().stream()
                    .flatMap(obj -> getAttributes(obj, testObjectgeometryAttribute).stream())
                    .map(a -> logExceptionAsWarning(() -> geometry2JtsOrNull(a)))
                    .filter(Objects::nonNull)
                    .allMatch(validArea::contains);

            // getClass().getProtectionDomain().getCodeSource().getLocation().toURI();
            //      -> SecurityException
            
            // getClass().getResource(getClass().getSimpleName() + ".class")
            
            // ClassLoader.getSystemClassLoader().getResource(".")
            
            // logger.addEvent(logger.logErrorMsg("Could not calculate {0}", this.getQualifiedIliName()));
            //            return null;

            return new Value(allInsideValidArea);
        } catch (IllegalStateException e) {
            logger.addEvent(logger.logErrorMsg(MessageFormat.format("{0}: Unable to evaluate {1}. {2}", usageScope, this.getQualifiedIliName(), e.getLocalizedMessage())));
            return Value.createUndefined();
        }
    }

    /**
     * Retrieve the geometries from external files according to the specified key.
     *
     * @param key The key that specifies which geometries to retrieve.
     * @return The union of all found geometries.
     */
    private Geometry getValidArea(ValidAreaKey key) {
        int firstPoint = key.datasetName.indexOf('.');
        int lastPoint = key.datasetName.lastIndexOf('.');
        if (firstPoint == -1 || lastPoint == -1) {
            throw new IllegalStateException(MessageFormat.format("Expected qualified attribute name but got <{0}>.", key.datasetName));
        }

        String model = key.datasetName.substring(0, firstPoint);
        String attribute = key.datasetName.substring(lastPoint + 1);
        String qualifiedClass = key.datasetName.substring(0, lastPoint);

        Set<File> files = modelFiles.get(model);
        if (files == null) {
            throw new IllegalStateException(MessageFormat.format("Could not find Transferfile containing model <{0}> in path {1}.", model, dataDirectory.getAbsolutePath()));
        }

        Polygon[] polygons = files.stream()
                .flatMap(file -> getObjectsFromFile(file, qualifiedClass, key.transferIds).stream())
                .flatMap(obj -> getAttributes(obj, attribute).stream())
                .map(a -> logExceptionAsWarning(() -> Iox2jtsext.surface2JTS(a, 0.0, new OutParam<Boolean>(), logger, 0.0, "warning")))
                .filter(Objects::nonNull)
                .toArray(Polygon[]::new);

        Geometry geometryCollection = new GeometryFactory().createGeometryCollection(polygons);
        return geometryCollection.buffer(0);
    }

    /**
     * Get the directory where external datasets are located.
     *
     * @return A {@link File} representing the directory or {@code null}.
     */
    private static File getDataDirectory() {
        Class<IsInsideExternalDatasetIoxPlugin> clazz = IsInsideExternalDatasetIoxPlugin.class;
        URL resource = clazz.getResource(clazz.getSimpleName() + ".class");
        if (resource == null) {
            return null;
        }
        String url = resource.toString();
        if (url.startsWith("jar:file:")) {
            String path = url.replaceAll("^jar:(file:.*\\.jar)!/.*", "$1");
            return new File(path).getParentFile();
        }

        return null;
    }

    /**
     * Analyze the XTF files in {@link this.dataDirectory}.
     *
     * @return A mapping of Interlis model names to XTF files that contain the model.
     */
    private static Map<String, Set<File>> analyzeDataDirectory(LogEventFactory logger, Settings settings) {
        final Map<String, Set<File>> result = new HashMap<>();

        try {
            List<File> xtfFiles = Files.walk(dataDirectory.toPath())
                    .filter(Files::isRegularFile)
                    .filter(path -> IoxUtility.isXtfFilename(path.toString()))
                    .map(Path::toFile)
                    .collect(Collectors.toList());

            for (File xtfFile : xtfFiles) {
                try {
                    List<String> models = IoxUtility.getModels(xtfFile, logger, settings);
                    for (String model : models) {
                        if (!result.containsKey(model)) {
                            result.put(model, new HashSet<>());
                        }
                        Set<File> files = result.get(model);
                        files.add(xtfFile);
                    }
                } catch (IoxException e) {
                    // ignore the erroneous file
                    logger.addEvent(logger.logWarningMsg("{0}: Error while reading xtf file {1}. {2}", qualifiedIliName, xtfFile.getAbsolutePath(), e.getLocalizedMessage()));
                }
            }
        } catch (IOException e) {
            logger.addEvent(logger.logWarningMsg("{0}: Could not read data directory {1}. {2}", qualifiedIliName, dataDirectory.getAbsolutePath(), e.getLocalizedMessage()));
        }

        return result;
    }

    /**
     * Search an XTF file for objects with the specified tag and transfer IDs.
     */
    private List<IomObject> getObjectsFromFile(File xtfFile, String objectTag, String[] transferIds) {
        Set<String> transferIdsToFind = new HashSet<>(Arrays.asList(transferIds));
        List<IomObject> foundObjects = new ArrayList<>();
        IoxReader reader = null;
        try {
            try {
                reader = new ReaderFactory().createReader(xtfFile, logger, settings);
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
                throw new IllegalStateException(MessageFormat.format("Could not find objects with TID <{0}> in transfer file {1}", String.join(", ", transferIdsToFind), xtfFile.getAbsolutePath()));
            }
        } catch (IoxException e) {
            throw new IllegalStateException(MessageFormat.format("Could not read {0}. {1}", xtfFile.getAbsolutePath(), e.getLocalizedMessage()), e);
        }

        return foundObjects;
    }

    /**
     * Convert the geometry {@link IomObject} to a JTS {@link Geometry}.
     *
     * @return The converted geometry or {@code null}.
     */
    private Geometry geometry2JtsOrNull(IomObject geometry) throws IoxException {
        String tag = geometry.getobjecttag();
        if ("COORD".equals(tag)) {
            Coordinate coord = Iox2jtsext.coord2JTS(geometry);
            return new GeometryFactory().createPoint(coord);
        } else if ("POLYLINE".equals(tag)) {
            return Iox2jtsext.polyline2JTS(geometry, false, 0.0, new OutParam<Boolean>(), logger, 0.0, "warning", "warning");
        } else if ("MULTISURFACE".equals(tag)) {
            if (geometry.getattrvaluecount("surface") > 1) {
                Polygon[] polygons = getAttributes(geometry, "surface").stream()
                        .map(s -> {
                            Iom_jObject singleSurface = new Iom_jObject("MULTISURFACE", null);
                            singleSurface.addattrobj("surface", s);
                            return singleSurface;
                        })
                        .map(s -> logExceptionAsWarning(() -> Iox2jtsext.surface2JTS(s, 0.0, new OutParam<Boolean>(), logger, 0.0, "warning")))
                        .filter(Objects::nonNull)
                        .toArray(Polygon[]::new);
                return new GeometryFactory().createMultiPolygon(polygons);
            } else {
                return Iox2jtsext.surface2JTS(geometry, 0.0, new OutParam<Boolean>(), logger, 0.0, "warning");
            }
        } else if ("MULTICOORD".equals(tag)) {
            Coordinate[] coords = getAttributes(geometry, "coord").stream()
                    .map(a -> logExceptionAsWarning(() -> Iox2jtsext.coord2JTS(a)))
                    .filter(Objects::nonNull)
                    .toArray(Coordinate[]::new);
            return new GeometryFactory().createMultiPoint(coords);
        } else if ("MULTIPOLYLINE".equals(tag)) {
            CompoundCurve[] curves = getAttributes(geometry, "polyline").stream()
                    .map(a -> logExceptionAsWarning(() -> Iox2jtsext.polyline2JTS(a, false, 0.0, new OutParam<Boolean>(), logger, 0.0, "warning", "warning")))
                    .filter(Objects::nonNull)
                    .toArray(CompoundCurve[]::new);
            return new GeometryFactory().createMultiLineString(curves);
        } else {
            logger.addEvent(logger.logWarningMsg("{0}: Unknown geometry tag {1}", this.getQualifiedIliName(), tag));
            return null;
        }
    }

    private <T> T logExceptionAsWarning(Callable<T> supplier) {
        try {
            return supplier.call();
        } catch (Exception e) {
            logger.addEvent(logger.logWarningMsg("{0}: {1}, {2}", this.getQualifiedIliName(), e.getClass().getSimpleName(), e.getLocalizedMessage()));
            return null;
        }
    }

    /**
     * Retrieve all the attributes with the specified name from an iomObject.
     */
    private Collection<IomObject> getAttributes(IomObject iomObject, String attributeName) {
        int attributeCount = iomObject.getattrvaluecount(attributeName);
        List<IomObject> foundAttributes = new ArrayList<>(attributeCount);
        for (int i = 0; i < attributeCount; i++) {
            foundAttributes.add(iomObject.getattrobj(attributeName, i));
        }
        return foundAttributes;
    }

    private static class ValidAreaKey {
        private final String datasetName;
        private final String[] transferIds;

        ValidAreaKey(String datasetName, String[] transferIds) {
            this.datasetName = datasetName;
            this.transferIds = transferIds;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ValidAreaKey that = (ValidAreaKey) o;
            return Objects.equals(datasetName, that.datasetName) && Arrays.equals(transferIds, that.transferIds);
        }

        @Override
        public int hashCode() {
            int result = Objects.hash(datasetName);
            result = 31 * result + Arrays.hashCode(transferIds);
            return result;
        }
    }
}
