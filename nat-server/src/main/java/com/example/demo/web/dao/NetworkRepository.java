package com.example.demo.web.dao;

import com.example.demo.web.entity.Network;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface NetworkRepository extends JpaRepository<Network, Long> {

    @Query(value = "SELECT n FROM Network n WHERE n.host LIKE :keyword OR n.port LIKE :keyword")
    Page<Network> findPageByKeyword(String keyword, Pageable pageable);
}
