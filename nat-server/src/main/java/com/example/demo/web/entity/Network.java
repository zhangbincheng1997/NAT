package com.example.demo.web.entity;

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

    @Column(name = "host", nullable = false)
    private String host;          // 地址

    @Column(name = "port", nullable = false)
    private String port;        // 端口

    @Column(name = "flow", nullable = true)
    private String flow;      // 流量
}
