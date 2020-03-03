package com.example.demo.dao;

import com.example.demo.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface NetworkRepository extends JpaRepository<User, Long> {

    @Query(value = "SELECT n FROM Network n WHERE n.username LIKE :username")
    Page<User> findPageByUsername(String username, Pageable pageable);
}
