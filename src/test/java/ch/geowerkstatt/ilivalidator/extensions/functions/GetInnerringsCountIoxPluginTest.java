package ch.geowerkstatt.ilivalidator.extensions.functions;

import ch.interlis.ili2c.Ili2cFailure;
import ch.interlis.iox.IoxException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GetInnerringsCountIoxPluginTest {

    protected static final String TEST_DATA = "GetInnerRingsCount/TestData.xtf";
    ValidationTestHelper vh = null;

    @BeforeEach
    void setUp() {
        vh = new ValidationTestHelper();
        vh.addFunction(new GetInnerRingsCountIoxPlugin());
    }

    @Test
    void mandatoryConstraintOnThis() throws Ili2cFailure, IoxException {
        vh.runValidation(new String[]{TEST_DATA}, new String[]{"GetInnerRingsCount/MandatoryConstraintThis.ili"});
        assertEquals(5, vh.getErrs().size());

        AssertionHelper.assertConstraintErrors(vh, 1, "1", "AreaInnerRingCount");
        AssertionHelper.assertConstraintErrors(vh, 1, "1", "SurfaceInnerRingCount");

        AssertionHelper.assertConstraintErrors(vh, 1, "2", "AreaInnerRingCount");
        AssertionHelper.assertConstraintErrors(vh, 1, "2", "MultiSurfaceInnerRingCount");

        AssertionHelper.assertConstraintErrors(vh, 1, "4", "StructCapsuledSurface");
    }
}
