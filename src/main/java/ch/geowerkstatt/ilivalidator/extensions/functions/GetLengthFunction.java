package ch.geowerkstatt.ilivalidator.extensions.functions;

import ch.ehi.basics.settings.Settings;
import ch.interlis.ili2c.metamodel.TransferDescription;
import ch.interlis.iom.IomObject;
import ch.interlis.iox.IoxValidationConfig;
import ch.interlis.iox_j.logging.LogEventFactory;
import ch.interlis.iox_j.validator.InterlisFunction;
import ch.interlis.iox_j.validator.ObjectPool;
import ch.interlis.iox_j.validator.Value;

public class GetLengthFunction implements InterlisFunction {
    @Override
    public void init(TransferDescription transferDescription,
                     Settings settings,
                     IoxValidationConfig ioxValidationConfig,
                     ObjectPool objectPool,
                     LogEventFactory logEventFactory) {

    }

    @Override
    public Value evaluate(String s, String s1, IomObject iomObject, Value[] values) {
        return null;
    }

    @Override
    public String getQualifiedIliName() {
        return "GeoW_FunctionsExt.GetLength";
    }
}
