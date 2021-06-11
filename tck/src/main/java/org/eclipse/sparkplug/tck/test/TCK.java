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
import com.hivemq.extension.sdk.api.annotations.Nullable;
import com.hivemq.extension.sdk.api.packets.connect.ConnectPacket;
import com.hivemq.extension.sdk.api.packets.publish.PublishPacket;
import com.hivemq.extension.sdk.api.packets.subscribe.SubscribePacket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;

/**
 * Main class of TCK.
 * Responsible for creating tests, ending tests and sending the required information to the test classes.
 */
public class TCK {

    private final static @NotNull Logger logger = LoggerFactory.getLogger("Sparkplug");

    private @Nullable BaseTCKTest currentTest = null;

    /**
     * Creates a new test.
     *
     * @param profile Either host or edge
     * @param test    Name of the test class
     * @param parms
     */
    public void newTest(final @NotNull String profile,
                        final @NotNull String test,
                        final @NotNull String @NotNull [] parms) {
        logger.info("Test requested " + profile + " " + test);

        try {
            final String className = "org.eclipse.sparkplug.tck.test." + profile + "." + test + "Test";
            final Class<? extends BaseTCKTest> testClass = Class.forName(className).asSubclass(BaseTCKTest.class);
            final Class<?>[] constructorTypes = {this.getClass(), String[].class};
            final Constructor<? extends BaseTCKTest> constructor = testClass.getConstructor(constructorTypes);

            final Object[] parameters = {this, parms};
            currentTest = constructor.newInstance(parameters);

        } catch (final Exception e) {
            logger.error("Could not find or set test class " + profile + "." + test, e);
        }
    }

    /**
     * Request to end a test.
     */
    public void endTest() {
        if (currentTest != null) {
            logger.info("Test end requested for " + currentTest.getName());
            currentTest.endTest();
            currentTest = null;
        } else {
            logger.info("Test end requested but no test active");
        }

    }

    /**
     * A client (host or edge) connects to the TCK server.
     *
     * @param clientId The MQTT client identifier
     * @param packet   The MQTT connect packet
     */
    public void onClientConnect(final @NotNull String clientId, final @NotNull ConnectPacket packet) {
        if (currentTest != null) {
            currentTest.onClientConnect(clientId, packet);
        }
    }

    /**
     * A client (host or edge) subscribes to a topic of the TCK server.
     *
     * @param clientId The MQTT client identifier
     * @param packet   The MQTT subscribe packet
     */
    public void onClientSubscribe(final @NotNull String clientId, final @NotNull SubscribePacket packet) {
        if (currentTest != null) {
            currentTest.onClientSubscribe(clientId, packet);
        }
    }

    /**
     * A client (host or edge) publishes a message to a topic of the TCK server.
     *
     * @param clientId The MQTT client identifier
     * @param packet   The MQTT publish packet
     */
    public void onClientPublish(final @NotNull String clientId, final @NotNull PublishPacket packet) {
        if (currentTest != null) {
            currentTest.onClientPublish(clientId, packet);
        }
    }

}