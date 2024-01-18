package ch.geowerkstatt.ilivalidator.extensions.functions;

import ch.ehi.basics.types.OutParam;
import ch.interlis.iom.IomObject;
import ch.interlis.iox.IoxException;
import ch.interlis.iox_j.jts.Iox2jtsext;
import ch.interlis.iox_j.validator.Value;
import com.vividsolutions.jts.geom.Geometry;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public final class IsInsideIoxPlugin extends BaseIsInsideFunction {
    private static final Map<IomObject, Geometry> VALID_AREA_CACHE = new HashMap<>();
    private static final String QUALIFIED_ILI_NAME = "GeoW_FunctionsExt.IsInside";

    @Override
    public String getQualifiedIliName() {
        return QUALIFIED_ILI_NAME;
    }

    @Override
    protected Value evaluateInternal(String validationKind, String usageScope, IomObject mainObj, Value[] arguments) {
        Value argReferenceObject = arguments[0]; // ANYSTRUCTURE
        Value argReferenceGeometryPath = arguments[1]; // TEXT
        Value argTestObject = arguments[2]; // OBJECT OF ANYCLASS
        Value argTestObjectgeometry = arguments[3]; // TEXT

        if (argTestObject.isUndefined() || argTestObjectgeometry.isUndefined()) {
            return Value.createSkipEvaluation();
        }
        if (argReferenceObject.isUndefined()) {
            writeLogErrorMessage(usageScope, "Missing reference geometry.");
            return Value.createUndefined();
        }

        Collection<IomObject> referenceGeometryObjects = EvaluationHelper.evaluateObjectPath(td, validator, argReferenceObject, argReferenceGeometryPath, mainObj, usageScope);
        if (referenceGeometryObjects.size() != 1) {
            writeLogErrorMessage(usageScope, "Expected exactly one reference geometry.");
            return Value.createUndefined();
        }

        IomObject referenceGeometry = referenceGeometryObjects.iterator().next();

        return isInsideValidArea(usageScope, argTestObject, argTestObjectgeometry, () -> VALID_AREA_CACHE.computeIfAbsent(referenceGeometry, this::getValidArea));
    }

    private Geometry getValidArea(IomObject referenceGeometry) {
        try {
            return Iox2jtsext.multisurface2JTS(referenceGeometry, 0.0, new OutParam<>(), logger, 0.0, "warning");
        } catch (IoxException e) {
            throw new IllegalStateException(e);
        }
    }
}
