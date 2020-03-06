package com.example.demo.protocol;

import lombok.Data;

import java.util.Map;

@Data
public class Message {

    private MessageType type;
//    private Map<String, Object> metaData;
    private String channelId;
    private byte[] data;

}
