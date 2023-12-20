package ch.geowerkstatt.ilivalidator.extensions.functions;

import ch.interlis.ili2c.Ili2cFailure;
import ch.interlis.iox.IoxException;
import com.vividsolutions.jts.util.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public final class PolylinesOverlapIoxPluginTest {
    private static final String TEST_DATA = "PolylinesOverlap/TestData.xtf";
    private static final String TEST_DATA_SAME_LINE = "PolylinesOverlap/TestDataSameLine.xtf";
    private ValidationTestHelper vh = null;

    @BeforeEach
    void setUp() {
        vh = new ValidationTestHelper();
        vh.addFunction(new PolylinesOverlapIoxPlugin());
    }

    @Test
    void polylinesOverlap() throws Ili2cFailure, IoxException {
        vh.runValidation(new String[]{TEST_DATA}, new String[]{"PolylinesOverlap/PolylinesOverlap.ili"});
        Assert.equals(4, vh.getErrs().size());
        AssertionHelper.assertConstraintErrors(vh, 1, "setConstraintAllNoOverlaps");
        AssertionHelper.assertNoConstraintError(vh, "setConstraintT1"); // Some lines in T1 overlap
        AssertionHelper.assertConstraintErrors(vh, 1, "setConstraintT2");
        AssertionHelper.assertConstraintErrors(vh, 1, "setConstraintT3");
        AssertionHelper.assertNoConstraintError(vh, "setConstraintT1or2");
        AssertionHelper.assertConstraintErrors(vh, 1, "setConstraintT2or3");
    }

    @Test
    void sameLineOverlaps() throws Ili2cFailure, IoxException {
        vh.runValidation(new String[]{TEST_DATA_SAME_LINE}, new String[]{"PolylinesOverlap/PolylinesOverlap.ili"});
        Assert.equals(4, vh.getErrs().size());
        AssertionHelper.assertConstraintErrors(vh, 1, "setConstraintAllNoOverlaps");
        AssertionHelper.assertConstraintErrors(vh, 1, "setConstraintT1");
        AssertionHelper.assertConstraintErrors(vh, 1, "setConstraintT2");
        AssertionHelper.assertConstraintErrors(vh, 1, "setConstraintT3");
        AssertionHelper.assertNoConstraintError(vh, "setConstraintT1or2"); // One line in T1 is a segment of T2
        AssertionHelper.assertNoConstraintError(vh, "setConstraintT2or3"); // The lines in T2 and T3 are equal
    }
}
