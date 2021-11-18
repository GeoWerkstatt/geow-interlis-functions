package ch.geowerkstatt.ilivalidator.extensions.functions;

import ch.interlis.ili2c.Ili2cFailure;
import ch.interlis.iox.IoxException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GetLengthFunctionTest {

    ValidationTestHelper vh = null;
    protected static final String TEST_MODEL = "src/test/data/TestModel.ili";
    protected  static final String TEST_DATA = "src/test/data/TestData.xtf";

    @BeforeEach
    void setUp() {
        vh = new ValidationTestHelper();
        vh.addFunction(new GetLengthFunction());
    }

    @Test
    void MandatoryConstraintOnThis() throws Ili2cFailure, IoxException {
        vh.runValidation(new String[]{TEST_DATA}, new String[]{TEST_MODEL});
    }
}