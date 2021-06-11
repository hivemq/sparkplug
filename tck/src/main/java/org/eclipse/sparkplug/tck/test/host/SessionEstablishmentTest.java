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

package org.eclipse.sparkplug.tck.test.host;

/*
 * This is the primary host Sparkplug session establishment, and re-establishment test.
 *
 */

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
            "host-topic-phid-birth-payload",
            "host-topic-phid-death-payload",
            "host-topic-phid-death-qos",
            "host-topic-phid-death-retain",
            "primary-application-connect",
            "primary-application-death-cert",
            "primary-application-subscribe",
            "primary-application-state-publish",
            "components-ph-state"
    );

    private final @NotNull TCK theTCK;

    private final @Nullable String host_application_id;

    private @Nullable String myClientId = null;
    private @Nullable String state = null;

    public SessionEstablishmentTest(final @NotNull TCK aTCK, final @Nullable String @NotNull [] parms) {
        logger.info("Primary host session establishment test. Parameter: host_application_id");
        theTCK = aTCK;

        for (final String testId : testIds) {
            testResults.put(testId, "");
        }

        host_application_id = parms[0];
        logger.info("Host application id is " + host_application_id);
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

    @SpecAssertion(
            section = Sections.TOPICS_DEATH_MESSAGE_STATE,
            id = "host-topic-phid-death-payload")
    @SpecAssertion(
            section = Sections.TOPICS_DEATH_MESSAGE_STATE,
            id = "host-topic-phid-death-qos")
    @SpecAssertion(
            section = Sections.TOPICS_DEATH_MESSAGE_STATE,
            id = "host-topic-phid-death-retain")
    public @NotNull Optional<WillPublishPacket> checkWillMessage(final @NotNull ConnectPacket packet) {
        final Optional<WillPublishPacket> willPublishPacketOptional = packet.getWillPublish();

        if (willPublishPacketOptional.isPresent()) {
            final WillPublishPacket willPublishPacket = willPublishPacketOptional.get();

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
            section = Sections.OPERATIONAL_BEHAVIOR_PRIMARY_HOST_APPLICATION_SESSION_ESTABLISHMENT,
            id = "primary-application-connect")
    @SpecAssertion(
            section = Sections.OPERATIONAL_BEHAVIOR_PRIMARY_HOST_APPLICATION_SESSION_ESTABLISHMENT,
            id = "primary-application-death-cert")
    public void onClientConnect(final @NotNull String clientId, final @NotNull ConnectPacket packet) {
        logger.info("Primary host session establishment test - connect");

        String result = "FAIL";

        final Optional<WillPublishPacket> willPublishPacketOptional = checkWillMessage(packet);
        try {
            if (willPublishPacketOptional.isPresent()) {
                result = "PASS";
            }
            testResults.put("primary-application-death-cert", result);
        } catch (final Exception e) {
            logger.info("Exception", e);
        }

        try {
            if (willPublishPacketOptional.isEmpty()) {
                throw new Exception("Will message is needed");
            }
            if (!packet.getCleanStart()) {
                throw new Exception("Clean start should be true");
            }
            // TODO: what else do we need to check?
            result = "PASS";
            myClientId = clientId;
            state = "CONNECTED";
        } catch (final Exception e) {
            logger.info("Test failed " + e.getMessage());
            result = "FAIL " + e.getMessage();
        }
        testResults.put("primary-application-connect", result);
    }

    @Test
    @SpecAssertion(
            section = Sections.OPERATIONAL_BEHAVIOR_PRIMARY_HOST_APPLICATION_SESSION_ESTABLISHMENT,
            id = "primary-application-subscribe")
    public void onClientSubscribe(final @NotNull String clientId, final @NotNull SubscribePacket packet) {
        logger.info("Primary host session establishment test - subscribe");

        if (myClientId.equals(clientId)) {
            String result = "FAIL";
            try {
                if (!state.equals("CONNECTED"))
                    throw new Exception("State should be connected");
                if (!packet.getSubscriptions().get(0).getTopicFilter().equals("spAv1.0/#"))
                    throw new Exception("Topic string wrong");
                // TODO: what else do we need to check?
                result = "PASS";
                state = "SUBSCRIBED";
            } catch (Exception e) {
                result = "FAIL " + e.getMessage();
            }
            testResults.put("primary-application-subscribe", result);
        }
    }

    @Test
    @SpecAssertion(
            section = Sections.TOPICS_PRIMARY_HOST,
            id = "host-topic-phid-birth-payload")
    @SpecAssertion(
            section = Sections.OPERATIONAL_BEHAVIOR_PRIMARY_HOST_APPLICATION_SESSION_ESTABLISHMENT,
            id = "primary-application-state-publish")
    @SpecAssertion(
            section = Sections.COMPONENTS_PRIMARY_HOST_APPLICATION,
            id = "components-ph-state")
    public void onClientPublish(final @NotNull String clientId, final @NotNull PublishPacket packet) {
        logger.info("Primary host session establishment test - publish");

        if (myClientId.equals(clientId)) {
            String result = "FAIL";
            try {
                if (!state.equals("SUBSCRIBED"))
                    throw new Exception("State should be subscribed");

                String topic = packet.getTopic();
                if (!topic.equals("STATE/" + host_application_id))
                    throw new Exception("Topic should be STATE/host_application_id");

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
            } catch (final Exception e) {
                result = "FAIL " + e.getMessage();
            }
            testResults.put("primary-application-state-publish", result);
            testResults.put("host-topic-phid-birth-payload", result);
            testResults.put("components-ph-state", result);
        }

        // TODO: now we can disconnnect the client and allow it to reconnect and go throught the
        // session re-establishment phases.  It would be nice to be able to do this at after a 
        // short arbitrary interval, but I haven't worked out a good way of doing that yet (assuming
        // that a sleep here is not a good idea).  Using a PING interceptor could be one way but
        // we probably can't rely on any particular keepalive interval values.

        theTCK.endTest();
    }

}