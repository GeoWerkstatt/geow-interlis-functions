package ch.geowerkstatt.ilivalidator.extensions.functions;

import ch.ehi.basics.settings.Settings;
import ch.interlis.ili2c.Ili2c;
import ch.interlis.ili2c.Ili2cFailure;
import ch.interlis.ili2c.metamodel.Evaluable;
import ch.interlis.ili2c.metamodel.Expression;
import ch.interlis.ili2c.metamodel.TransferDescription;
import ch.interlis.ili2c.metamodel.Viewable;
import ch.interlis.iom.IomObject;
import ch.interlis.iom_j.Iom_jObject;
import ch.interlis.iox_j.PipelinePool;
import ch.interlis.iox_j.logging.LogEventFactory;
import ch.interlis.iox_j.validator.ValidationConfig;
import ch.interlis.iox_j.validator.Validator;
import ch.interlis.iox_j.validator.Value;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InterlisExpressionParserTest {
    private static final String ILI_FILE = "src/test/data/ExpressionParser/ExpressionParser.ili";
    private static final String CLASS_NAME = "TestSuite.FunctionTestTopic.TestClass";
    private TransferDescription td;
    private Viewable<?> viewable;
    private LogCollector logCollector;
    private Validator validator;

    @BeforeEach
    public void setUp() throws Ili2cFailure {
        ArrayList<String> modelFiles = new ArrayList<>();
        modelFiles.add(ILI_FILE);
        td = Ili2c.compileIliFiles(modelFiles, new ArrayList<String>());
        viewable = (Viewable<?>) td.getElement(CLASS_NAME);

        logCollector = new LogCollector();
        validator = new Validator(td, new ValidationConfig(), logCollector, new LogEventFactory(), new PipelinePool(), new Settings());
    }

    @Test
    public void parseWhereExpressionTrueResult() {
        InterlisExpressionParser parser = InterlisExpressionParser.createParser(td, "WHERE THIS->enumAttr == #a;");
        Evaluable evaluable = parser.parseWhereExpression(viewable);

        assertEquals(Expression.Equality.class, evaluable.getClass());

        IomObject iomObject = createIomObject("1", "a");
        Value value = validator.evaluateExpression(null, "test", "test", iomObject, evaluable, null);

        assertEquals(0, logCollector.getErrs().size());
        assertTrue(value.isTrue());
    }

    @Test
    public void parseWhereExpressionFalseResult() {
        InterlisExpressionParser parser = InterlisExpressionParser.createParser(td, "WHERE enumAttr != #c;");
        Evaluable evaluable = parser.parseWhereExpression(viewable);

        assertEquals(Expression.Inequality.class, evaluable.getClass());

        IomObject iomObject = createIomObject("2", "c");
        Value value = validator.evaluateExpression(null, "test", "test", iomObject, evaluable, null);

        assertEquals(0, logCollector.getErrs().size());
        assertEquals(false, value.isTrue());
    }

    private static IomObject createIomObject(String oid, String enumAttrValue) {
        IomObject iomObject = new Iom_jObject(CLASS_NAME, oid);
        iomObject.setattrvalue("enumAttr", enumAttrValue);
        return iomObject;
    }
}
