package ch.geowerkstatt.ilivalidator.extensions.functions;

import ch.interlis.ili2c.Ili2cFailure;
import ch.interlis.ili2c.metamodel.SetConstraint;
import ch.interlis.iox.IoxException;
import com.vividsolutions.jts.util.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GetLengthFunctionTest {

    ValidationTestHelper vh = null;
    protected  static final String TEST_DATA = "GetLength/TestData.xtf";

    @BeforeEach
    void setUp() {
        vh = new ValidationTestHelper();
        vh.addFunction(new GetLengthFunction());
    }

    @Test
    void MandatoryConstraintOnThis() throws Ili2cFailure, IoxException {
        vh.runValidation(new String[]{TEST_DATA}, new String[]{"GetLength/MandatoryConstraintThis.ili"});
        Assert.equals(4, vh.getErrs().size());
        AssertionHelper.assertSingleConstraintError(vh, 0, "Constraint1");
        AssertionHelper.assertSingleConstraintError(vh, 0, "Constraint2");
        AssertionHelper.assertSingleConstraintError(vh, 3, "Constraint3");
        AssertionHelper.assertSingleConstraintError(vh, 3, "Constraint4");
    }

    @Test
    void SetConstraintOnAll() throws Ili2cFailure, IoxException {
        vh.runValidation(new String[]{TEST_DATA}, new String[]{"GetLength/SetConstraintAll.ili"});
        Assert.equals(3, vh.getErrs().size());
        AssertionHelper.assertNoConstraintError(vh,"Constraint1");
        AssertionHelper.assertNoConstraintError(vh,"Constraint2");
        AssertionHelper.assertConstraintErrors(vh, 1, "Constraint3");
        AssertionHelper.assertConstraintErrors(vh, 1, "Constraint4");
        AssertionHelper.assertConstraintErrors(vh, 1, "Constraint5");
    }

    @Test
    void SetConstraintOnThis() throws Ili2cFailure, IoxException {
        vh.runValidation(new String[]{TEST_DATA}, new String[]{"GetLength/SetConstraintThis.ili"});
        Assert.equals(2, vh.getErrs().size());
        AssertionHelper.assertSingleConstraintError(vh, 0, "Constraint2");
        AssertionHelper.assertSingleConstraintError(vh, 3, "Constraint3");
    }

    @Test
    void SetConstraintOnThisWithBagOfStruct() throws Ili2cFailure, IoxException {
        vh.runValidation(new String[]{"GetLength/TestDataBagOf.xtf"}, new String[]{"GetLength/SetConstraintBagOf.ili"});
        Assert.equals(1, vh.getErrs().size());
        AssertionHelper.assertNoConstraintError(vh, "Constraint1");
        AssertionHelper.assertSingleConstraintError(vh, 0, "Constraint2");
        AssertionHelper.assertNoConstraintError(vh, "Constraint3");
        AssertionHelper.assertSingleConstraintError(vh, 0, "Constraint4");

    }

    @Test
    void SetConstraintOnAssociation() throws Ili2cFailure, IoxException {
        vh.runValidation(new String[]{"GetLength/TestDataAssocOne2Many.xtf"}, new String[]{"GetLength/SetConstraintAssocOne2Many.ili"});
        Assert.equals(1, vh.getErrs().size());
    }
}