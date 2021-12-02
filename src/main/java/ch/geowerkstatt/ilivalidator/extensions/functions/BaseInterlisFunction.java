package ch.geowerkstatt.ilivalidator.extensions.functions;

import ch.ehi.basics.settings.Settings;
import ch.interlis.ili2c.metamodel.TransferDescription;
import ch.interlis.iom.IomObject;
import ch.interlis.iox.IoxValidationConfig;
import ch.interlis.iox_j.logging.LogEventFactory;
import ch.interlis.iox_j.validator.InterlisFunction;
import ch.interlis.iox_j.validator.ObjectPool;
import ch.interlis.iox_j.validator.Validator;
import ch.interlis.iox_j.validator.Value;

public abstract class BaseInterlisFunction implements InterlisFunction {

    protected LogEventFactory logger;
    protected TransferDescription td;
    protected Validator validator;

    @Override
    public void init(TransferDescription td, Settings settings, IoxValidationConfig validationConfig, ObjectPool objectPool, LogEventFactory logEventFactory) {
        this.logger = logEventFactory;
        this.logger.setValidationConfig(validationConfig);
        this.td = td;
        this.validator = (Validator) settings.getTransientObject(IOX_VALIDATOR);
    }

    @Override
    public Value evaluate(String validationKind, String usageScope, IomObject mainObj, Value[] actualArguments) {

        for (Value arg : actualArguments) {
            if (arg.skipEvaluation()){
                return arg;
            }
        }

        logger.setDataObj(mainObj);

        return evaluateInternal(validationKind, usageScope, mainObj, actualArguments);
    }

    protected abstract Value evaluateInternal(String validationKind, String usageScope, IomObject mainObj, Value[] actualArguments);
}
