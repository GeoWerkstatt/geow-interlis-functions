package ch.geowerkstatt.ilivalidator.extensions.functions;

import ch.interlis.ili2c.Ili2cFailure;
import ch.interlis.iox.IoxException;
import com.vividsolutions.jts.util.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public final class PolylinesNotOverlappingIoxPluginTest {
    private static final String TEST_DATA = "PolylinesNotOverlapping/TestData.xtf";
    private ValidationTestHelper vh = null;

    @BeforeEach
    void setUp() {
        vh = new ValidationTestHelper();
        vh.addFunction(new PolylinesNotOverlappingIoxPlugin());
    }

    @Test
    void polylinesNotOverlapping() throws Ili2cFailure, IoxException {
        vh.runValidation(new String[]{TEST_DATA}, new String[]{"PolylinesNotOverlapping/PolylinesNotOverlapping.ili"});
        Assert.equals(2, vh.getErrs().size());
        AssertionHelper.assertConstraintErrors(vh, 1, "setConstraintAll");
        AssertionHelper.assertConstraintErrors(vh, 1, "setConstraintT1");
        AssertionHelper.assertNoConstraintError(vh, "setConstraintT2");
        AssertionHelper.assertNoConstraintError(vh, "setConstraintT3");
        AssertionHelper.assertNoConstraintError(vh, "setConstraintT2or3");
    }
}
