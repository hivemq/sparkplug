package org.eclipse.sparkplug.tck.message.payload;

import com.hivemq.extension.sdk.api.annotations.NotNull;
import org.eclipse.sparkplug.tck.message.MessageType;
import org.eclipse.sparkplug.tck.message.TestResult;
import org.eclipse.sparkplug.tck.protobuf.SparkplugBProto;
import org.eclipse.sparkplug.tck.sparkplug.Sections;
import org.jboss.test.audit.annotations.SpecAssertion;
import org.jboss.test.audit.annotations.SpecVersion;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.concurrent.TimeUnit;

/**
 * Tests assertions in chapter 6.4.5 - PAYLOADS_B_PAYLOAD
 *
 * @author Lukas Brand
 */
@SpecVersion(spec = "sparkplug", version = "3.0.0-SNAPSHOT")
public class PayloadTest {

    private final static long UTC_EPSILON = 10; //Minutes between server test time and payload timestamp time.
    /*
        <assertion id="payloads_timestamp_in_UTC" testable="false">
            <text>This timestamp MUST be in UTC.</text>
        </assertion>
        <assertion id="payloads_sequence_num_always_included">
            <text>A sequence number MUST be included in the payload of every Sparkplug MQTT message except NDEATH messages.</text>
        </assertion>
        <assertion id="payloads_sequence_num_zero_nbirth">
            <text>A NBIRTH message MUST always contain a sequence number of zero.</text>
        </assertion>
        <assertion id="payloads_sequence_num_incrementing">
            <text>All subsequent messages MUST contain a sequence number that is continually increasing by one in each message until a value of 255 is reached. At that point, the sequence number of the following message MUST be zero.</text>
        </assertion>
     */

    @Test
    @SpecAssertion(section = Sections.PAYLOADS_B_PAYLOAD, id = "payloads_timestamp_in_UTC")
    public @NotNull TestResult testTimestamp(final @NotNull SparkplugBProto.Payload payload) {
        final TestResult testResult = new TestResult();
        if (!payload.hasTimestamp()) {
            testResult.addIssue("Payload MUST include a timestamp.");
        } else {
            if (Instant.now().toEpochMilli() - payload.getTimestamp() < TimeUnit.MINUTES.toMillis(UTC_EPSILON)) {
                testResult.addIssue("Payload timestamp MUST be in UTC.");
            }
        }
        return testResult;
    }

    @Test
    @SpecAssertion(section = Sections.PAYLOADS_B_PAYLOAD, id = "payloads_sequence_num_always_included")
    @SpecAssertion(section = Sections.PAYLOADS_B_PAYLOAD, id = "payloads_sequence_num_zero_nbirth")
    public @NotNull TestResult testSequenceNumber(final @NotNull SparkplugBProto.Payload payload,
                                                  final @NotNull MessageType messageType) {
        final TestResult testResult = new TestResult();
        switch (messageType) {
            case NDEATH:
                if (payload.hasSeq()) {
                    testResult.addIssue("Payload of message type NDEATH MUST include a sequence number.");
                }
                break;
            case NBIRTH:
                if (!payload.hasSeq()) {
                    testResult.addIssue("Payload of message type NBIRTH MUST include a sequence number.");
                } else {
                    if (payload.getSeq() != 0) {
                        testResult.addIssue("Payload sequence number of message type NBIRTH MUST be 0.");
                    }
                }
                break;
            case NDATA:
            case NCMD:
            case DBIRTH:
            case DDEATH:
            case DDATA:
            case DCMD:
                if (!payload.hasSeq()) {
                    testResult.addIssue("Payload of message type " + messageType + " MUST include a sequence number.");
                } else {
                    if (payload.getSeq() < 0 || payload.getSeq() > 255) {
                        testResult.addIssue("Payload sequence number MUST be a value between 0 and 255.");
                    }
                }
                break;
        }
        return testResult;
    }

    @Test
    @SpecAssertion(section = Sections.PAYLOADS_B_PAYLOAD, id = "payloads_sequence_num_always_included")
    @SpecAssertion(section = Sections.PAYLOADS_B_PAYLOAD, id = "payloads_sequence_num_zero_nbirth")
    public @NotNull TestResult testSequenceNumber(final @NotNull SparkplugBProto.Payload payload,
                                                  final @NotNull MessageType messageType,
                                                  final long desiredSequenceNumber,
                                                  final @NotNull String notEqualIssueText) {
        final TestResult testResult = testSequenceNumber(payload, messageType);
        if (messageType != MessageType.NDEATH && messageType != MessageType.NBIRTH) {
            if (payload.getSeq() != desiredSequenceNumber) {
                testResult.addIssue(notEqualIssueText);
            }
        }
        return testResult;
    }


}
