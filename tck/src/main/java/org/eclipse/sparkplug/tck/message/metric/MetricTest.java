package org.eclipse.sparkplug.tck.message.metric;

import com.hivemq.extension.sdk.api.annotations.NotNull;
import org.eclipse.sparkplug.tck.message.TestResult;
import org.eclipse.sparkplug.tck.protobuf.SparkplugBProto;
import org.eclipse.sparkplug.tck.sparkplug.Sections;
import org.jboss.test.audit.annotations.SpecAssertion;
import org.jboss.test.audit.annotations.SpecVersion;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.concurrent.TimeUnit;

/**
 * Tests assertions in chapter 6.4.6 - PAYLOADS_B_METRIC
 *
 * @author Lukas Brand
 */
@SpecVersion(spec = "sparkplug", version = "3.0.0-SNAPSHOT")
public class MetricTest {

    private final static long UTC_EPSILON = 10; //Minutes between server test time and payload timestamp time.
    /*
        <assertion id="payloads_alias_uniqueness">
            <text>If supplied in an NBIRTH or DBIRTH it MUST be a unique number across this Edge Node's entire set of metrics.</text>
        </assertion>
        <assertion id="payloads_metric_timestamp_in_UTC" testable="false">
            <text>This timestamp MUST be in in UTC.</text>
        </assertion>
        <assertion id="payloads_metric_datatype_value_type">
            <text>This MUST be an unsigned 32-bit integer representing the datatype.</text>
        </assertion>
        <assertion id="payloads_metric_datatype_value">
            <text>This value MUST be one of the enumerated values as shown in the valid Sparkplug Data Types.</text>
        </assertion>
        <assertion id="payloads_metric_datatype_req">
            <text>This MUST be included in Metric Definitions in NBIRTH and DBIRTH messages.</text>
        </assertion>
     */

    @Test
    @SpecAssertion(section = Sections.PAYLOADS_B_METRIC, id = "payloads_metric_timestamp_in_UTC")
    public @NotNull TestResult testTimestamp(final @NotNull SparkplugBProto.Payload.Metric metric) {
        final TestResult testResult = new TestResult();

        if (!metric.hasTimestamp()) {
            testResult.addIssue("Payload MUST include a timestamp.");
        } else {
            if (Instant.now().toEpochMilli() - metric.getTimestamp() < TimeUnit.MINUTES.toMillis(UTC_EPSILON)) {
                testResult.addIssue("Payload timestamp MUST be in UTC.");
            }
        }

        return testResult;
    }

    @Test
    @SpecAssertion(section = Sections.PAYLOADS_B_METRIC, id = "payloads_metric_datatype_value_type")
    @SpecAssertion(section = Sections.PAYLOADS_B_METRIC, id = "payloads_metric_datatype_value")
    public @NotNull TestResult testDatatypeValueType(final @NotNull SparkplugBProto.Payload.Metric metric) {
        final TestResult testResult = new TestResult();

        if (!metric.hasDatatype()) {
            testResult.addIssue("Payload MUST include a timestamp.");
        } else {
            //metric.getDatatype() always returns an int due to the protobuf implementation.
            //Its value is always between 0x0 and 0xFFF_FFFF.


        }

        return testResult;
    }
}
