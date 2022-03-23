package ch.geowerkstatt.ilivalidator.extensions.functions;

import ch.interlis.ili2c.Ili2cFailure;
import ch.interlis.iox.IoxException;
import com.vividsolutions.jts.util.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class HasIntersectionIoxPluginTest {

    protected static final String TEST_ILI = "HasIntersection/HasIntersection.ili";
    ValidationTestHelper vh = null;

    @BeforeEach
    void setUp() {
        vh = new ValidationTestHelper();
        vh.addFunction(new NplagHasIntersectionIoxPlugin());
    }

    @Test
    void NoIntersection() throws Ili2cFailure, IoxException {
        vh.runValidation(new String[]{"HasIntersection/HasIntersection.xtf"}, new String[]{TEST_ILI});
        Assert.equals(1, vh.getErrs().size());
        AssertionHelper.assertConstraintErrors(vh, 1, "7", "overlayConstraint");
    }

    @Test
    void WithIntersection() throws Ili2cFailure, IoxException {
        vh.runValidation(new String[]{"HasIntersection/WithIntersection.xtf"}, new String[]{TEST_ILI});
        Assert.equals(0, vh.getErrs().size());
    }
}
