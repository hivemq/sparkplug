/*
 * Copyright © 2021 The Eclipse Foundation, Cirrus Link Solutions, and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.sparkplug.tck;

import com.hivemq.extension.sdk.api.annotations.NotNull;
import com.hivemq.extension.sdk.api.interceptor.publish.PublishInboundInterceptor;
import com.hivemq.extension.sdk.api.interceptor.publish.parameter.PublishInboundInput;
import com.hivemq.extension.sdk.api.interceptor.publish.parameter.PublishInboundOutput;
import com.hivemq.extension.sdk.api.packets.publish.PublishPacket;
import org.eclipse.sparkplug.tck.test.TCK;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;

public class PublishInterceptor implements PublishInboundInterceptor {

    private final static @NotNull Logger logger = LoggerFactory.getLogger("Sparkplug");
    private final @NotNull TCK theTCK;

    public PublishInterceptor(final @NotNull TCK aTCK) {
        theTCK = aTCK;
    }

    @Override
    public void onInboundPublish(final @NotNull PublishInboundInput publishInboundInput,
                                 final @NotNull PublishInboundOutput publishInboundOutput) {
        try {
            final String clientId = publishInboundInput.getClientInformation().getClientId();
            logger.info("Inbound publish from '{}'", clientId);

            final PublishPacket packet = publishInboundInput.getPublishPacket();

            final String topic = packet.getTopic();
            logger.info("\tTopic {}", topic);

            final String payload;
            if (packet.getPayload().isPresent()) {
                payload = StandardCharsets.UTF_8.decode(packet.getPayload().get()).toString();
            } else {
                payload = "";
            }
            logger.info("\tPayload {}", payload);


            if (topic.equals("SPARKPLUG_TCK/TEST_CONTROL")) {
                String cmd = "NEW ";
                if (payload.startsWith(cmd)) {
                    final String[] strings = payload.split(" ");
                    if (strings.length < 3) {
                        throw new Exception("New test syntax is: NEW profile testname <parameters>");
                    }
                    int no_parms = strings.length - 3;
                    final String[] parms = new String[no_parms];
                    if (no_parms > 0) {
                        System.arraycopy(strings, 3, parms, 0, no_parms);
                    }
                    theTCK.newTest(strings[1], strings[2], parms);
                } else {
                    cmd = "END TEST";
                    if (payload.trim().equals(cmd)) {
                        theTCK.endTest();
                    }
                }
            } else
                theTCK.publish(clientId, packet);

        } catch (final Exception e) {
            logger.error("Exception", e);
        }
    }
}