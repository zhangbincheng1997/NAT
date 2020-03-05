package com.example.demo.web.service;

import com.example.demo.web.entity.Network;
import org.springframework.data.domain.Page;

public interface NetworkService {

    Page<Network> getList(int page, int size, String keyword);

    void add(Network network);

    void delete(Long id);
}
