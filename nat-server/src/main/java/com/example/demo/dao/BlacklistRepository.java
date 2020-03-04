package com.example.demo.dao;

import com.example.demo.entity.Blacklist;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface BlacklistRepository extends JpaRepository<Blacklist, Long> {

    @Query(value = "SELECT n FROM Network n WHERE n.ip LIKE :keyword")
    Page<Blacklist> findPageByKeyword(String keyword, Pageable pageable);
}
