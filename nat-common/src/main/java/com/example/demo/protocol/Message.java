package com.example.demo.protocol;

import lombok.Data;

@Data
public class Message {

    private MessageType type;
    private String channelId;
    private byte[] data;

    public Message(MessageType type, String channelId, byte[] data) {
        this.type = type;
        this.channelId = channelId;
        this.data = data;
    }

    public static Message of(MessageType type, String channelId, byte[] data) {
        return new Message(type, channelId, data);
    }

    public static Message of(MessageType type, String channelId) {
        return new Message(type, channelId, null);
    }

    public static Message of(MessageType type) {
        return new Message(type, null, null);
    }

}
