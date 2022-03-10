package ch.geowerkstatt.ilivalidator.extensions.functions;

import ch.interlis.ili2c.metamodel.TextType;
import ch.interlis.iom.IomObject;
import ch.interlis.iox_j.validator.Value;

public class SubstringIoxPlugin extends BaseInterlisFunction {

    @Override
    public String getQualifiedIliName() {
        return "GeoW_FunctionsExt.Substring";
    }

    @Override
    protected Value evaluateInternal(String validationKind, String usageScope, IomObject mainObj, Value[] arguments) {
        Value textVal = arguments[0];
        Value startIndexVal = arguments[1];
        Value endIndexVal = arguments[2];

        if (textVal.isUndefined() || startIndexVal.isUndefined() || endIndexVal.isUndefined()) {
            return Value.createSkipEvaluation();
        }

        int startIndex = Integer.parseInt(startIndexVal.getValue());
        int endIndex = Integer.parseInt(endIndexVal.getValue());

        if (startIndex < 0 || endIndex > textVal.getValue().length() || endIndex < startIndex) {
            return  Value.createUndefined();
        }

        String substring = textVal.getValue().substring(startIndex, endIndex);
        return new Value(new TextType(), substring);
    }
}
