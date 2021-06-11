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

/*
 * This is the edge node Sparkplug send data test.  Data can be sent from edge
 * nodes and devices.
 *
 *
 */

import com.hivemq.extension.sdk.api.annotations.NotNull;
import com.hivemq.extension.sdk.api.annotations.Nullable;
import com.hivemq.extension.sdk.api.packets.connect.ConnectPacket;
import com.hivemq.extension.sdk.api.packets.publish.PublishPacket;
import com.hivemq.extension.sdk.api.packets.subscribe.SubscribePacket;
import org.eclipse.sparkplug.tck.test.TCK;
import org.eclipse.sparkplug.tck.test.TCKTest;
import org.jboss.test.audit.annotations.SpecVersion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SpecVersion(
        spec = "sparkplug",
        version = "3.0.0-SNAPSHOT")
public class ReceiveCommandTest extends TCKTest {

    private final static @NotNull Logger logger = LoggerFactory.getLogger("Sparkplug");

    private final @NotNull Map<String, String> testResults = new HashMap<>();
    private final @NotNull List<String> testIds = List.of(
            ""
    );

    private final @NotNull TCK theTCK;

    private final @Nullable String host_application_id;

    private @Nullable String myClientId = null;
    private @Nullable String state = null;

    public ReceiveCommandTest(final @NotNull TCK aTCK, final @Nullable String @NotNull [] parms) {
        logger.info(getName());
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
        return "Sparkplug Edge Node Send Data Test";
    }

    public @NotNull List<String> getTestIds() {
        return testIds;
    }

    public @NotNull Map<String, String> getResults() {
        return testResults;
    }

    @Override
    public void connect(@NotNull String clientId, @NotNull ConnectPacket packet) {
        // TODO Auto-generated method stub

    }

    @Override
    public void subscribe(@NotNull String clientId, @NotNull SubscribePacket packet) {
        // TODO Auto-generated method stub

    }

    @Override
    public void publish(@NotNull String clientId, @NotNull PublishPacket packet) {
        // TODO Auto-generated method stub

    }

}