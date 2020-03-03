package com.example.demo.service;

import com.example.demo.entity.User;

public interface UserService {

    void register(String username, String password);

    void login(String username, String password);

    void updatePwd(String oldPwd, String newPwd);
}
