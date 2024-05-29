package ch.geowerkstatt.ilivalidator.extensions.functions;

import ch.ehi.basics.types.OutParam;
import ch.interlis.iom.IomObject;
import ch.interlis.iox.IoxException;
import ch.interlis.iox_j.jts.Iox2jtsException;
import ch.interlis.iox_j.jts.Iox2jtsext;
import ch.interlis.iox_j.jts.Jts2iox;
import ch.interlis.iox_j.jts.Jtsext2iox;
import ch.interlis.iox_j.validator.Value;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Polygon;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public final class UnionIoxPlugin extends BaseInterlisFunction {
    private static final Map<UnionSurfaceKey, IomObject> UNION_SURFACE_CACHE = new HashMap<>();

    @Override
    public String getQualifiedIliName() {
        return "GeoW_FunctionsExt.Union";
    }

    @Override
    protected Value evaluateInternal(String validationKind, String usageScope, IomObject contextObject, Value[] actualArguments) {
        Value argObjects = actualArguments[0];
        Value argPath = actualArguments[1];

        if (argObjects.isUndefined()) {
            return Value.createSkipEvaluation();
        }

        Collection<IomObject> surfaces = EvaluationHelper.evaluateObjectPath(td, validator, argObjects, argPath, contextObject, usageScope);
        if (surfaces == null) {
            return Value.createUndefined();
        }

        UnionSurfaceKey key = new UnionSurfaceKey(surfaces);
        IomObject unionSurface = UNION_SURFACE_CACHE.computeIfAbsent(key, this::union);
        if (unionSurface == null) {
            return Value.createUndefined();
        }
        return new Value(Collections.singletonList(unionSurface));
    }

    private IomObject union(UnionSurfaceKey key) {
        Collection<IomObject> surfaces = key.surfaces;

        MultiPolygon[] polygons = surfaces.stream()
                .map(surface -> {
                    try {
                        return Iox2jtsext.multisurface2JTS(surface, 0, new OutParam<>(), logger, 0, "warning");
                    } catch (IoxException e) {
                        logger.addEvent(logger.logErrorMsg("Could not convert surface to JTS"));
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .toArray(MultiPolygon[]::new);

        Geometry geometryCollection = new GeometryFactory().createGeometryCollection(polygons);
        Geometry unionGeometry = geometryCollection.buffer(0);

        try {
            if (unionGeometry instanceof Polygon) {
                return Jtsext2iox.JTS2surface((Polygon) unionGeometry);
            } else if (unionGeometry instanceof MultiPolygon) {
                return Jts2iox.JTS2multisurface((MultiPolygon) unionGeometry);
            } else {
                logger.addEvent(logger.logErrorMsg("Expected {0} or {1} but was {2}", Polygon.class.toString(), MultiPolygon.class.toString(), unionGeometry.getClass().toString()));
                return null;
            }
        } catch (Iox2jtsException e) {
            logger.addEvent(logger.logErrorMsg("Could not calculate {0}", this.getQualifiedIliName()));
            return null;
        }
    }

    private static final class UnionSurfaceKey {
        private final Collection<IomObject> surfaces;

        private UnionSurfaceKey(Collection<IomObject> surfaces) {
            this.surfaces = surfaces;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof UnionSurfaceKey)) {
                return false;
            }
            UnionSurfaceKey that = (UnionSurfaceKey) o;
            return Objects.equals(surfaces, that.surfaces);
        }

        @Override
        public int hashCode() {
            return Objects.hash(surfaces);
        }
    }
}
