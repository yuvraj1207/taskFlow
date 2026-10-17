package com.taskflow.auth.config;

import com.taskflow.auth.entity.Role;
import com.taskflow.auth.entity.User;
import com.taskflow.auth.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

// Creates one admin account on first startup so you can test ADMIN access
@Component
public class AdminSeeder implements CommandLineRunner {

    private final UserRepository users;
    private final PasswordEncoder passwordEncoder;
    private final String adminPassword;

    public AdminSeeder(UserRepository users, PasswordEncoder passwordEncoder,
                       @Value("${app.admin.password}") String adminPassword) {
        this.users = users;
        this.passwordEncoder = passwordEncoder;
        this.adminPassword = adminPassword;
    }

    @Override
    public void run(String... args) {
        if (!users.existsByUsername("admin")) {
            users.save(new User("admin", passwordEncoder.encode(adminPassword), Role.ADMIN));
        }
    }
}