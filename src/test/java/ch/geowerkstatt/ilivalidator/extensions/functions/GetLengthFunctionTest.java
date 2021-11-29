package ch.geowerkstatt.ilivalidator.extensions.functions;

import ch.interlis.ili2c.Ili2cFailure;
import ch.interlis.iox.IoxException;
import com.vividsolutions.jts.util.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

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
    @Disabled("XTF24Reader fails on reading Bag Of Struct")
    void SetConstraintOnThisWithBagOfStruct() throws Ili2cFailure, IoxException {
        vh.runValidation(new String[]{"GetLength/TestDataBagOf.xtf"}, new String[]{"GetLength/SetConstraintBagOf.ili"});
        Assert.equals(1, vh.getErrs().size());
        AssertionHelper.assertNoConstraintError(vh, "Constraint1");
        AssertionHelper.assertSingleConstraintError(vh, 0, "Constraint2");
        AssertionHelper.assertNoConstraintError(vh, "Constraint3");
        AssertionHelper.assertSingleConstraintError(vh, 0, "Constraint4");

    }

    @Test
    void SetConstraintOnAssociationOne2Many() throws Ili2cFailure, IoxException {
        vh.runValidation(new String[]{"GetLength/TestDataAssocOne2Many.xtf"}, new String[]{"GetLength/SetConstraintAssocOne2Many.ili"});
        AssertionHelper.assertNoConstraintError(vh, "Constraint1");

        AssertionHelper.assertSingleConstraintError(vh, 0, "Constraint2");

        AssertionHelper.assertSingleConstraintError(vh, 0, "Constraint3");
        AssertionHelper.assertSingleConstraintError(vh, 1, "Constraint3");

        AssertionHelper.assertSingleConstraintError(vh, 0, "Constraint4");
        AssertionHelper.assertSingleConstraintError(vh, 1, "Constraint4");
        AssertionHelper.assertSingleConstraintError(vh, 2, "Constraint4");

        AssertionHelper.assertNoConstraintError(vh, "Constraint5");
        AssertionHelper.assertSingleConstraintError(vh, 0, "Constraint6");
        Assert.equals(7, vh.getErrs().size());
    }

    @Test
    void SetConstraintOnAssociationMany2Many() throws Ili2cFailure, IoxException {
        vh.runValidation(new String[]{"GetLength/TestDataAssocMany2Many.xtf"}, new String[]{"GetLength/SetConstraintAssocMany2Many.ili"});
        AssertionHelper.assertNoConstraintError(vh, "Constraint1");

        AssertionHelper.assertSingleConstraintError(vh, 0, "Constraint2");

        AssertionHelper.assertSingleConstraintError(vh, 0, "Constraint3");
          AssertionHelper.assertSingleConstraintError(vh, 1, "Constraint3");

        AssertionHelper.assertSingleConstraintError(vh, 0, "Constraint4");
        AssertionHelper.assertSingleConstraintError(vh, 1, "Constraint4");
        AssertionHelper.assertSingleConstraintError(vh, 2, "Constraint4");

        AssertionHelper.assertSingleConstraintError(vh, 0, "Constraint5");
        AssertionHelper.assertSingleConstraintError(vh, 1, "Constraint5");
        AssertionHelper.assertSingleConstraintError(vh, 2, "Constraint5");
        AssertionHelper.assertSingleConstraintError(vh, 3, "Constraint5");

        AssertionHelper.assertNoConstraintError(vh, "Constraint6");
        AssertionHelper.assertSingleConstraintError(vh, 0, "Constraint7");
        AssertionHelper.assertNoConstraintError(vh, "Constraint8");
        Assert.equals(11, vh.getErrs().size());
    }
}