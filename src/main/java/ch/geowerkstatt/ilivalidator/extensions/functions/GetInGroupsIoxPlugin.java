package ch.geowerkstatt.ilivalidator.extensions.functions;

import ch.interlis.ili2c.metamodel.PathEl;
import ch.interlis.ili2c.metamodel.Viewable;
import ch.interlis.iom.IomObject;
import ch.interlis.iox_j.validator.Value;

import java.security.acl.Group;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class GetInGroupsIoxPlugin extends BaseInterlisFunction {

    private static Map<GroupKey, List<IomObject>> groups = new HashMap<>();

    @Override
    public String getQualifiedIliName() {
        return "GeoW_FunctionsExt.GetInGroups";
    }

    @Override
    public Value evaluateInternal(String validationKind, String usageScope, IomObject contextObject, Value[] arguments) {
        Value argObjects = arguments[0];
        Value argPath = arguments[1];

        if (argObjects.isUndefined()) {
            return Value.createSkipEvaluation();
        }

        Collection<IomObject> inputObjects = argObjects.getComplexObjects();
        if (inputObjects == null) {
            return Value.createUndefined();
        }

        Viewable contextClass = EvaluationHelper.getContextClass(td, contextObject, argObjects);
        if (contextClass == null) {
            throw new IllegalStateException("unknown class in " + usageScope);
        }
        PathEl[] attributePath = EvaluationHelper.getAttributePathEl(validator, contextClass, argPath);

        GroupKey key = new GroupKey(inputObjects, validator.getValueFromObjectPath(null, contextObject, attributePath, null));
        if(!groups.containsKey(key)){
            FillGroups(inputObjects, attributePath);
        }

        return new Value(groups.get(key));
    }

    private void FillGroups(Collection<IomObject> inputObjects, PathEl[] attributePath) {
        for (IomObject object: inputObjects) {
            GroupKey key = new GroupKey(inputObjects, validator.getValueFromObjectPath(null, object, attributePath, null));
            if(!groups.containsKey(key)){
                groups.put(key, new ArrayList<>());
            }

            groups.get(key).add(object);
        }
    }

    private static class GroupKey
    {
        private Collection<IomObject> inputObjects;
        private Value groupId;
        public GroupKey(Collection<IomObject> inputObjects, Value groupId){
            this.inputObjects = inputObjects;
            this.groupId = groupId;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            GroupKey that = (GroupKey) o;
            return inputObjects.equals(that.inputObjects) && groupId.compareTo(that.groupId) == 0;
        }

        @Override
        public int hashCode() {
            return Objects.hash(inputObjects, groupId.getValue());
        }
    }
}
