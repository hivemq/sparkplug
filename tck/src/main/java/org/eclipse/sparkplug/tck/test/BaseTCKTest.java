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

package org.eclipse.sparkplug.tck.test;

import com.hivemq.extension.sdk.api.annotations.NotNull;
import com.hivemq.extension.sdk.api.packets.connect.ConnectPacket;
import com.hivemq.extension.sdk.api.packets.general.Qos;
import com.hivemq.extension.sdk.api.packets.publish.PublishPacket;
import com.hivemq.extension.sdk.api.packets.subscribe.SubscribePacket;
import com.hivemq.extension.sdk.api.services.Services;
import com.hivemq.extension.sdk.api.services.builder.Builders;
import com.hivemq.extension.sdk.api.services.publish.Publish;
import com.hivemq.extension.sdk.api.services.publish.PublishService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class BaseTCKTest {

    private final static @NotNull Logger logger = LoggerFactory.getLogger("Sparkplug");

    public abstract void onClientConnect(final @NotNull String clientId, final @NotNull ConnectPacket packet);

    public abstract void onClientSubscribe(final @NotNull String clientId, final @NotNull SubscribePacket packet);

    public abstract void onClientPublish(final @NotNull String clientId, final @NotNull PublishPacket packet);

    public abstract void endTest();

    public abstract @NotNull String getName();

    public abstract @NotNull List<String> getTestIds();

    public abstract @NotNull Map<String, String> getResults();


    public void reportResults(final @NotNull Map<String, String> results) {
        final StringBuilder payload = new StringBuilder();

        String overall = "PASS";

        for (final String key : results.keySet()) {
            final String result = results.get(key);

            payload.append(key);
            payload.append(": ");
            payload.append(result);
            payload.append("; ");

            if (!result.equals("PASS")) {
                overall = "FAIL";
            }
        }

        payload.append("OVERALL: ");
        payload.append(overall);
        payload.append("; ");

        logger.info("Test results " + payload);

        final PublishService publishService = Services.publishService();

        final Publish message = Builders.publish()
                .topic("SPARKPLUG_TCK/RESULT")
                .qos(Qos.AT_LEAST_ONCE)
                .payload(ByteBuffer.wrap(payload.toString().getBytes()))
                .build();
        publishService.publish(message);
    }

}