package ch.geowerkstatt.ilivalidator.extensions.functions;

import ch.interlis.iom.IomObject;
import ch.interlis.iox.IoxException;
import ch.interlis.iox_j.jts.Iox2jtsext;
import ch.interlis.iox_j.validator.Value;

import java.util.Collection;

public final class GetAreaIoxPlugin extends BaseInterlisFunction {

    @Override
    public String getQualifiedIliName() {
        return "GeoW_FunctionsExt.GetArea";
    }

    private double getArea(IomObject surface) {
        try {
            return Iox2jtsext.surface2JTS(surface, 0.0).getArea();
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

        Collection<IomObject> surfaces = EvaluationHelper.evaluateObjectPath(td, validator, argObjects, argPath, contextObject, usageScope);

        double areaSum = EvaluationHelper.sum(surfaces, this::getArea);
        return new Value(areaSum);
    }
}
