package org.eclipse.sparkplug.tck.message;

import com.google.protobuf.InvalidProtocolBufferException;
import com.hivemq.extension.sdk.api.annotations.NotNull;
import org.eclipse.sparkplug.tck.message.payload.NBIRTHTest;
import org.eclipse.sparkplug.tck.protobuf.SparkplugBProto;
import org.eclipse.sparkplug.tck.message.exception.InvalidPayloadException;
import org.eclipse.sparkplug.tck.message.exception.UnknownMessageTypeException;

public interface MessagePayload {

    @NotNull TestResult testMessage(final @NotNull SparkplugBProto.Payload payload);



    static @NotNull TestResult testMessageWithType(final @NotNull MessageType messageType,
                                                   final @NotNull SparkplugBProto.Payload payload) {
        switch (messageType) {
            case NBIRTH:
                return new NBIRTHTest().testMessage(payload);
            default:
                throw new UnknownMessageTypeException("Unknown message type.");
        }
    }

    static @NotNull TestResult testMessageWithType(final @NotNull MessageType messageType,
                                                   final byte @NotNull [] payload) {
        try {
            final SparkplugBProto.Payload protobufPayload = SparkplugBProto.Payload.parseFrom(payload);

            switch (messageType) {
                case NBIRTH:
                    return new NBIRTHTest().testMessage(protobufPayload);
                default:
                    throw new UnknownMessageTypeException("Unknown message type.");
            }
        } catch (final InvalidProtocolBufferException e) {
            throw new InvalidPayloadException(e);
        }
    }
}
