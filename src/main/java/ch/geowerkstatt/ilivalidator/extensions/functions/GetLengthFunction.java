package ch.geowerkstatt.ilivalidator.extensions.functions;

import ch.ehi.basics.settings.Settings;
import ch.interlis.ili2c.metamodel.TransferDescription;
import ch.interlis.iom.IomObject;
import ch.interlis.iox.IoxException;
import ch.interlis.iox.IoxLogEvent;
import ch.interlis.iox.IoxValidationConfig;
import ch.interlis.iox_j.jts.Iox2jtsext;
import ch.interlis.iox_j.logging.LogEventFactory;
import ch.interlis.iox_j.validator.InterlisFunction;
import ch.interlis.iox_j.validator.ObjectPool;
import ch.interlis.iox_j.validator.Value;

import java.util.HashMap;

public class GetLengthFunction implements InterlisFunction {

    private LogEventFactory logger;
    private HashMap tag2class;
    private TransferDescription td;
    private Validator validator;

    @Override
    public void init(TransferDescription td, Settings settings, IoxValidationConfig validationConfig, ObjectPool objectPool, LogEventFactory logEventFactory) {
        this.logger = logEventFactory;
        this.logger.setValidationConfig(validationConfig);
        this.tag2class = ch.interlis.iom_j.itf.ModelUtilities.getTagMap(td);
        this.td = td;
        this.validator = (Validator) settings.getTransientObject(IOX_VALIDATOR);
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

        Value argPath = arguments[1];
        if (argPath.skipEvaluation()){
            return argPath;
        }

        //TODO Parse attribute values

        double result = 0.0d;

        for (IomObject rootObject: argObjects.getComplexObjects()) {
            try {
                IomObject polylineAttribute = rootObject.getattrobj(argPath.getValue(),0);
                result += Iox2jtsext.polyline2JTS(polylineAttribute, false, 0.00001).getLength();
            }catch (IoxException ex){
                logger.addEvent(logger.logErrorMsg("Could not calculate GetLength for Object with OID {0}", rootObject.getobjectoid()));
            }
        }

        return new Value(result);
    }

    @Override
    public String getQualifiedIliName() {
        return "GeoW_FunctionsExt.GetLength";
    }
}
