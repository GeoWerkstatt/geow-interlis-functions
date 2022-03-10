package ch.geowerkstatt.ilivalidator.extensions.functions;

import ch.interlis.iom.IomObject;
import ch.interlis.iox_j.validator.Value;

public class IsMatchIoxPlugin extends BaseInterlisFunction {
    @Override
    public String getQualifiedIliName() {
        return "GeoW_FunctionsExt.IsMatch";
    }

    @Override
    protected Value evaluateInternal(String validationKind, String usageScope, IomObject mainObj, Value[] actualArguments) {
        return Value.createNotYetImplemented();
    }
}
