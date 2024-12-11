package ch.geowerkstatt.ilivalidator.extensions.functions;

import ch.interlis.ili2c.Ili2cFailure;
import ch.interlis.iox.IoxException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GetInGroupsIoxPluginTest {

    protected static final String TEST_DATA = "GetInGroups/TestData.xtf";
    protected static final String TEST_DATA_EMPTY = "GetInGroups/TestDataEmpty.xtf";
    protected static final String TEST_DATA_ONLY_T2 = "GetInGroups/TestDataOnlyT2.xtf";
    ValidationTestHelper vh = null;

    @BeforeEach
    void setUp() {
        vh = new ValidationTestHelper();
        vh.addFunction(new GetInGroupsIoxPlugin());
    }

    @Test
    void mandatoryConstraintOnThis() throws Ili2cFailure, IoxException {
        vh.runValidation(new String[]{TEST_DATA}, new String[]{"GetInGroups/SetConstraintAll.ili"});
        assertEquals(3, vh.getErrs().size());
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
        assertEquals(1, vh.getErrs().size());
        AssertionHelper.assertNoConstraintError(vh, "trueConstraintTextAttr");
        AssertionHelper.assertConstraintErrors(vh, 1, "falseConstraintTextAttr");
    }

    @Test
    void emptyAfterWhereCondition() throws Ili2cFailure, IoxException {
        vh.runValidation(new String[]{TEST_DATA_ONLY_T2}, new String[]{"GetInGroups/SetConstraintWhereCondition.ili"});
        assertEquals(1, vh.getErrs().size());
        AssertionHelper.assertConstraintErrors(vh, 1, "onlyT1");
        AssertionHelper.assertNoConstraintError(vh, "onlyT2");
    }
}
