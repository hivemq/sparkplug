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
 * This is the primary host Sparkplug send command test.
 *
 * There will be a prompt to the person executing the test to send a command to
 * a device and edge node we will connect.
 *
 */

import com.hivemq.extension.sdk.api.annotations.NotNull;
import com.hivemq.extension.sdk.api.annotations.Nullable;
import com.hivemq.extension.sdk.api.packets.connect.ConnectPacket;
import com.hivemq.extension.sdk.api.packets.general.Qos;
import com.hivemq.extension.sdk.api.packets.publish.PublishPacket;
import com.hivemq.extension.sdk.api.packets.subscribe.SubscribePacket;
import com.hivemq.extension.sdk.api.services.Services;
import com.hivemq.extension.sdk.api.services.builder.Builders;
import com.hivemq.extension.sdk.api.services.publish.Publish;
import com.hivemq.extension.sdk.api.services.publish.PublishService;
import org.eclipse.sparkplug.tck.test.TCK;
import org.eclipse.sparkplug.tck.test.TCKTest;
import org.jboss.test.audit.annotations.SpecVersion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;

@SpecVersion(
        spec = "sparkplug",
        version = "3.0.0-SNAPSHOT")
public class SendCommandTest extends TCKTest {

    private final static @NotNull Logger logger = LoggerFactory.getLogger("Sparkplug");

    private final @NotNull HashMap<String, String> testResults = new HashMap<>();
    private final @NotNull List<String> testIds = List.of(
            ""
    );

    private final @NotNull TCK theTCK;
    private final @NotNull PublishService publishService = Services.publishService();

    private final @Nullable String host_application_id;

    private @Nullable String myClientId;
    private @Nullable String state = null;

    public SendCommandTest(final @NotNull TCK aTCK, final @Nullable String @NotNull [] parms) {
        logger.info("Primary host send command test");
        theTCK = aTCK;

        for (final String testId : testIds) {
            testResults.put(testId, "");
        }

        host_application_id = parms[0];
        logger.info("Host application id is " + host_application_id);

        // First we have to connect an edge node and device      
        state = "ConnectingDevice";
        final String payload = "NEW DEVICE";
        final Publish message = Builders.publish()
                .topic("SPARKPLUG_TCK/DEVICE_CONTROL")
                .qos(Qos.AT_LEAST_ONCE)
                .payload(ByteBuffer.wrap(payload.getBytes()))
                .build();
        publishService.publish(message);

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
        return "SendCommandTest";
    }

    public @NotNull List<String> getTestIds() {
        return testIds;
    }

    public @NotNull HashMap<String, String> getResults() {
        return testResults;
    }

    @Override
    public void connect(final @NotNull String clientId, final @NotNull ConnectPacket packet) {

    }

    @Override
    public void subscribe(final @NotNull String clientId, final @NotNull SubscribePacket packet) {

    }

    @Override
    public void publish(final @NotNull String clientId, final @NotNull PublishPacket packet) {

        if (packet.getTopic().equals("SPARKPLUG_TCK/DEVICE_CONTROL")) {
            final String payload;
            if (packet.getPayload().isPresent()) {
                payload = StandardCharsets.UTF_8.decode(packet.getPayload().get()).toString();
            } else {
                payload = null;
            }

            if (payload.equals("DEVICE CONNECTED")) {

            }
        }

    }

}