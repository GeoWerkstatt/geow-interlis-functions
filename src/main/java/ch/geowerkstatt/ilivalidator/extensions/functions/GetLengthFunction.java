package ch.geowerkstatt.ilivalidator.extensions.functions;

import ch.ehi.basics.logging.EhiLogger;
import ch.ehi.basics.settings.Settings;
import ch.interlis.ili2c.Ili2cException;
import ch.interlis.ili2c.metamodel.ObjectPath;
import ch.interlis.ili2c.metamodel.PathEl;
import ch.interlis.ili2c.metamodel.TransferDescription;
import ch.interlis.ili2c.metamodel.Viewable;
import ch.interlis.iom.IomObject;
import ch.interlis.iox.IoxException;
import ch.interlis.iox.IoxValidationConfig;
import ch.interlis.iox_j.jts.Iox2jtsext;
import ch.interlis.iox_j.logging.LogEventFactory;
import ch.interlis.iox_j.validator.InterlisFunction;
import ch.interlis.iox_j.validator.ObjectPool;
import ch.interlis.iox_j.validator.Validator;
import ch.interlis.iox_j.validator.Value;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

public class GetLengthFunction implements InterlisFunction {

    private LogEventFactory logger;
    private TransferDescription td;
    private Validator validator;

    @Override
    public void init(TransferDescription td, Settings settings, IoxValidationConfig validationConfig, ObjectPool objectPool, LogEventFactory logEventFactory) {
        this.logger = logEventFactory;
        this.logger.setValidationConfig(validationConfig);
        this.td = td;
        this.validator = (Validator) settings.getTransientObject(IOX_VALIDATOR);
    }

    @Override
    public Value evaluate(String validationKind, String usageScope, IomObject iomObject, Value[] arguments) {
        Value argObjects=arguments[0];
        Value argPath = arguments[1];

        if (argObjects.skipEvaluation()){
            return argObjects;
        }
        if (argObjects.isUndefined()){
            return Value.createSkipEvaluation();
        }
        if (argObjects.getComplexObjects() == null){
            return Value.createUndefined();
        }

        Collection<IomObject> polylines = new ArrayList<>();
        Viewable contextClass;

        if (argPath.isUndefined() || argPath.skipEvaluation()){
            polylines.addAll(argObjects.getComplexObjects());
        } else {
            contextClass = getContextClass(iomObject, argObjects);

            if (contextClass == null){
                throw new IllegalStateException("unknown class in " + usageScope);
            }

            PathEl[] polylineAttributePath = getAttributePathEl(contextClass, argPath);

            for (IomObject rootObject : argObjects.getComplexObjects()) {
                Value polylineAttributes = validator.getValueFromObjectPath(null, rootObject, polylineAttributePath, null);
                if(!(polylineAttributes.isUndefined() || polylineAttributes.skipEvaluation() || polylineAttributes.getComplexObjects() == null ))
                {
                    polylines.addAll(polylineAttributes.getComplexObjects());
                }
            }
        }

        double result = 0.0d;

        for (IomObject polyline: polylines) {
            try {
                result += Iox2jtsext.polyline2JTS(polyline, false, 0.0).getLength();
            }catch (IoxException ex){
                logger.addEvent(logger.logErrorMsg("Could not calculate GetLength for Object {0}", polyline.toString()));
            }
        }

        return new Value(result);
    }

    private PathEl[] getAttributePathEl(Viewable contextClass, Value argPath) {
        try {
            ObjectPath objectPath = validator.parseObjectOrAttributePath(contextClass, argPath.getValue());
            if (objectPath.getPathElements() != null) {
                return objectPath.getPathElements();
            }
        } catch (Ili2cException e) {
            EhiLogger.logError(e);
        }
        return null;
    }

    private Viewable getContextClass(IomObject iomObject, Value argObjects) {
        if (iomObject != null) {
            return (Viewable) td.getElement(iomObject.getobjecttag());
        } else if (argObjects.getViewable() != null) {
            return argObjects.getViewable();
        } else if (argObjects.getComplexObjects() != null) {
            Iterator<IomObject> it = argObjects.getComplexObjects().iterator();
            if (!it.hasNext()) {
                return null;
            }
            return (Viewable) td.getElement(it.next().getobjecttag());
        }
        return null;
    }

    @Override
    public String getQualifiedIliName() {
        return "GeoW_FunctionsExt.GetLength";
    }
}
