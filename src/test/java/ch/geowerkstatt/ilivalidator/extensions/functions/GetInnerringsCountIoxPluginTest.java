package ch.geowerkstatt.ilivalidator.extensions.functions;

import ch.interlis.ili2c.Ili2cFailure;
import ch.interlis.iox.IoxException;
import com.vividsolutions.jts.util.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

class GetInnerringsCountIoxPluginTest {

    protected static final String TEST_DATA = "GetInnerRingsCount/TestData.xtf";
    ValidationTestHelper vh = null;

    @BeforeEach
    void setUp() {
        vh = new ValidationTestHelper();
        vh.addFunction(new GetInnerRingsCountIoxPlugin());
    }

    @Test
    void MandatoryConstraintOnThis() throws Ili2cFailure, IoxException {
        vh.runValidation(new String[]{TEST_DATA}, new String[]{"GetInnerRingsCount/MandatoryConstraintThis.ili"});
        Assert.equals(4, vh.getErrs().size());

        AssertionHelper.assertConstraintErrors(vh,0,"0", "AreaInnerRingCount");
        AssertionHelper.assertConstraintErrors(vh,0,"0", "SurfaceInnerRingCount");
        AssertionHelper.assertConstraintErrors(vh,0,"0", "MultiSurfaceInnerRingCount");

        AssertionHelper.assertConstraintErrors(vh, 1, "1", "AreaInnerRingCount");
        AssertionHelper.assertConstraintErrors(vh, 1, "1", "SurfaceInnerRingCount");
        AssertionHelper.assertConstraintErrors(vh, 0, "1", "MultiSurfaceInnerRingCount");

        AssertionHelper.assertConstraintErrors(vh, 1, "2", "AreaInnerRingCount");
        AssertionHelper.assertConstraintErrors(vh, 0, "2", "SurfaceInnerRingCount");
        AssertionHelper.assertConstraintErrors(vh, 1, "2", "MultiSurfaceInnerRingCount");

        AssertionHelper.assertConstraintErrors(vh, 0, "3", "AreaInnerRingCount");
        AssertionHelper.assertConstraintErrors(vh, 0, "3", "SurfaceInnerRingCount");
        AssertionHelper.assertConstraintErrors(vh, 0, "3", "MultiSurfaceInnerRingCount");
    }
}
