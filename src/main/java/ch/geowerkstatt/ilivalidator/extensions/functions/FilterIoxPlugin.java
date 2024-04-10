package ch.geowerkstatt.ilivalidator.extensions.functions;

import ch.interlis.ili2c.metamodel.Evaluable;
import ch.interlis.ili2c.metamodel.Viewable;
import ch.interlis.iom.IomObject;
import ch.interlis.iox_j.validator.Value;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public final class FilterIoxPlugin extends BaseInterlisFunction {
    private static final HashMap<ExpressionKey, Evaluable> EXPRESSION_CACHE = new HashMap<>();

    @Override
    public String getQualifiedIliName() {
        return "GeoW_FunctionsExt.Filter";
    }

    @Override
    protected Value evaluateInternal(String validationKind, String usageScope, IomObject contextObject, Value[] arguments) {
        Value argObjects = arguments[0];
        Value argFilter = arguments[1];

        if (argObjects.isUndefined() || argObjects.getComplexObjects() == null) {
            return Value.createUndefined();
        }

        Collection<IomObject> objects = argObjects.getComplexObjects();
        if (objects.isEmpty()) {
            return argObjects;
        }

        Viewable<?> objectClass = EvaluationHelper.getCommonBaseClass(td, objects);
        if (objectClass == null) {
            throw new IllegalStateException("Objects have no common base class in " + usageScope);
        }

        ExpressionKey expressionKey = new ExpressionKey(objectClass, argFilter.getValue());
        Evaluable filter = EXPRESSION_CACHE.computeIfAbsent(expressionKey, key -> parseFilterExpression(key.objectClass, key.filter, usageScope));

        List<IomObject> filteredObjects = objects.stream()
                .filter(object -> {
                    Value value = validator.evaluateExpression(null, validationKind, usageScope, object, filter, null);
                    return value.skipEvaluation() || value.isTrue();
                })
                .collect(Collectors.toList());

        return new Value(filteredObjects);
    }

    private Evaluable parseFilterExpression(Viewable<?> objectClass, String filter, String usageScope) {
        InterlisExpressionParser parser = InterlisExpressionParser.createParser(td, filter);
        parser.setFilename(getQualifiedIliName() + ":" + usageScope);
        return parser.parseWhereExpression(objectClass);
    }

    private static final class ExpressionKey {
        private final Viewable<?> objectClass;
        private final String filter;

        private ExpressionKey(Viewable<?> objectClass, String filter) {
            this.objectClass = objectClass;
            this.filter = filter;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof ExpressionKey)) {
                return false;
            }
            ExpressionKey that = (ExpressionKey) o;
            return Objects.equals(objectClass, that.objectClass) && Objects.equals(filter, that.filter);
        }

        @Override
        public int hashCode() {
            return Objects.hash(objectClass, filter);
        }
    }
}
