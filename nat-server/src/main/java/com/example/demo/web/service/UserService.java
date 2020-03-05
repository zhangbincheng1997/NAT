package com.example.demo.web.service;

public interface UserService {

    void register(String username, String password);

    void login(String username, String password);

    void updatePwd(String oldPwd, String newPwd);
}
