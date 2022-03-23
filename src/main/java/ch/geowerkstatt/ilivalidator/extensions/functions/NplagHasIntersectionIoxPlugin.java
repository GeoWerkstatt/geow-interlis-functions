package ch.geowerkstatt.ilivalidator.extensions.functions;

import ch.ehi.iox.objpool.impl.ObjPoolImpl2;
import ch.interlis.ili2c.metamodel.TransferDescription;
import ch.interlis.ili2c.metamodel.Viewable;
import ch.interlis.iom.IomObject;
import ch.interlis.iom_j.itf.impl.jtsext.geom.CompoundCurve;
import ch.interlis.iox.IoxException;
import ch.interlis.iox_j.jts.Iox2jtsext;
import ch.interlis.iox_j.validator.ObjectPool;
import ch.interlis.iox_j.validator.ObjectPoolKey;
import ch.interlis.iox_j.validator.Value;
import com.vividsolutions.jts.geom.Polygon;

import java.util.*;
import java.util.stream.Collectors;

public class NplagHasIntersectionIoxPlugin extends BaseInterlisFunction {
    private final Map<Viewable, List<Polygon>> basePolygonCache = new HashMap<>();

    @Override
    protected Value evaluateInternal(String validationKind, String usageScope, IomObject mainObj, Value[] actualArguments) {
        Value targetObject = actualArguments[0];
        Value baseClassConstant = actualArguments[1];

        if (targetObject.isUndefined() || baseClassConstant.isUndefined()) {
            return Value.createSkipEvaluation();
        }

        if (targetObject.getComplexObjects() == null || targetObject.getComplexObjects().size() != 1) {
            return Value.createUndefined();
        }

        IomObject lineObject = targetObject.getComplexObjects().iterator().next();

        // check if KTCode of targetObject has a specific value
        IomObject ktCodeRef = lineObject.getattrobj("Typ_GDE_U_LinieR", 0);
        IomObject typLinie = objectPool.getObject(ktCodeRef.getobjectrefoid(), null, null);
        String ktCode = typLinie.getattrvalue("KTCode");
        if (!ktCode.equals("N7931")) {
            return Value.createUndefined();
        }

        // get Linienbezogene_Festlegung geometry
        IomObject lineGeometryAttribute = lineObject.getattrobj("Geom", 0);
        CompoundCurve polyline = polyline2JtsOrNull(lineGeometryAttribute);

        // get Grundnutzung geometry
        String currentBid = getBidOfIomObject(mainObj, td, objectPool);
        Viewable baseObjectViewable = baseClassConstant.getViewable();
        List<Polygon> basePolygons = basePolygonCache.computeIfAbsent(baseObjectViewable, (v) ->
                getGeometryIomObjects(v, "Geom", currentBid).stream()
                .map(this::surface2JtsOrNull)
                .filter(Objects::nonNull)
                .collect(Collectors.toList()));

        // determine if geometries intersect
        boolean hasIntersection = basePolygons.stream().anyMatch(p -> p.intersects(polyline));
        return new Value(hasIntersection);
    }

    @Override
    public String getQualifiedIliName() {
        return "GeoW_FunctionsExt.NplagHasIntersection";
    }

    /**
     * Get a specific attribute object from every instance of a class or struct.
     *
     * @param classOrStructViewable The class or struct, that contains the attribute.
     * @param geomAttribute The name of the attribute.
     * @param currentBid The id of the basket to get the objects from.
     * @return A list of attribute objects.
     */
    private List<IomObject> getGeometryIomObjects(Viewable classOrStructViewable, String geomAttribute, String currentBid) {
        ArrayList<IomObject> geometries = new ArrayList<>();

        ObjPoolImpl2<ObjectPoolKey, IomObject> objectsOfBasketId = objectPool.getObjectsOfBasketId(currentBid);
        objectsOfBasketId.valueIterator().forEachRemaining(obj -> {
            Viewable element = (Viewable) td.getElement(obj.getobjecttag());

            if (element.equals(classOrStructViewable)) {
                for (int i = 0; i < obj.getattrvaluecount(geomAttribute); i++) {
                    IomObject geometry = obj.getattrobj(geomAttribute, i);
                    geometries.add(geometry);
                }
            }
        });

        return geometries;
    }

    private Polygon surface2JtsOrNull (IomObject surface) {
        try {
            return Iox2jtsext.surface2JTS(surface, 0.0);
        } catch (IoxException ex) {
            logger.addEvent(logger.logErrorMsg("Could not calculate {0}", this.getQualifiedIliName()));
        }
        return null;
    }

    private CompoundCurve polyline2JtsOrNull (IomObject polyline) {
        try {
            return Iox2jtsext.polyline2JTS(polyline, false, 0.0);
        } catch (IoxException ex) {
            logger.addEvent(logger.logErrorMsg("Could not calculate {0}", this.getQualifiedIliName()));
        }
        return null;
    }

    /**
     * Retrieve the basked id (BID) from {@code iomObjects}.
     */
    private static String getBidOfIomObject(IomObject iomObject, TransferDescription td, ObjectPool objectPool) {
        String objectOid = iomObject.getobjectoid();
        Viewable viewable = (Viewable) td.getElement(iomObject.getobjecttag());
        String bidOfObject = objectPool.getBidOfObject(objectOid, viewable);

        return bidOfObject;
    }
}
