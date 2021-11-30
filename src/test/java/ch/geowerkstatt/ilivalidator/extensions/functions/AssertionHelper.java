package ch.geowerkstatt.ilivalidator.extensions.functions;

import ch.interlis.iox.IoxLogEvent;
import com.vividsolutions.jts.util.Assert;

public class AssertionHelper {

    public static void assertConstraintErrors(ValidationTestHelper vh, int expectedCount, String oid, String constraintName) {
        int errorsFound = 0;
        for (IoxLogEvent err : vh.getErrs()) {
            if (oid.equals(err.getSourceObjectXtfId()) && err.getEventMsg().contains(constraintName))
                errorsFound++;
        }

        Assert.equals(expectedCount, errorsFound,
                String.format("Expected %d but found %d errors with OID <%s> and Source <%s>.", expectedCount, errorsFound, oid, constraintName));
    }

    public static void assertSingleConstraintError(ValidationTestHelper vh, int oid, String constraintName) {
        assertConstraintErrors(vh, 1, Integer.toString(oid), constraintName);
    }

    public static void assertConstraintErrors(ValidationTestHelper vh, int expectedCount, String constraintName) {
        int errorsFound = 0;
        for (IoxLogEvent err : vh.getErrs()) {
            if (err.getEventMsg().contains(constraintName))
                errorsFound++;
        }

        Assert.equals(expectedCount, errorsFound,
                String.format("Expected %s errors with Source <%s> but found %d.", expectedCount, constraintName, errorsFound));
    }

    public static void assertNoConstraintError(ValidationTestHelper vh, String constraintName) {
        int errorsFound = 0;
        for (IoxLogEvent err : vh.getErrs()) {
            if (err.getEventMsg().contains(constraintName))
                errorsFound++;
        }

        Assert.equals(0, errorsFound,
                String.format("Expected No errors with Source <%s> but found %d.", constraintName, errorsFound));
    }
}
