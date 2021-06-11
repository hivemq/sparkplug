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

package org.eclipse.sparkplug.tck.utility;

/*
 * This is a utility to connect an MQTT client to a broker.
 *
 * There will be a prompt to the person executing the test to send a command to
 * a device and edge node we will connect.
 *
 */

import com.hivemq.extension.sdk.api.annotations.NotNull;
import com.hivemq.extension.sdk.api.annotations.Nullable;
import org.eclipse.paho.client.mqttv3.*;
import org.jboss.test.audit.annotations.SpecVersion;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@SpecVersion(
        spec = "sparkplug",
        version = "3.0.0-SNAPSHOT")
public class DeviceConnect {

    private @Nullable String state = null;

    private final @NotNull String brokerURI = "tcp://localhost:1883";
    private final @NotNull String log_topic = "SPARKPLUG_TCK/LOG";

    private String controlId = "Sparkplug TCK device utility";
    private MqttClient control = null;
    private MqttTopic control_topic = null;
    private MessageListener control_listener = null;

    private MqttClient edge = null;
    private MqttTopic edge_topic = null;
    private MessageListener edge_listener = null;

    public void log(final @NotNull String message) {
        try {
            MqttMessage mqttmessage = new MqttMessage(message.getBytes());
            control_topic.publish(mqttmessage);
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(final @NotNull String... args) {
        new DeviceConnect().run(args);
    }

    public void run(final @NotNull String... args) {
        try {
            control = new MqttClient(brokerURI, controlId);
            control_listener = new MessageListener();
            control.setCallback(control_listener);
            control_topic = control.getTopic(log_topic);
            control.connect();
            log("Sparkplug device utility starting");
            control.subscribe("SPARKPLUG_TCK/DEVICE_CONTROL");

            while (true) {
                MqttMessage msg = control_listener.getNextMessage();

                if (msg != null && msg.toString().equals("NEW DEVICE")) {
                    log("NEW DEVICE");
                    deviceCreate("hostid");
                }
                Thread.sleep(100);
            }

        } catch (final Exception e) {
            e.printStackTrace();
        }
    }

    public void deviceCreate(final @NotNull String host_application_id) throws MqttException, InterruptedException {
        edge = new MqttClient(brokerURI, "Sparkplug TCK edge node 1");
        edge_listener = new MessageListener();
        edge.setCallback(edge_listener);
        String host_topic = edge.getTopic("STATE/#").toString();

        edge.connect();

        edge.subscribe("STATE/" + host_application_id); /* look for status of the host application we are to use */

        /* wait for retained message indicating state of host application under test */
        int count = 0;
        while (true) {
            final MqttMessage msg = edge_listener.getNextMessage();

            if (msg != null) {
                if (msg.toString().equals("ONLINE")) {
                    break;
                } else {
                    log("Error: host application not online");
                    return;
                }
            }
            Thread.sleep(100);
            if (count >= 5) {
                log("Error: no host application state");
                return;
            }
            count++;
        }

        // subscribe to NCMD topic
        final String namespace = "spBv1.0";
        final String group_id = "test_group";
        final String edge_node_id = "test_id";
        edge.subscribe(namespace + "/" + group_id + "/NCMD/" + edge_node_id);

        // issue NBIRTH for the edge node
        String payload = "";
        MqttMessage mqttmessage = new MqttMessage(payload.getBytes());
        edge_topic.publish(mqttmessage);


    }

    class MessageListener implements MqttCallback {

        final @NotNull List<MqttMessage> messages = Collections.synchronizedList(new ArrayList<>());

        public MessageListener() {
        }

        public @Nullable MqttMessage getNextMessage() {
            synchronized (messages) {
                if (messages.size() == 0) {
                    try {
                        messages.wait(1000);
                    } catch (final InterruptedException e) {
                        // empty
                    }
                }

                if (messages.size() == 0) {
                    return null;
                }
                return messages.remove(0);
            }
        }

        public void connectionLost(final @NotNull Throwable cause) {
            log("connection lost: " + cause.getMessage());
        }

        public void deliveryComplete(final @NotNull IMqttDeliveryToken token) {

        }

        public void messageArrived(final @NotNull String topic, final @NotNull MqttMessage message) throws Exception {
            log("message arrived: '" + new String(message.getPayload()) + "'");

            synchronized (messages) {
                messages.add(message);
                messages.notifyAll();
            }
        }
    }

}
