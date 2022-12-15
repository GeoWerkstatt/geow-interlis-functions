package ch.geowerkstatt.ilivalidator.extensions.functions;

import ch.interlis.ili2c.Ili2cFailure;
import ch.interlis.iox.IoxException;
import com.vividsolutions.jts.util.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.lang.reflect.Field;

public class IsInsideExternalDatasetIoxPluginTest {
    protected static final String TEST_DATA = "IsInsideExternalDataset/TestData.xtf";
    ValidationTestHelper vh;

    @BeforeEach
    void setUp() throws NoSuchFieldException, IllegalAccessException {
        IsInsideExternalDatasetIoxPlugin interlisFunction = new IsInsideExternalDatasetIoxPlugin();

        Field dataDirectory = IsInsideExternalDatasetIoxPlugin.class.getDeclaredField("dataDirectory");
        dataDirectory.setAccessible(true);
        dataDirectory.set(null, new File("src/test/data/IsInsideExternalDataset").getAbsoluteFile());

        vh = new ValidationTestHelper();
        vh.addFunction(interlisFunction);
    }

    @Test
    void IsInsideExternalDataset() throws Ili2cFailure, IoxException {
        vh.runValidation(new String[]{TEST_DATA}, new String[]{"IsInsideExternalDataset/MandatoryConstraintThis.ili"});

        Assert.equals(6, vh.getErrs().size());
        AssertionHelper.assertLogEventsContainMessage(vh.getErrs(), "TestSuite.FunctionTestTopic.InvalidConstraints.MalformedDatasetName: Unable to evaluate GeoW_FunctionsExt.IsInsideExternalDataset. Expected qualified attribute name but got <DatasetNameWithoutQualifiedAttribute>.");
        AssertionHelper.assertLogEventsContainMessage(vh.getErrs(), "TestSuite.FunctionTestTopic.InvalidConstraints.NonExistentDatasetName: Unable to evaluate GeoW_FunctionsExt.IsInsideExternalDataset. Could not find Transferfile containing model <DoesNotExist>.");
        AssertionHelper.assertLogEventsContainMessage(vh.getErrs(), "TestSuite.FunctionTestTopic.InvalidConstraints.NonExistentTransferIds: Unable to evaluate GeoW_FunctionsExt.IsInsideExternalDataset. Could not find objects with TID <100000000, 9999> in transfer file");
        AssertionHelper.assertLogEventsContainMessage(vh.getErrs(), "Mandatory Constraint TestSuite.FunctionTestTopic.InvalidConstraints.MalformedDatasetName is not true.");
        AssertionHelper.assertLogEventsContainMessage(vh.getErrs(), "Mandatory Constraint TestSuite.FunctionTestTopic.InvalidConstraints.NonExistentDatasetName is not true.");
        AssertionHelper.assertLogEventsContainMessage(vh.getErrs(), "Mandatory Constraint TestSuite.FunctionTestTopic.InvalidConstraints.NonExistentTransferIds is not true.");

        Assert.equals(1, vh.getWarn().size());
        // Interlis 2.4 XTF files do not work.
        AssertionHelper.assertLogEventsContainMessage(vh.getWarn(), "GeoW_FunctionsExt.IsInsideExternalDataset: Error while reading xtf file \\S*TestData.xtf. Expected TRANSFER, but transfer found.");
    }
}
