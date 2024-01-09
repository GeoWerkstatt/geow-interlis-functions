package ch.geowerkstatt.ilivalidator.extensions.functions;

import ch.interlis.ili2c.metamodel.PathEl;
import ch.interlis.ili2c.metamodel.Viewable;
import ch.interlis.iom.IomObject;
import ch.interlis.iox_j.validator.Value;

import java.util.*;

public final class GetInGroupsIoxPlugin extends BaseInterlisFunction {

    private static Map<GroupKey, List<IomObject>> groups = new HashMap<>();

    @Override
    public String getQualifiedIliName() {
        return "GeoW_FunctionsExt.GetInGroups";
    }

    @Override
    public Value evaluateInternal(String validationKind, String usageScope, IomObject contextObject, Value[] arguments) {
        Value argObjects = arguments[0];
        Value argPath = arguments[1];

        if (argObjects.isUndefined() || argPath.isUndefined()) {
            return Value.createSkipEvaluation();
        }

        Collection<IomObject> inputObjects = argObjects.getComplexObjects();
        if (inputObjects == null) {
            return Value.createUndefined();
        }

        if (inputObjects.isEmpty()) {
            return new Value(Collections.emptyList());
        }

        Viewable contextClass = EvaluationHelper.getContextClass(td, contextObject, argObjects);
        if (contextClass == null) {
            throw new IllegalStateException("unknown class in " + usageScope);
        }
        PathEl[] attributePath = EvaluationHelper.getAttributePathEl(validator, contextClass, argPath);

        GroupKey key = new GroupKey(inputObjects, validator.getValueFromObjectPath(null, contextObject, attributePath, null));
        if (!groups.containsKey(key)) {
            fillGroups(inputObjects, attributePath);
        }

        return new Value(groups.get(key));
    }

    private void fillGroups(Collection<IomObject> inputObjects, PathEl[] attributePath) {
        for (IomObject object: inputObjects) {
            GroupKey key = new GroupKey(inputObjects, validator.getValueFromObjectPath(null, object, attributePath, null));
            if (!groups.containsKey(key)) {
                groups.put(key, new ArrayList<>());
            }

            groups.get(key).add(object);
        }
    }

    private static final class GroupKey {
        private Collection<IomObject> inputObjects;
        private Value groupId;
        GroupKey(Collection<IomObject> inputObjects, Value groupId) {
            this.inputObjects = inputObjects;
            this.groupId = groupId;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            GroupKey that = (GroupKey) o;
            return inputObjects.equals(that.inputObjects) && groupId.compareTo(that.groupId) == 0;
        }

        @Override
        public int hashCode() {
            return Objects.hash(inputObjects, groupId.getValue());
        }
    }
}
