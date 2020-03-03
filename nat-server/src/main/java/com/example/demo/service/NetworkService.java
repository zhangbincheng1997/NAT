package com.example.demo.service;

import com.example.demo.entity.Network;
import org.springframework.data.domain.Page;

public interface NetworkService {

    Page<Network> getList();

    void add();

    void delete(Long id);
}
