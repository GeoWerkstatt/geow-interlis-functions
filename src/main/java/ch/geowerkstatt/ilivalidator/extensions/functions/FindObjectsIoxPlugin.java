package ch.geowerkstatt.ilivalidator.extensions.functions;

import ch.ehi.iox.objpool.impl.ObjPoolImpl2;
import ch.interlis.ili2c.metamodel.Element;
import ch.interlis.ili2c.metamodel.PathEl;
import ch.interlis.ili2c.metamodel.TextType;
import ch.interlis.ili2c.metamodel.Viewable;
import ch.interlis.iom.IomObject;
import ch.interlis.iox_j.validator.ObjectPoolKey;
import ch.interlis.iox_j.validator.Value;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public final class FindObjectsIoxPlugin extends BaseInterlisFunction {
    private static final Map<FindObjectsKey, List<IomObject>> OBJECTS_CACHE = new HashMap<>();

    @Override
    public String getQualifiedIliName() {
        return "GeoW_FunctionsExt.FindObjects";
    }

    @Override
    protected Value evaluateInternal(String validationKind, String usageScope, IomObject contextObject, Value[] arguments) {
        Value argClassName = arguments[0];
        Value argPath = arguments[1];
        Value argValue = arguments[2];

        if (argClassName.isUndefined() || argPath.isUndefined()) {
            return Value.createSkipEvaluation();
        }

        String className = argClassName.getValue();
        String attributePath = argPath.getValue();
        if (className == null || attributePath == null) {
            return Value.createUndefined();
        }

        FindObjectsKey key = new FindObjectsKey(className, attributePath, argValue);
        return new Value(OBJECTS_CACHE.computeIfAbsent(key, this::findObjects));
    }

    private List<IomObject> findObjects(FindObjectsKey key) {
        Element classElement = td.getElement(key.className);
        if (!(classElement instanceof Viewable)) {
            throw new IllegalStateException("Could not find class \"" + key.className + "\"");
        }
        PathEl[] attributePath = EvaluationHelper.getAttributePathEl(validator, (Viewable) classElement, new Value(new TextType(), key.attributePath));

        List<IomObject> objects = findObjectsOfClass(key.className);
        return objects.stream()
                .filter(object -> {
                    Value value = validator.getValueFromObjectPath(null, object, attributePath, null);
                    return value.compareTo(key.value) == 0;
                })
                .collect(Collectors.toList());
    }

    private List<IomObject> findObjectsOfClass(String className) {
        List<IomObject> objects = new ArrayList<>();
        for (String basketId : objectPool.getBasketIds()) {
            ObjPoolImpl2<ObjectPoolKey, IomObject> basketObjectPool = objectPool.getObjectsOfBasketId(basketId);
            Iterator<IomObject> valueIterator = basketObjectPool.valueIterator();
            while (valueIterator.hasNext()) {
                IomObject object = valueIterator.next();
                if (object.getobjecttag().equals(className)) {
                    objects.add(object);
                }
            }
        }
        return objects;
    }

    private static final class FindObjectsKey {
        private final String className;
        private final String attributePath;
        private final Value value;

        FindObjectsKey(String className, String attributePath, Value value) {
            this.className = className;
            this.attributePath = attributePath;
            this.value = value;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof FindObjectsKey)) {
                return false;
            }
            FindObjectsKey that = (FindObjectsKey) o;
            return className.equals(that.className)
                    && attributePath.equals(that.attributePath)
                    && value.compareTo(that.value) == 0;
        }

        @Override
        public int hashCode() {
            return Objects.hash(className, attributePath, value.getValue());
        }
    }
}
