package com.taskflow.auth.controller;

import com.taskflow.auth.dto.UserSummary;
import com.taskflow.auth.repository.UserRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final UserRepository users;

    public AdminController(UserRepository users) {
        this.users = users;
    }

    // GET /api/admin/users: only ADMIN. A USER token gets 403.
    @GetMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    public List<UserSummary> listUsers() {
        return users.findAll().stream()
                .map(u -> new UserSummary(u.getId(), u.getUsername(), u.getRole().name()))
                .toList();
    }
}