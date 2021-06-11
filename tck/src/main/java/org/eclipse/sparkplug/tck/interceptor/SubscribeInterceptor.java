/*
 * Copyright © 2021 The Eclipse Foundation, Cirrus Link Solutions, and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.sparkplug.tck.interceptor;

import com.hivemq.extension.sdk.api.annotations.NotNull;
import com.hivemq.extension.sdk.api.interceptor.subscribe.SubscribeInboundInterceptor;
import com.hivemq.extension.sdk.api.interceptor.subscribe.parameter.SubscribeInboundInput;
import com.hivemq.extension.sdk.api.interceptor.subscribe.parameter.SubscribeInboundOutput;
import com.hivemq.extension.sdk.api.packets.subscribe.SubscribePacket;
import org.eclipse.sparkplug.tck.test.TCK;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SubscribeInterceptor implements SubscribeInboundInterceptor {

    private final static @NotNull Logger logger = LoggerFactory.getLogger("Sparkplug");

    private final @NotNull TCK theTCK;

    public SubscribeInterceptor(final @NotNull TCK aTCK) {
        theTCK = aTCK;
    }

    @Override
    public void onInboundSubscribe(final @NotNull SubscribeInboundInput subscribeInboundInput,
                                   final @NotNull SubscribeInboundOutput subscribeInboundOutput) {
        try {
            final String clientId = subscribeInboundInput.getClientInformation().getClientId();
            logger.info("Inbound subscribe from '{}'", clientId);

            final SubscribePacket packet = subscribeInboundInput.getSubscribePacket();
            logger.info("\tTopic {}", packet.getSubscriptions().get(0).getTopicFilter());

            theTCK.onClientSubscribe(clientId, packet);

        } catch (final Exception e) {
            logger.error("Exception", e);
        }
    }
}