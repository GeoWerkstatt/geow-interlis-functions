package ch.geowerkstatt.ilivalidator.extensions.functions;

import ch.interlis.ili2c.Ili2cFailure;
import ch.interlis.iox.IoxException;
import com.vividsolutions.jts.util.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

class FilterIoxPluginTest {
    protected static final String TEST_DATA = "Filter/TestData.xtf";
    private ValidationTestHelper vh;

    @BeforeEach
    public void setUp() {
        vh = new ValidationTestHelper();
        vh.addFunction(new FilterIoxPlugin());
    }

    @Test
    public void mandatoryConstraint() throws Ili2cFailure, IoxException {
        vh.runValidation(new String[]{TEST_DATA}, new String[]{"Filter/MandatoryConstraints.ili"});
        Assert.equals(2, vh.getErrs().size());
        AssertionHelper.assertNoConstraintError(vh, "trueConstraintEnumAttr");
        AssertionHelper.assertNoConstraintError(vh, "trueConstraintNumberAttr");
        AssertionHelper.assertConstraintErrors(vh, 1, "base", "falseConstraintEnumAttr");
        AssertionHelper.assertConstraintErrors(vh, 1, "base", "falseConstraintNumberAttr");
    }

    @Test
    @Disabled("Escape sequences in strings (https://github.com/claeis/ili2c/issues/124)")
    public void filterTextAttr() throws Ili2cFailure, IoxException {
        vh.runValidation(new String[]{TEST_DATA}, new String[]{"Filter/MandatoryConstraintsText.ili"});
        Assert.equals(1, vh.getErrs().size());
        AssertionHelper.assertNoConstraintError(vh, "trueConstraintTextAttr");
        AssertionHelper.assertConstraintErrors(vh, 1, "base", "falseConstraintTextAttr");
    }
}
