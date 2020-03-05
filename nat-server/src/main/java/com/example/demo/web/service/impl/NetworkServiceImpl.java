package com.example.demo.web.service.impl;

import com.example.demo.web.dao.NetworkRepository;
import com.example.demo.web.entity.Network;
import com.example.demo.web.service.NetworkService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.util.StringUtils;

public class NetworkServiceImpl implements NetworkService {

    @Autowired
    private NetworkRepository networkRepository;

    @Override
    public Page<Network> getList(int page, int size, String keyword) {
        Pageable pageable = PageRequest.of(page, size);
        if (!StringUtils.isEmpty(keyword)) {
            keyword = '%' + keyword + '%';
            return networkRepository.findPageByKeyword(keyword, pageable);
        } else {
            return networkRepository.findAll(pageable);
        }
    }

    @Override
    public void add(Network network) {
        networkRepository.save(network);
    }

    @Override
    public void delete(Long id) {
        networkRepository.deleteById(id);
    }
}
