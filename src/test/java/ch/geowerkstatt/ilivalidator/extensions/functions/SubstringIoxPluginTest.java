package ch.geowerkstatt.ilivalidator.extensions.functions;

import ch.interlis.ili2c.Ili2cFailure;
import ch.interlis.iox.IoxException;
import com.vividsolutions.jts.util.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class SubstringIoxPluginTest {
    protected static final String TEST_DATA = "GetLength/TestData.xtf";
    ValidationTestHelper vh = null;

    @BeforeEach
    public void setUp() {
        vh = new ValidationTestHelper();
        vh.addFunction(new SubstringIoxPlugin());
    }

    @Test
    public void Substring() throws Ili2cFailure, IoxException {
        vh.runValidation(new String[]{"Substring/Substring.xtf"}, new String[]{"Substring/Substring.ili"});
        Assert.equals(0, vh.getErrs().size());
    }

    @Test
    public void SubstringEndIndexLowerThanStart() throws Ili2cFailure, IoxException {
        vh.runValidation(new String[]{"Substring/SubstringEndIndexLowerThanStart.xtf"}, new String[]{"Substring/Substring.ili"});
        Assert.equals(0, vh.getErrs().size());
    }

    @Test
    public void SubstringEndIndexBiggerThanText() throws Ili2cFailure, IoxException {
        vh.runValidation(new String[]{"Substring/SubstringEndIndexBiggerThanText.xtf"}, new String[]{"Substring/Substring.ili"});
        Assert.equals(0, vh.getErrs().size());
    }

    @Test
    public void SubstringStartIndexBelowZero() throws Ili2cFailure, IoxException {
        vh.runValidation(new String[]{"Substring/SubstringStartIndexBelowZero.xtf"}, new String[]{"Substring/Substring.ili"});
        Assert.equals(0, vh.getErrs().size());
    }

    @Test()
    public void SubstringIndexTooLarge() {
        assertThrows(NumberFormatException.class, () -> {
            vh.runValidation(new String[]{"Substring/SubstringIndexTooLarge.xtf"}, new String[]{"Substring/Substring.ili"});
        });
    }
}
