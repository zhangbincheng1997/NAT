package com.example.demo.protocol;

public enum MessageType {

    UNKNOWN(0),
    REGISTER(1),
    REGISTER_SUCCESS(2),
    REGISTER_FAILURE(3),
    CONNECTED(4),
    DATA(5),
    DISCONNECTED(6),
    KEEPALIVE(7);

    private int code;

    MessageType(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public static MessageType valueOf(int code) {
        for (MessageType item : MessageType.values()) {
            if (item.code == code) {
                return item;
            }
        }
        return UNKNOWN;
    }
}
