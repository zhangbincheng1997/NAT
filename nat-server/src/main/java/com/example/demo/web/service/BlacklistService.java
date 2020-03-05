package com.example.demo.web.service;

import com.example.demo.web.entity.Blacklist;
import org.springframework.data.domain.Page;

public interface BlacklistService {

    boolean findByHost(String host);

    Page<Blacklist> getList(int page, int size, String keyword);

    void add(Blacklist blacklist);

    void delete(Long id);
}
