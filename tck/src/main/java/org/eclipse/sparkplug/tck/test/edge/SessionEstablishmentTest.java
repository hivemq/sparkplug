/*******************************************************************************
 * Copyright (c) 2021 Ian Craggs
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Ian Craggs - initial implementation and documentation
 *******************************************************************************/

package org.eclipse.sparkplug.tck.test.edge;

import com.hivemq.extension.sdk.api.annotations.NotNull;
import com.hivemq.extension.sdk.api.annotations.Nullable;
import com.hivemq.extension.sdk.api.packets.connect.ConnectPacket;
import com.hivemq.extension.sdk.api.packets.connect.WillPublishPacket;
import com.hivemq.extension.sdk.api.packets.general.Qos;
import com.hivemq.extension.sdk.api.packets.publish.PublishPacket;
import com.hivemq.extension.sdk.api.packets.subscribe.SubscribePacket;
import org.eclipse.sparkplug.tck.sparkplug.Sections;
import org.eclipse.sparkplug.tck.test.TCK;
import org.eclipse.sparkplug.tck.test.BaseTCKTest;
import org.jboss.test.audit.annotations.SpecAssertion;
import org.jboss.test.audit.annotations.SpecVersion;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

@SpecVersion(
        spec = "sparkplug",
        version = "3.0.0-SNAPSHOT")
public class SessionEstablishmentTest extends BaseTCKTest {

    private final static @NotNull Logger logger = LoggerFactory.getLogger("Sparkplug");

    private final @NotNull HashMap<String, String> testResults = new HashMap<>();
    private final @NotNull List<String> testIds = List.of(
            "message-flow-edge-node-birth-publish-connect",
            "message-flow-edge-node-birth-publish-subscribe"
    );

    private final @NotNull TCK theTCK;

    private final @Nullable String host_application_id; // The primary host application id to be used

    private @Nullable String myClientId = null;
    private @Nullable String state = null;
    private boolean commands_supported = true; // Are commands supported by the edge node?

    enum TestType {
        GOOD,
        HOST_OFFLINE
    }

    private @NotNull TestType test_type = TestType.GOOD;


    public SessionEstablishmentTest(final @NotNull TCK aTCK, final @Nullable String @NotNull [] parms) {
        logger.info("Edge Node session establishment test");
        theTCK = aTCK;

        for (final String testId : testIds) {
            testResults.put(testId, "");
        }

        host_application_id = parms[0];
        logger.info("Host application id is " + host_application_id);

        if (parms.length > 1 && parms[1].equals("false")) {
            commands_supported = false;
        }

    }

    public void endTest() {
        state = null;
        myClientId = null;
        reportResults(testResults);
        for (final String testId : testIds) {
            testResults.put(testId, "");
        }
    }

    public @NotNull String getName() {
        return "SessionEstablishmentTest";
    }

    public @NotNull List<String> getTestIds() {
        return testIds;
    }

    public @NotNull HashMap<String, String> getResults() {
        return testResults;
    }

    public @NotNull String checkNDEATHMessage(final @NotNull String d0) {
        return "";
    }


    public @NotNull Optional<WillPublishPacket> checkWillMessage(final @NotNull ConnectPacket packet) {
        final Optional<WillPublishPacket> willPublishPacketOptional = packet.getWillPublish();
        if (willPublishPacketOptional.isPresent()) {
            WillPublishPacket willPublishPacket = willPublishPacketOptional.get();

            String result = "FAIL";
            final Optional<ByteBuffer> payload = willPublishPacket.getPayload();
            if (payload.isPresent() && "OFFLINE".equals(StandardCharsets.UTF_8.decode(payload.get()).toString())) {
                result = "PASS";
            }
            testResults.put("host-topic-phid-death-payload", result);

            result = "FAIL";
            if (willPublishPacket.getQos() == Qos.AT_LEAST_ONCE) {
                result = "PASS";
            }
            testResults.put("host-topic-phid-death-qos", result);

            result = "FAIL";
            if (willPublishPacket.getRetain()) {
                result = "PASS";
            }
            testResults.put("host-topic-phid-death-retain", result);
        }
        return willPublishPacketOptional;
    }

    @Test
    @SpecAssertion(
            section = Sections.OPERATIONAL_BEHAVIOR_EDGE_NODE_SESSION_ESTABLISHMENT,
            id = "message-flow-edge-node-birth-publish-connect")
    public void onClientConnect(@NotNull String clientId, @NotNull ConnectPacket packet) {
        logger.info("Primary host session establishment test - connect");

        String result = "FAIL";
        Optional<WillPublishPacket> willPublishPacketOptional = null;
        try {
            willPublishPacketOptional = checkWillMessage(packet);
            if (willPublishPacketOptional.isPresent()) {
                result = "PASS";
            }
            //testResults.put("primary-application-death-cert", result);
        } catch (final Exception e) {
            logger.info("Exception", e);
        }

        try {
            myClientId = clientId;
            state = "CONNECTED";
            if (willPublishPacketOptional.isEmpty())
                throw new Exception("Will message is needed");
            if (!packet.getCleanStart())
                throw new Exception("Clean start should be true");
            // TODO: what else do we need to check?
            result = "PASS";
        } catch (Exception e) {
            logger.info("Test failed " + e.getMessage());
            result = "FAIL " + e.getMessage();
        }
        testResults.put("message-flow-edge-node-birth-publish-connect", result);
    }

    @Test
    @SpecAssertion(
            section = Sections.OPERATIONAL_BEHAVIOR_EDGE_NODE_SESSION_ESTABLISHMENT,
            id = "message-flow-edge-node-birth-publish-subscribe")
    public void onClientSubscribe(final @NotNull String clientId, final @NotNull SubscribePacket packet) {
        logger.info("Edge node session establishment test - subscribe");

        if (myClientId.equals(clientId)) {
            String result = "FAIL";
            try {
                if (!state.equals("CONNECTED"))
                    throw new Exception("State should be connected");
                if (!packet.getSubscriptions().get(0).getTopicFilter().equals("STATE/" + host_application_id))
                    throw new Exception("Topic string wrong");
                // TODO: what else do we need to check?
                result = "PASS";
                state = "SUBSCRIBED";
            } catch (Exception e) {
                result = "FAIL " + e.getMessage();
            }
            testResults.put("message-flow-edge-node-birth-publish-subscribe", result);


            // A retained message should have been set on the STATE/host_application_id topic to indicate the
            // status of the primary host.  The edge node's behavior will vary depending on the result.

        }
    }

    @Test
    @SpecAssertion(
            section = Sections.OPERATIONAL_BEHAVIOR_EDGE_NODE_SESSION_ESTABLISHMENT,
            id = "primary-application-state-publish")
    public void onClientPublish(@NotNull String clientId, @NotNull PublishPacket packet) {
        logger.info("Primary host session establishment test - publish");

        if (myClientId.equals(clientId)) {
            String result = "FAIL";
            try {
                if (!state.equals("SUBSCRIBED"))
                    throw new Exception("State should be subscribed");

                String payload = null;
                ByteBuffer bpayload = packet.getPayload().orElseGet(null);
                if (bpayload != null) {
                    payload = StandardCharsets.UTF_8.decode(bpayload).toString();
                }
                if (!payload.equals("ONLINE"))
                    throw new Exception("Payload should be ONLINE");

                // TODO: what else do we need to check?
                result = "PASS";
                state = "PUBLISHED";
            } catch (Exception e) {
                result = "FAIL " + e.getMessage();
            }
            testResults.put("primary-application-state-publish", result);
        }

        theTCK.endTest();

    }

}