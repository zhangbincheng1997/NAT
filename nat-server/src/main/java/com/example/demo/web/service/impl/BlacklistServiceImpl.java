package com.example.demo.web.service.impl;

import com.example.demo.web.dao.BlacklistRepository;
import com.example.demo.web.entity.Blacklist;
import com.example.demo.web.service.BlacklistService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.util.StringUtils;

public class BlacklistServiceImpl implements BlacklistService {

    @Autowired
    private BlacklistRepository blacklistRepository;

    @Override
    public boolean findByHost(String host) {
        return blacklistRepository.findByHost(host) != null;
    }

    @Override
    public Page<Blacklist> getList(int page, int size, String keyword) {
        Pageable pageable = PageRequest.of(page, size);
        if (!StringUtils.isEmpty(keyword)) {
            keyword = '%' + keyword + '%';
            return blacklistRepository.findPageByKeyword(keyword, pageable);
        } else {
            return blacklistRepository.findAll(pageable);
        }
    }

    @Override
    public void add(Blacklist blacklist) {
        blacklistRepository.save(blacklist);
    }

    @Override
    public void delete(Long id) {
        blacklistRepository.deleteById(id);
    }
}
