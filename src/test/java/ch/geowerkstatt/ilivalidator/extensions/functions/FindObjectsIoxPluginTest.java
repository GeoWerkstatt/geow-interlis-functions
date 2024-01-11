package ch.geowerkstatt.ilivalidator.extensions.functions;

import ch.interlis.ili2c.Ili2cFailure;
import ch.interlis.iox.IoxException;
import com.vividsolutions.jts.util.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class FindObjectsIoxPluginTest {
    protected static final String TEST_DATA = "FindObjects/TestData.xtf";
    private ValidationTestHelper vh = null;

    @BeforeEach
    void setUp() {
        vh = new ValidationTestHelper();
        vh.addFunction(new FindObjectsIoxPlugin());
    }

    @Test
    void mandatoryConstraint() throws Ili2cFailure, IoxException {
        vh.runValidation(new String[]{TEST_DATA}, new String[]{"FindObjects/MandatoryConstraints.ili"});
        Assert.equals(3, vh.getErrs().size());
        AssertionHelper.assertNoConstraintError(vh, "trueConstraintTextAttr");
        AssertionHelper.assertNoConstraintError(vh, "trueConstraintEnumAttr");
        AssertionHelper.assertNoConstraintError(vh, "trueConstraintNumberAttr");
        AssertionHelper.assertConstraintErrors(vh, 1, "base", "falseConstraintTextAttr");
        AssertionHelper.assertConstraintErrors(vh, 1, "base", "falseConstraintEnumAttr");
        AssertionHelper.assertConstraintErrors(vh, 1, "base", "falseConstraintNumberAttr");
    }
}
