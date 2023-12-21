package ch.geowerkstatt.ilivalidator.extensions.functions;

import ch.ehi.basics.types.OutParam;
import ch.interlis.iom.IomObject;
import ch.interlis.iom_j.Iom_jObject;
import ch.interlis.iom_j.itf.impl.jtsext.geom.CompoundCurve;
import ch.interlis.iox.IoxException;
import ch.interlis.iox_j.jts.Iox2jtsext;
import ch.interlis.iox_j.validator.Value;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Polygon;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class BaseIsInsideFunction extends BaseInterlisFunction {
    private static final Map<ValidAreaKey, Geometry> VALID_AREA_CACHE = new HashMap<>();

    /**
     * Retrieve the geometries from referenced data according to the specified key.
     *
     * @param key The key that specifies which geometries to retrieve.
     * @return The union of all found geometries.
     */
    protected abstract Geometry getValidArea(ValidAreaKey key);

    protected final Value isInsideValidArea(String usageScope, ValidAreaKey key, Collection<IomObject> testObjects, String geometryAttribute) {
        try {
            Geometry validArea = VALID_AREA_CACHE.computeIfAbsent(key, this::getValidArea);

            if (validArea == null) {
                return Value.createUndefined();
            }

            boolean allInsideValidArea = testObjects.stream()
                    .flatMap(obj -> getAttributes(obj, geometryAttribute).stream())
                    .map(geometry -> logExceptionAsWarning(() -> geometry2JtsOrNull(geometry)))
                    .filter(Objects::nonNull)
                    .allMatch(validArea::contains);

            return new Value(allInsideValidArea);
        } catch (IllegalStateException e) {
            logger.addEvent(logger.logErrorMsg(MessageFormat.format("{0}: Unable to evaluate {1}. {2}", usageScope, this.getQualifiedIliName(), e.getLocalizedMessage())));
            return Value.createUndefined();
        }
    }

    protected final Polygon[] getPolygonsFromObjects(Stream<IomObject> objects, String attribute) {
        return objects
                .flatMap(obj -> getAttributes(obj, attribute).stream())
                .flatMap(attr -> extractChBaseSurfaceGeometryFromAttribute(attr).stream())
                .map(obj -> logExceptionAsWarning(() -> Iox2jtsext.surface2JTS(obj, 0.0, new OutParam<>(), logger, 0.0, "warning")))
                .filter(Objects::nonNull)
                .toArray(Polygon[]::new);
    }

    protected final <T> T logExceptionAsWarning(Callable<T> supplier) {
        try {
            return supplier.call();
        } catch (Exception e) {
            logger.addEvent(logger.logWarningMsg("{0}: {1}, {2}", this.getQualifiedIliName(), e.getClass().getSimpleName(), e.getLocalizedMessage()));
            return null;
        }
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
            return Iox2jtsext.polyline2JTS(geometry, false, 0.0, new OutParam<>(), logger, 0.0, "warning", "warning");
        } else if ("MULTISURFACE".equals(tag)) {
            if (geometry.getattrvaluecount("surface") > 1) {
                Polygon[] polygons = getAttributes(geometry, "surface").stream()
                        .map(s -> {
                            Iom_jObject singleSurface = new Iom_jObject("MULTISURFACE", null);
                            singleSurface.addattrobj("surface", s);
                            return singleSurface;
                        })
                        .map(s -> logExceptionAsWarning(() -> Iox2jtsext.surface2JTS(s, 0.0, new OutParam<>(), logger, 0.0, "warning")))
                        .filter(Objects::nonNull)
                        .toArray(Polygon[]::new);
                return new GeometryFactory().createMultiPolygon(polygons);
            } else {
                return Iox2jtsext.surface2JTS(geometry, 0.0, new OutParam<>(), logger, 0.0, "warning");
            }
        } else if ("MULTICOORD".equals(tag)) {
            Coordinate[] coords = getAttributes(geometry, "coord").stream()
                    .map(a -> logExceptionAsWarning(() -> Iox2jtsext.coord2JTS(a)))
                    .filter(Objects::nonNull)
                    .toArray(Coordinate[]::new);
            return new GeometryFactory().createMultiPoint(coords);
        } else if ("MULTIPOLYLINE".equals(tag)) {
            CompoundCurve[] curves = getAttributes(geometry, "polyline").stream()
                    .map(a -> logExceptionAsWarning(() -> Iox2jtsext.polyline2JTS(a, false, 0.0, new OutParam<>(), logger, 0.0, "warning", "warning")))
                    .filter(Objects::nonNull)
                    .toArray(CompoundCurve[]::new);
            return new GeometryFactory().createMultiLineString(curves);
        } else {
            logger.addEvent(logger.logWarningMsg("{0}: Unknown geometry tag {1}", this.getQualifiedIliName(), tag));
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

    /**
     * Extract surface geometries from a CHBase Geometry Attribute.
     * If the IomObject is not a CHBase surface Geometry, it is returned unchanged.
     */
    private Collection<IomObject> extractChBaseSurfaceGeometryFromAttribute(IomObject iomObject) {
        switch (iomObject.getobjecttag()) {
            case "GeometryCHLV95_V1.MultiSurface":
            case "GeometryCHLV03_V1.MultiSurface":
                return getAttributes(iomObject, "Surfaces").stream().flatMap(attr -> extractChBaseSurfaceGeometryFromAttribute(attr).stream()).collect(Collectors.toList());
            case "GeometryCHLV95_V1.SurfaceStructure":
            case "GeometryCHLV03_V1.SurfaceStructure":
                return getAttributes(iomObject, "Surface");
            default:
                return Collections.singleton(iomObject);
        }
    }

    protected static final class ValidAreaKey {
        private final String dataSource;
        private final String datasetName;
        private final String[] transferIds;

        ValidAreaKey(String dataSource, String datasetName, String transferIds) {
            this.dataSource = dataSource;
            this.datasetName = datasetName;

            String[] splitTransferIds = transferIds.split("\\s*,\\s*");
            Arrays.sort(splitTransferIds);
            this.transferIds = splitTransferIds;
        }

        public String getDataSource() {
            return dataSource;
        }

        public String getDatasetName() {
            return datasetName;
        }

        public String[] getTransferIds() {
            return transferIds;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            ValidAreaKey that = (ValidAreaKey) o;
            return Objects.equals(dataSource, that.dataSource) && Objects.equals(datasetName, that.datasetName) && Arrays.equals(transferIds, that.transferIds);
        }

        @Override
        public int hashCode() {
            int result = Objects.hash(dataSource, datasetName);
            result = 31 * result + Arrays.hashCode(transferIds);
            return result;
        }
    }
}
