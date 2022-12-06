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

        AssertionHelper.assertConstraintErrors(vh,0,"0", "Constraint1");
        AssertionHelper.assertConstraintErrors(vh,0,"0", "Constraint2");
        AssertionHelper.assertConstraintErrors(vh,0,"0", "Constraint3");

        AssertionHelper.assertConstraintErrors(vh, 1, "1", "Constraint1");
        AssertionHelper.assertConstraintErrors(vh, 1, "1", "Constraint2");
        AssertionHelper.assertConstraintErrors(vh, 0, "1", "Constraint3");

        AssertionHelper.assertConstraintErrors(vh, 1, "2", "Constraint1");
        AssertionHelper.assertConstraintErrors(vh, 0, "2", "Constraint2");
        AssertionHelper.assertConstraintErrors(vh, 1, "2", "Constraint3");

        AssertionHelper.assertConstraintErrors(vh, 0, "3", "Constraint1");
        AssertionHelper.assertConstraintErrors(vh, 0, "3", "Constraint2");
        AssertionHelper.assertConstraintErrors(vh, 0, "3", "Constraint3");
    }
}
