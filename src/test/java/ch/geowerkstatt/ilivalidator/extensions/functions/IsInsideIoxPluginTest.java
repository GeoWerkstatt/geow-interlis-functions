package ch.geowerkstatt.ilivalidator.extensions.functions;

import ch.interlis.ili2c.Ili2cFailure;
import ch.interlis.iox.IoxException;
import com.vividsolutions.jts.util.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public final class IsInsideIoxPluginTest {
    private static final String TEST_DATA = "IsInside/TestData.xtf";
    private ValidationTestHelper vh;

    @BeforeEach
    void setUp() {
        vh = new ValidationTestHelper();
        vh.addFunction(new IsInsideIoxPlugin());
    }

    @Test
    void isInside() throws Ili2cFailure, IoxException {
        vh.runValidation(new String[]{TEST_DATA}, new String[]{"IsInside/MandatoryConstraintThis.ili"});

        Assert.equals(2, vh.getErrs().size());
        AssertionHelper.assertLogEventsContainMessage(vh.getErrs(), "TestSuite.FunctionTestTopic.InvalidConstraints.IsInsideMissingArea: Unable to evaluate GeoW_FunctionsExt.IsInside. Missing reference geometry.");
        AssertionHelper.assertLogEventsContainMessage(vh.getErrs(), "Mandatory Constraint TestSuite.FunctionTestTopic.InvalidConstraints.IsInsideMissingArea is not true.");
    }
}
