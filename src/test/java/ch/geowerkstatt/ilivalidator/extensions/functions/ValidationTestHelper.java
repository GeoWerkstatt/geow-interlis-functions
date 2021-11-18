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
import java.util.HashMap;

public class ValidationTestHelper {

    HashMap<String, Class<InterlisFunction>> userFunctions = new HashMap<String, Class<InterlisFunction>>();

    public void runValidation(String[] dataFiles, String[] modelFiles) throws IoxException, Ili2cFailure {
        IoxLogging errHandler = new ch.interlis.iox_j.logging.Log2EhiLogger();
        LogEventFactory errFactory = new LogEventFactory();
        errFactory.setLogger(errHandler);

        Settings settings = new Settings();
        settings.setTransientObject(ch.interlis.iox_j.validator.Validator.CONFIG_CUSTOM_FUNCTIONS, userFunctions);

        modelFiles = appendGeoWFunctionsExtIli(modelFiles);
        TransferDescription td = ch.interlis.ili2c.Ili2c.compileIliFiles(new ArrayList<String>(Arrays.asList(modelFiles)), new ArrayList<String>());

        ValidationConfig modelConfig = new ValidationConfig();
        modelConfig.mergeIliMetaAttrs(td);

        PipelinePool pool = new PipelinePool();
        Validator validator = new ch.interlis.iox_j.validator.Validator(td, modelConfig, errHandler, errFactory, pool, settings);

        for (String filename : dataFiles) {
            IoxReader ioxReader = new ReaderFactory().createReader(new java.io.File(filename), errFactory, settings);
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
        System.out.println("Working Directory = " + System.getProperty("user.dir"));
        String GeoW_FunctionsExtIliPath = "src/model/GeoW_FunctionsExt.ili";
        ArrayList<String> result = new ArrayList<>();
        result.add(GeoW_FunctionsExtIliPath);
        result.addAll(Arrays.asList(modelDirs));
        return result.toArray(new String[0]);
    }

    @SuppressWarnings("unchecked")
    public void addFunction(InterlisFunction function) {
        userFunctions.put(function.getQualifiedIliName(), (Class<InterlisFunction>) function.getClass());
    }
}
