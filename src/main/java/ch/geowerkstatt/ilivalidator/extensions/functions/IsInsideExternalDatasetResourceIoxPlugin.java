package ch.geowerkstatt.ilivalidator.extensions.functions;

import ch.interlis.iom.IomObject;
import ch.interlis.iox_j.validator.Value;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Polygon;

import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class IsInsideExternalDatasetResourceIoxPlugin extends BaseIsInsideFunction {
    private static final Map<ValidAreaKey, Geometry> VALID_AREA_CACHE = new HashMap<>();
    private static final String QUALIFIED_ILI_NAME = "GeoW_FunctionsExt.IsInsideExternalDatasetResource";

    @Override
    public String getQualifiedIliName() {
        return QUALIFIED_ILI_NAME;
    }

    @Override
    protected Value evaluateInternal(String validationKind, String usageScope, IomObject mainObj, Value[] arguments) {
        Value argTransferFile = arguments[0]; // TEXT
        Value argDatasetName = arguments[1]; // TEXT
        Value argObjects = arguments[2]; // TEXT
        Value argTestObject = arguments[3]; // OBJECT OF ANYCLASS
        Value argTestObjectgeometry = arguments[4]; // TEXT

        if (argTransferFile.isUndefined() || argDatasetName.isUndefined() || argObjects.isUndefined() || argTestObject.isUndefined() || argTestObjectgeometry.isUndefined()) {
            return Value.createSkipEvaluation();
        }

        String transferFile = argTransferFile.getValue();
        String transferIds = argObjects.getValue();
        String datasetName = argDatasetName.getValue();
        ValidAreaKey key = new ValidAreaKey(transferFile, datasetName, transferIds);
        String testObjectGeometryAttribute = argTestObjectgeometry.getValue();

        return isInsideValidArea(usageScope, argTestObject.getComplexObjects(), testObjectGeometryAttribute, () -> VALID_AREA_CACHE.computeIfAbsent(key, this::getValidArea));
    }

    /**
     * Retrieve the geometries from referenced data according to the specified key.
     *
     * @param key The key that specifies which geometries to retrieve.
     * @return The union of all found geometries.
     */
    private Geometry getValidArea(ValidAreaKey key) {
        String transferFile = key.getDataSource();
        String datasetName = key.getDatasetName();
        int lastPoint = datasetName.lastIndexOf('.');
        if (lastPoint == -1) {
            throw new IllegalStateException(MessageFormat.format("Expected qualified attribute name but got <{0}>.", datasetName));
        }

        String attribute = datasetName.substring(lastPoint + 1);
        String qualifiedClass = datasetName.substring(0, lastPoint);

        Polygon[] polygons = readPolygonsFromTransferFile(transferFile, key.getTransferIds(), qualifiedClass, attribute);

        Geometry geometryCollection = new GeometryFactory().createGeometryCollection(polygons);
        return geometryCollection.buffer(0);
    }

    private Polygon[] readPolygonsFromTransferFile(String transferFile, String[] transferIds, String qualifiedClass, String attribute) {
        ClassLoader loader = getClass().getClassLoader();
        try (InputStream stream = loader.getResourceAsStream(transferFile)) {
            if (stream == null) {
                throw new IllegalStateException(MessageFormat.format("Could not find Transferfile <{0}> in resources.", transferFile));
            }

            List<IomObject> objects = XtfHelper.getObjectsFromXTF(stream, qualifiedClass, transferIds);
            return getPolygonsFromObjects(objects.stream(), attribute);
        } catch (IOException e) {
            throw new IllegalStateException(MessageFormat.format("Unable to read Transferfile <{0}>.", transferFile), e);
        }
    }
}
