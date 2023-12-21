package ch.geowerkstatt.ilivalidator.extensions.functions;

import ch.interlis.ili2c.Ili2cFailure;
import ch.interlis.iox.IoxException;
import com.vividsolutions.jts.util.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public final class IsInsideExternalDatasetResourceIoxPluginTest {
    private static final String TEST_DATA = "IsInsideExternalDatasetResource/TestData.xtf";
    private ValidationTestHelper vh;

    @BeforeEach
    void setUp() {
        vh = new ValidationTestHelper();
        vh.addFunction(new IsInsideExternalDatasetResourceIoxPlugin());
    }

    @Test
    void isInsideResourceDataset() throws Ili2cFailure, IoxException {
        vh.runValidation(new String[]{TEST_DATA}, new String[]{"IsInsideExternalDatasetResource/MandatoryConstraintThis.ili"});

        Assert.equals(6, vh.getErrs().size());
        AssertionHelper.assertLogEventsContainMessage(vh.getErrs(), "TestSuite.FunctionTestTopic.InvalidConstraints.MalformedDatasetName: Unable to evaluate GeoW_FunctionsExt.IsInsideExternalDatasetResource. Expected qualified attribute name but got <DatasetNameWithoutQualifiedAttribute>.");
        AssertionHelper.assertLogEventsContainMessage(vh.getErrs(), "TestSuite.FunctionTestTopic.InvalidConstraints.NonExistentTransferFile: Unable to evaluate GeoW_FunctionsExt.IsInsideExternalDatasetResource. Could not find Transferfile <NotExistingFile.xtf> in resources.");
        AssertionHelper.assertLogEventsContainMessage(vh.getErrs(), "TestSuite.FunctionTestTopic.InvalidConstraints.NonExistentTransferIds: Unable to evaluate GeoW_FunctionsExt.IsInsideExternalDatasetResource. Could not find objects with TID <100000000, 9999> in transfer file");
        AssertionHelper.assertLogEventsContainMessage(vh.getErrs(), "Mandatory Constraint TestSuite.FunctionTestTopic.InvalidConstraints.MalformedDatasetName is not true.");
        AssertionHelper.assertLogEventsContainMessage(vh.getErrs(), "Mandatory Constraint TestSuite.FunctionTestTopic.InvalidConstraints.NonExistentTransferFile is not true.");
        AssertionHelper.assertLogEventsContainMessage(vh.getErrs(), "Mandatory Constraint TestSuite.FunctionTestTopic.InvalidConstraints.NonExistentTransferIds is not true.");
    }
}
