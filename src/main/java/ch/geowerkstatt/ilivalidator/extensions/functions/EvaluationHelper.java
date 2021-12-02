package ch.geowerkstatt.ilivalidator.extensions.functions;

import ch.ehi.basics.logging.EhiLogger;
import ch.interlis.ili2c.Ili2cException;
import ch.interlis.ili2c.metamodel.Element;
import ch.interlis.ili2c.metamodel.ObjectPath;
import ch.interlis.ili2c.metamodel.PathEl;
import ch.interlis.ili2c.metamodel.TransferDescription;
import ch.interlis.ili2c.metamodel.Viewable;
import ch.interlis.iom.IomObject;
import ch.interlis.iox_j.validator.Validator;
import ch.interlis.iox_j.validator.Value;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.function.Function;

public class EvaluationHelper {
    public static PathEl[] getAttributePathEl(Validator validator, Viewable<Element> contextClass, Value argPath) {
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

    public static Viewable getContextClass(TransferDescription td, IomObject iomObject, Value argObjects) {
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

    public static Collection<IomObject> evaluateAttributes(Validator validator, Value argObjects, PathEl[] attributePath) {
        Collection<IomObject> attributes = new ArrayList<>();

        for (IomObject rootObject : argObjects.getComplexObjects()) {
            Value surfaceAttributes = validator.getValueFromObjectPath(null, rootObject, attributePath, null);
            if (!(surfaceAttributes.isUndefined() || surfaceAttributes.skipEvaluation() || surfaceAttributes.getComplexObjects() == null)) {
                attributes.addAll(surfaceAttributes.getComplexObjects());
            }
        }

        return attributes;
    }

    public static Double sum(Collection<IomObject> items, Function<IomObject, Double> func) {
        return items
                .stream()
                .map(func)
                .reduce(0.0, Double::sum);
    }

}
