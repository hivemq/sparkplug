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
import com.hivemq.extension.sdk.api.interceptor.connect.ConnectInboundInterceptor;
import com.hivemq.extension.sdk.api.interceptor.connect.parameter.ConnectInboundInput;
import com.hivemq.extension.sdk.api.interceptor.connect.parameter.ConnectInboundOutput;
import com.hivemq.extension.sdk.api.packets.connect.ConnectPacket;
import org.eclipse.sparkplug.tck.test.TCK;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConnectInterceptor implements ConnectInboundInterceptor {

    private final static @NotNull Logger logger = LoggerFactory.getLogger("Sparkplug");

    private final @NotNull TCK theTCK;

    public ConnectInterceptor(final @NotNull TCK aTCK) {
        theTCK = aTCK;
    }

    @Override
    public void onConnect(final @NotNull ConnectInboundInput connectInboundInput,
                          final @NotNull ConnectInboundOutput connectInboundOutput) {
        try {
            final String clientId = connectInboundInput.getClientInformation().getClientId();

            logger.info("Inbound connect from '{}'", clientId);
            logger.info("\tInet Address {}", connectInboundInput.getConnectionInformation().getInetAddress());
            logger.info("\tMQTT Version {}", connectInboundInput.getConnectionInformation().getMqttVersion());
            logger.info("\tClean Start {}", connectInboundInput.getConnectPacket().getCleanStart());
            logger.info("\tKeep Alive {}", connectInboundInput.getConnectPacket().getKeepAlive());

            final ConnectPacket packet = connectInboundInput.getConnectPacket();
            theTCK.onClientConnect(clientId, packet);

        } catch (final Exception e) {
            logger.error("Exception", e);
        }
    }
}