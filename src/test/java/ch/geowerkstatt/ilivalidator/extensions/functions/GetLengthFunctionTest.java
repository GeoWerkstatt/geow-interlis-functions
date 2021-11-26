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
        Assert.equals("0", vh.getErrs().get(0).getSourceObjectXtfId());
        Assert.equals("Mandatory Constraint TestSuite.FunctionTestTopic.BaseClass.Constraint1 is not true.", vh.getErrs().get(0).getEventMsg());

        Assert.equals("0", vh.getErrs().get(1).getSourceObjectXtfId());
        Assert.equals("Mandatory Constraint TestSuite.FunctionTestTopic.BaseClass.Constraint2 is not true.", vh.getErrs().get(1).getEventMsg());

        Assert.equals("3", vh.getErrs().get(2).getSourceObjectXtfId());
        Assert.equals("Mandatory Constraint TestSuite.FunctionTestTopic.BaseClass.Constraint3 is not true.", vh.getErrs().get(2).getEventMsg());

        Assert.equals("3", vh.getErrs().get(3).getSourceObjectXtfId());
        Assert.equals("Mandatory Constraint TestSuite.FunctionTestTopic.BaseClass.Constraint4 is not true.", vh.getErrs().get(3).getEventMsg());

    }

    @Test
    void SetConstraintOnAll() throws Ili2cFailure, IoxException {
        vh.runValidation(new String[]{TEST_DATA}, new String[]{"GetLength/SetConstraintAll.ili"});
        Assert.equals(1, vh.getErrs().size());
    }

    @Test
    void SetConstraintOnThis() throws Ili2cFailure, IoxException {
        vh.runValidation(new String[]{TEST_DATA}, new String[]{"GetLength/SetConstraintThis.ili"});
        Assert.equals(1, vh.getErrs().size());
    }

    @Test
    void SetConstraintOnThisWithBagOfStruct() throws Ili2cFailure, IoxException {
        vh.runValidation(new String[]{"GetLength/TestDataBagOf.xtf"}, new String[]{"GetLength/SetConstraintBagOf.ili"});
        Assert.equals(1, vh.getErrs().size());
    }

    @Test
    void SetConstraintOnAssociation() throws Ili2cFailure, IoxException {
        vh.runValidation(new String[]{"GetLength/TestDataAssociation.xtf"}, new String[]{"GetLength/SetConstraintAssocOne2Many.ili"});
        Assert.equals(1, vh.getErrs().size());
    }
}