package ch.geowerkstatt.ilivalidator.extensions.functions;

import ch.interlis.ili2c.metamodel.PathEl;
import ch.interlis.ili2c.metamodel.Viewable;
import ch.interlis.iom.IomObject;
import ch.interlis.iox_j.validator.Value;

import java.util.Collection;

public final class GetInnerRingsCountIoxPlugin extends BaseInterlisFunction {

    @Override
    public String getQualifiedIliName() {
        return "GeoW_FunctionsExt.GetInnerRingsCount";
    }

    private Integer getInnerRingsCount(IomObject multisurface) {
        int innerRings = 0;

        for (int si = 0; si < multisurface.getattrvaluecount("surface"); si++) {
            IomObject surface = multisurface.getattrobj("surface", si);
            innerRings += (surface.getattrvaluecount("boundary") - 1);
        }

        return innerRings;
    }

    @Override
    public Value evaluateInternal(String validationKind, String usageScope, IomObject contextObject, Value[] arguments) {
        Value argObjects = arguments[0];
        Value argPath = arguments[1];

        if (argObjects.isUndefined()) {
            return Value.createSkipEvaluation();
        }

        if (argObjects.getComplexObjects() == null) {
            return Value.createUndefined();
        }

        Collection<IomObject> surfaces;

        if (argPath.isUndefined()) {
            surfaces = argObjects.getComplexObjects();
        } else {
            Viewable contextClass = EvaluationHelper.getContextClass(td, contextObject, argObjects);

            if (contextClass == null) {
                throw new IllegalStateException("unknown class in " + usageScope);
            }

            PathEl[] attributePath = EvaluationHelper.getAttributePathEl(validator, contextClass, argPath);
            surfaces = EvaluationHelper.evaluateAttributes(validator, argObjects, attributePath);
        }

        int innerRingsCount = surfaces.stream().map(this::getInnerRingsCount).reduce(0, Integer::sum);
        return new Value(innerRingsCount);
    }
}
