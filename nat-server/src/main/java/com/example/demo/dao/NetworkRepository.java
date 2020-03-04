package com.example.demo.dao;

import com.example.demo.entity.Network;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface NetworkRepository extends JpaRepository<Network, Long> {

    @Query(value = "SELECT n FROM Network n WHERE n.ip LIKE :keyword OR n.port LIKE :keyword OR n.remote LIKE :keyword")
    Page<Network> findPageByKeyword(String keyword, Pageable pageable);
}
