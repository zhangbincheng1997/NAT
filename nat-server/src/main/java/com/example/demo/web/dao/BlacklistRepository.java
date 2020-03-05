package com.example.demo.web.dao;

import com.example.demo.web.entity.Blacklist;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface BlacklistRepository extends JpaRepository<Blacklist, Long> {

    Blacklist findByHost(String host);

    @Query(value = "SELECT n FROM Network n WHERE n.host LIKE :keyword")
    Page<Blacklist> findPageByKeyword(String keyword, Pageable pageable);
}
