package ch.geowerkstatt.ilivalidator.extensions.functions;

import ch.ehi.basics.settings.Settings;
import ch.interlis.ili2c.metamodel.TransferDescription;
import ch.interlis.iom.IomObject;
import ch.interlis.iox.IoxValidationConfig;
import ch.interlis.iox_j.logging.LogEventFactory;
import ch.interlis.iox_j.validator.InterlisFunction;
import ch.interlis.iox_j.validator.ObjectPool;
import ch.interlis.iox_j.validator.Value;

import java.util.HashMap;

public class GetLengthFunction implements InterlisFunction {

    private LogEventFactory logger = null;
    private HashMap tag2class = null;
    private TransferDescription td = null;

    @Override
    public void init(TransferDescription td, Settings settings, IoxValidationConfig validationConfig, ObjectPool objectPool, LogEventFactory logEventFactory) {
        this.logger = logEventFactory;
        this.logger.setValidationConfig(validationConfig);
        this.tag2class = ch.interlis.iom_j.itf.ModelUtilities.getTagMap(td);
        this.td = td;
    }

    @Override
    public Value evaluate(String s, String s1, IomObject iomObject, Value[] arguments) {
        Value argObjects=arguments[0];
        if (argObjects.skipEvaluation()){
            return argObjects;
        }
        if (argObjects.isUndefined()){
            return Value.createSkipEvaluation();
        }

        return new Value(-1.0);
    }

    @Override
    public String getQualifiedIliName() {
        return "GeoW_FunctionsExt.GetLength";
    }
}
