package com.example.demo.protocol;

import lombok.Data;

@Data
public class Message {

    private MessageType type;
    private String channelId;
    private byte[] data;

}
