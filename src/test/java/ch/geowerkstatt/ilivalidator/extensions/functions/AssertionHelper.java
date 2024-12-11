package ch.geowerkstatt.ilivalidator.extensions.functions;

import ch.interlis.iox.IoxLogEvent;

import java.util.List;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

public final class AssertionHelper {

    private AssertionHelper() {
        // Utility class
    }

    public static void assertConstraintErrors(ValidationTestHelper vh, int expectedCount, String oid, String constraintName) {
        int errorsFound = 0;
        for (IoxLogEvent err : vh.getErrs()) {
            if (oid.equals(err.getSourceObjectXtfId()) && err.getEventMsg().contains(String.format(".%s ", constraintName))) {
                errorsFound++;
            }
        }

        assertEquals(expectedCount, errorsFound,
                String.format("Expected %d but found %d errors with OID <%s> and Source <%s>.", expectedCount, errorsFound, oid, constraintName));
    }

    public static void assertSingleConstraintError(ValidationTestHelper vh, int oid, String constraintName) {
        assertConstraintErrors(vh, 1, Integer.toString(oid), constraintName);
    }

    public static void assertConstraintErrors(ValidationTestHelper vh, int expectedCount, String constraintName) {
        int errorsFound = 0;
        for (IoxLogEvent err : vh.getErrs()) {
            if (err.getEventMsg().contains(String.format(".%s ", constraintName))) {
                errorsFound++;
            }
        }

        assertEquals(expectedCount, errorsFound,
                String.format("Expected %s errors with Source <%s> but found %d.", expectedCount, constraintName, errorsFound));
    }

    public static void assertNoConstraintError(ValidationTestHelper vh, String constraintName) {
        int errorsFound = 0;
        for (IoxLogEvent err : vh.getErrs()) {
            if (err.getEventMsg().contains(String.format(".%s ", constraintName))) {
                errorsFound++;
            }
        }

        assertEquals(0, errorsFound,
                String.format("Expected No errors with Source <%s> but found %d.", constraintName, errorsFound));
    }

    public static void assertLogEventsContainMessage(List<IoxLogEvent> logs, String expectedMessageRegex) {
        Pattern pattern = Pattern.compile(expectedMessageRegex);
        if (logs.stream().noneMatch(log -> pattern.matcher(log.getEventMsg()).find())) {
            fail(String.format("The logs are missing the message <%s>.", expectedMessageRegex));
        }
    }
}
