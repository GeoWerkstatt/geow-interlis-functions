package ch.geowerkstatt.ilivalidator.extensions.functions;

import ch.interlis.ili2c.metamodel.PathEl;
import ch.interlis.ili2c.metamodel.Viewable;
import ch.interlis.iom.IomObject;
import ch.interlis.iom_j.itf.impl.jtsext.geom.CompoundCurve;
import ch.interlis.iox.IoxException;
import ch.interlis.iox_j.jts.Iox2jtsext;
import ch.interlis.iox_j.validator.Value;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public final class PolylinesOverlapIoxPlugin extends BaseInterlisFunction {
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

        List<CompoundCurve> lines = convertToJTSLines(polylineObjects);
        boolean hasOverlap = hasLineOverlap(lines);
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

    private static boolean hasLineOverlap(List<CompoundCurve> lines) {
        for (int i = 0; i < lines.size(); i++) {
            for (int j = i + 1; j < lines.size(); j++) {
                if (lines.get(i).overlaps(lines.get(j))) {
                    return true;
                }
            }
        }

        return false;
    }
}
