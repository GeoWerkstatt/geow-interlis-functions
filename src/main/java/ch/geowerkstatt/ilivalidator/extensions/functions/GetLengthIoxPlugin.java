package ch.geowerkstatt.ilivalidator.extensions.functions;

import ch.interlis.ili2c.metamodel.PathEl;
import ch.interlis.ili2c.metamodel.Viewable;
import ch.interlis.iom.IomObject;
import ch.interlis.iox.IoxException;
import ch.interlis.iox_j.jts.Iox2jtsext;
import ch.interlis.iox_j.validator.Value;

import java.util.Collection;

public final class GetLengthIoxPlugin extends BaseInterlisFunction {

    @Override
    public String getQualifiedIliName() {
        return "GeoW_FunctionsExt.GetLength";
    }

    private double getLength(IomObject polyline) {
        try {
            return Iox2jtsext.polyline2JTS(polyline, false, 0.0).getLength();
        } catch (IoxException ex) {
            logger.addEvent(logger.logErrorMsg("Could not calculate {0}", this.getQualifiedIliName()));
        }
        return 0.0;
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

        Collection<IomObject> polylines;

        if (argPath.isUndefined()) {
            polylines = argObjects.getComplexObjects();
        } else {
            Viewable contextClass = EvaluationHelper.getContextClass(td, contextObject, argObjects);

            if (contextClass == null) {
                throw new IllegalStateException("unknown class in " + usageScope);
            }

            PathEl[] attributePath = EvaluationHelper.getAttributePathEl(validator, contextClass, argPath);
            polylines = EvaluationHelper.evaluateAttributes(validator, argObjects, attributePath);
        }

        double lengthSum = EvaluationHelper.sum(polylines, this::getLength);
        return new Value(lengthSum);
    }
}
