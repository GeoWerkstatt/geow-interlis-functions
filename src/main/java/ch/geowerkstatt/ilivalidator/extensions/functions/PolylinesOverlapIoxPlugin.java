package ch.geowerkstatt.ilivalidator.extensions.functions;

import ch.interlis.ili2c.metamodel.PathEl;
import ch.interlis.ili2c.metamodel.Viewable;
import ch.interlis.iom.IomObject;
import ch.interlis.iom_j.itf.impl.jtsext.geom.CompoundCurve;
import ch.interlis.iox.IoxException;
import ch.interlis.iox_j.jts.Iox2jtsext;
import ch.interlis.iox_j.validator.Value;
import com.vividsolutions.jts.geom.IntersectionMatrix;
import com.vividsolutions.jts.index.strtree.STRtree;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

public final class PolylinesOverlapIoxPlugin extends BaseInterlisFunction {
    private static final Map<HasEqualLinePartKey, Boolean> HAS_EQUAL_LINE_PART_CACHE = new HashMap<>();

    @Override
    public String getQualifiedIliName() {
        return "GeoW_FunctionsExt.PolylinesOverlap";
    }

    @Override
    protected Value evaluateInternal(String validationKind, String usageScope, IomObject contextObject, Value[] arguments) {
        Value argObjects = arguments[0];
        Value argPath = arguments[1];

        if (argObjects.isUndefined()) {
            return Value.createSkipEvaluation();
        }

        if (argObjects.getComplexObjects() == null) {
            return Value.createUndefined();
        }

        Collection<IomObject> polylineObjects;

        if (argPath.isUndefined()) {
            polylineObjects = argObjects.getComplexObjects();
        } else {
            Viewable contextClass = EvaluationHelper.getContextClass(td, contextObject, argObjects);
            if (contextClass == null) {
                throw new IllegalStateException("unknown class in " + usageScope);
            }

            PathEl[] attributePath = EvaluationHelper.getAttributePathEl(validator, contextClass, argPath);
            polylineObjects = EvaluationHelper.evaluateAttributes(validator, argObjects, attributePath);
        }

        Collection<IomObject> inputObjects = argObjects.getComplexObjects();
        boolean hasObjectIds = inputObjects.stream().anyMatch(o -> o.getobjectoid() != null);
        if (!hasObjectIds) {
            List<CompoundCurve> lines = convertToJTSLines(polylineObjects);
            return new Value(hasEqualLinePart(lines));
        }

        List<String> objectIds = inputObjects.stream().map(IomObject::getobjectoid).collect(Collectors.toList());
        HasEqualLinePartKey key = new HasEqualLinePartKey(objectIds, argPath.isUndefined() ? null : argPath.getValue());

        boolean hasOverlap = HAS_EQUAL_LINE_PART_CACHE.computeIfAbsent(key, k -> {
            List<CompoundCurve> lines = convertToJTSLines(polylineObjects);
            return hasEqualLinePart(lines);
        });
        return new Value(hasOverlap);
    }

    private List<CompoundCurve> convertToJTSLines(Collection<IomObject> polylines) {
        return polylines.stream()
                .map(line -> {
                    try {
                        return Iox2jtsext.polyline2JTS(line, false, 0);
                    } catch (IoxException e) {
                        logger.addEvent(logger.logErrorMsg("Could not calculate {0}", getQualifiedIliName()));
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private static boolean hasEqualLinePart(List<CompoundCurve> lines) {
        if (lines.size() <= 1) {
            return false;
        }

        STRtree tree = new STRtree();
        for (CompoundCurve line : lines) {
            tree.insert(line.getEnvelopeInternal(), line);
        }

        AtomicBoolean hasOverlap = new AtomicBoolean(false);
        for (CompoundCurve line : lines) {
            if (hasOverlap.get()) {
                break;
            }
            tree.query(line.getEnvelopeInternal(), o -> {
                if (!hasOverlap.get() && o != line && linesHaveEqualPart(line, (CompoundCurve) o)) {
                    hasOverlap.set(true);
                }
            });
        }
        return hasOverlap.get();
    }

    private static boolean linesHaveEqualPart(CompoundCurve a, CompoundCurve b) {
        IntersectionMatrix relation = a.relate(b);

        // If the intersection of the interiors is a line, they have at least one part of a section in common
        int interiorIntersection = relation.get(0, 0);
        return interiorIntersection == 1;
    }

    private static final class HasEqualLinePartKey {
        private final List<String> objectIds;
        private final String attributeName;

        HasEqualLinePartKey(List<String> objectIds, String attributeName) {
            this.objectIds = objectIds;
            this.attributeName = attributeName;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            HasEqualLinePartKey that = (HasEqualLinePartKey) o;
            return Objects.equals(objectIds, that.objectIds) && Objects.equals(attributeName, that.attributeName);
        }

        @Override
        public int hashCode() {
            return Objects.hash(objectIds, attributeName);
        }
    }
}
