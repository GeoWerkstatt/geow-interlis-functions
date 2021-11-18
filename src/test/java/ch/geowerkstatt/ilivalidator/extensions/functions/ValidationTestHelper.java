package ch.geowerkstatt.ilivalidator.extensions.functions;

import ch.ehi.basics.settings.Settings;
import ch.interlis.ili2c.Ili2cFailure;
import ch.interlis.ili2c.metamodel.TransferDescription;
import ch.interlis.iox.EndTransferEvent;
import ch.interlis.iox.IoxEvent;
import ch.interlis.iox.IoxException;
import ch.interlis.iox.IoxLogging;
import ch.interlis.iox.IoxReader;
import ch.interlis.iox_j.IoxIliReader;
import ch.interlis.iox_j.PipelinePool;
import ch.interlis.iox_j.logging.LogEventFactory;
import ch.interlis.iox_j.utility.ReaderFactory;
import ch.interlis.iox_j.validator.InterlisFunction;
import ch.interlis.iox_j.validator.ValidationConfig;
import ch.interlis.iox_j.validator.Validator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;

public class ValidationTestHelper {

    Map<String,Class> userFunctions = new java.util.HashMap();

    public void runValidation(String[] dataFiles, String[] modelDirs) throws IoxException, Ili2cFailure {
        IoxLogging errHandler = new ch.interlis.iox_j.logging.Log2EhiLogger();
        LogEventFactory errFactory = new LogEventFactory();
        errFactory.setLogger(errHandler);

        Settings settings = new Settings();
        settings.setTransientObject(ch.interlis.iox_j.validator.Validator.CONFIG_CUSTOM_FUNCTIONS, userFunctions);

        modelDirs = appendGeoWFunctionsExtIli(modelDirs);
        TransferDescription td = ch.interlis.ili2c.Ili2c.compileIliFiles(new ArrayList(Arrays.asList(modelDirs)), new ArrayList());

        ValidationConfig modelConfig = new ValidationConfig();
        modelConfig.mergeIliMetaAttrs(td);

        PipelinePool pool = new PipelinePool();
        Validator validator = new ch.interlis.iox_j.validator.Validator(td, modelConfig, errHandler, errFactory, pool, settings);

        for (String filename : dataFiles) {
            IoxReader ioxReader = new ReaderFactory().createReader(new java.io.File(filename), errFactory);
            if (ioxReader instanceof IoxIliReader) {
                ((IoxIliReader) ioxReader).setModel(td);

            errFactory.setDataSource(filename);
            td.setActualRuntimeParameter(ch.interlis.ili2c.metamodel.RuntimeParameters.MINIMAL_RUNTIME_SYSTEM01_CURRENT_TRANSFERFILE, filename);
            try {
                IoxEvent event = null;
                do {
                    event = ioxReader.read();
                    validator.validate(event);
                } while (!(event instanceof EndTransferEvent));
            } finally {
                ioxReader.close();
            }
            }
        }
    }

    private String[] appendGeoWFunctionsExtIli(String[] modelDirs){
        String GeoW_FunctionsExtIliPath = "";
        ArrayList<String> result = new ArrayList<>(Arrays.asList(modelDirs));
        result.add(GeoW_FunctionsExtIliPath);
        return result.toArray(new String[0]);
    }


    public void AddFunction(InterlisFunction function) {
        userFunctions.put(function.getQualifiedIliName(), function.getClass());
    }
}
