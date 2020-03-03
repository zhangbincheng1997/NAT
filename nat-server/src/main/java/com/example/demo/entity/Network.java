package com.example.demo.entity;

import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;

@Data
@Entity
@Table(name = "network")
public class Network implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name = "username", nullable = false)
    private String username;    // 用户名

    @Column(name = "ip", nullable = false)
    private String ip;          // 内网地址

    @Column(name = "port", nullable = false)
    private String port;        // 内网端口

    @Column(name = "public", nullable = false)
    private String remote;      // 公网端口

    @Column(name = "content", nullable = false)
    private String content;     // 备注

    @Column(name = "number", nullable = false)
    private String number;      // 连接数

    @Column(name = "enable", nullable = false)
    private String enable;      // 正常/禁止

    @Column(name = "stop", nullable = false)
    private String stop;        // 正常/停止

    @Column(name = "secret", nullable = false)
    private String secret;      // 密钥
}
