package ch.geowerkstatt.ilivalidator.extensions.functions;

import ch.interlis.ili2c.Ili2cFailure;
import ch.interlis.iox.IoxException;
import com.vividsolutions.jts.util.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public final class UnionIoxPluginTest {
    private static final String TEST_DATA = "Union/TestData.xtf";
    private static final String TEST_DATA_MERGED_INNER_RINGS = "Union/TestDataMergedInnerRings.xtf";
    private ValidationTestHelper vh;

    @BeforeEach
    void setUp() {
        vh = new ValidationTestHelper();
        vh.addFunction(new GetInnerRingsCountIoxPlugin());
        vh.addFunction(new UnionIoxPlugin());
    }

    @Test
    void mandatoryConstraintOnThis() throws Ili2cFailure, IoxException {
        vh.runValidation(new String[]{TEST_DATA}, new String[]{"Union/MandatoryConstraintThis.ili"});
        Assert.equals(3, vh.getErrs().size());
        AssertionHelper.assertConstraintErrors(vh, 2, "falseConstraint");
        AssertionHelper.assertConstraintErrors(vh, 0, "0", "oneInnerRingConstraint");
        AssertionHelper.assertConstraintErrors(vh, 1, "1", "oneInnerRingConstraint");
    }

    @Test
    void mandatoryConstraintMergedInnerRings() throws Ili2cFailure, IoxException {
        vh.runValidation(new String[]{TEST_DATA_MERGED_INNER_RINGS}, new String[]{"Union/MandatoryConstraintMergedInnerRings.ili"});
        Assert.equals(0, vh.getErrs().size());
        AssertionHelper.assertNoConstraintError(vh, "innerRings");
        AssertionHelper.assertNoConstraintError(vh, "innerRingsUnion");
    }

    @Test
    void test() throws Exception {
        vh.runValidation(new String[]{"Union/UnionTest.xtf"}, new String[]{"Union/UnionTest.ili"});
        Assert.equals(0, vh.getErrs().size());
    }
}
