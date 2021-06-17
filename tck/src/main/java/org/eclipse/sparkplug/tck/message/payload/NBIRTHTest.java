package org.eclipse.sparkplug.tck.message.payload;

import com.hivemq.extension.sdk.api.annotations.NotNull;
import org.eclipse.sparkplug.tck.message.MessagePayload;
import org.eclipse.sparkplug.tck.message.MessageType;
import org.eclipse.sparkplug.tck.message.TestResult;
import org.eclipse.sparkplug.tck.protobuf.SparkplugBProto;
import org.eclipse.sparkplug.tck.sparkplug.Sections;
import org.jboss.test.audit.annotations.SpecAssertion;
import org.jboss.test.audit.annotations.SpecVersion;
import org.junit.jupiter.api.Test;

/**
 * Tests assertions in chapter 6.4.19 - PAYLOADS_B_NBIRTH
 *
 * @author Lukas Brand
 */
@SpecVersion(spec = "sparkplug", version = "3.0.0-SNAPSHOT")
public class NBIRTHTest implements MessagePayload {

    /*
        <assertion id="payloads_nbirth_timestamp">
            <text>NBIRTH messages MUST include a payload timestamp that denotes the time at which the message was published.</text>
        </assertion>
        <assertion id="payloads_nbirth_edge_node_descriptor">
            <text>Every Edge Node Descriptor in any Sparkplug infrastructure MUST be unique in the system.</text>
        </assertion>
        <assertion id="payloads_nbirth_seq">
            <text>Every NBIRTH message MUST include a sequence number and it MUST have a value of 0.</text>
        </assertion>
        <assertion id="payloads_nbirth_bdseq">
            <text>Every NBIRTH message MUST include a bdSeq number metric.</text>
        </assertion>
        <assertion id="payloads_nbirth_bdseq_inc">
            <text>Every NBIRTH message SHOULD include a bdSeq number value that is one greater than the previous NBIRTH's bdSeq number. This value MUST never exceed 255. If in the previous NBIRTH a value of 255 was sent, the next NBIRTH MUST have a value of 0.</text>
        </assertion>
        <assertion id="payloads_nbirth_bdseq_inc">
            <text>Every NBIRTH MUST include a metric with the name 'Node Control/Rebirth' and have a boolean value of false.</text>
        </assertion>
        <assertion id="payloads_nbirth_qos">
            <text>NBIRTH messages MUST be published with the MQTT QoS set to 0.</text>
        </assertion>
        <assertion id="payloads_nbirth_retain">
            <text>NBIRTH messages MUST be published with the MQTT retain flag set to false.</text>
        </assertion>
     */

    @Override
    @Test
    @SpecAssertion(section = Sections.PAYLOADS_B_NBIRTH, id = "payloads_nbirth_timestamp")
    @SpecAssertion(section = Sections.PAYLOADS_B_NBIRTH, id = "payloads_nbirth_seq")
    public @NotNull TestResult testMessage(final @NotNull SparkplugBProto.Payload payload) {
        final TestResult testResult = new TestResult();
        PayloadTest payloadTest = new PayloadTest();

        testResult.addSubResult(payloadTest.testTimestamp(payload));
        testResult.addSubResult(payloadTest.testSequenceNumber(payload, MessageType.NBIRTH));


        return null;
    }

}
