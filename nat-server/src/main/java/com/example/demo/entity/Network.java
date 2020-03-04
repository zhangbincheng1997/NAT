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

    @Column(name = "ip", nullable = false)
    private String ip;          // 内网地址

    @Column(name = "port", nullable = false)
    private String port;        // 内网端口

    @Column(name = "remote", nullable = false)
    private String remote;      // 公网端口

    @Column(name = "flow", nullable = false)
    private String flow;      // 流量
}
