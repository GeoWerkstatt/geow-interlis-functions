package ch.geowerkstatt.ilivalidator.extensions.functions;

import ch.interlis.ili2c.Ili2cFailure;
import ch.interlis.iox.IoxException;
import com.vividsolutions.jts.util.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class GetInGroupsIoxPluginTest {

    protected static final String TEST_DATA = "GetInGroups/TestData.xtf";
    protected static final String TEST_DATA_EMPTY = "GetInGroups/TestDataEmpty.xtf";
    ValidationTestHelper vh = null;

    @BeforeEach
    void setUp() {
        vh = new ValidationTestHelper();
        vh.addFunction(new GetInGroupsIoxPlugin());
    }

    @Test
    void mandatoryConstraintOnThis() throws Ili2cFailure, IoxException {
        vh.runValidation(new String[]{TEST_DATA}, new String[]{"GetInGroups/SetConstraintAll.ili"});
        Assert.equals(3, vh.getErrs().size());
        AssertionHelper.assertNoConstraintError(vh, "trueConstraintTextAttr");
        AssertionHelper.assertNoConstraintError(vh, "trueConstraintEnumAttr");
        AssertionHelper.assertNoConstraintError(vh, "trueConstraintNumberAttr");
        AssertionHelper.assertSingleConstraintError(vh, 0, "falseConstraintTextAttr");
        AssertionHelper.assertSingleConstraintError(vh, 0, "falseConstraintEnumAttr");
        AssertionHelper.assertSingleConstraintError(vh, 0, "falseConstraintNumberAttr");
    }

    @Test
    void emptySet() throws Ili2cFailure, IoxException {
        vh.runValidation(new String[]{TEST_DATA_EMPTY}, new String[]{"GetInGroups/SetConstraintEmpty.ili"});
        Assert.equals(1, vh.getErrs().size());
        AssertionHelper.assertNoConstraintError(vh, "trueConstraintTextAttr");
        AssertionHelper.assertConstraintErrors(vh, 1, "falseConstraintTextAttr");
    }
}
