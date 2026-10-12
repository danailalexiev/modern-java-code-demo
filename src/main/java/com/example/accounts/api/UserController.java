package com.example.accounts.api;

import com.example.accounts.service.UserService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users")
class UserController {

    private final UserService service;

    UserController(UserService service) {
        this.service = service;
    }

    @GetMapping("/{id}")
    UserDto get(@PathVariable long id) {
        return UserDto.from(service.findById(id));
    }

    @PostMapping("/{id}/upgrade")
    UserDto upgrade(@PathVariable long id, @Valid @RequestBody UpgradeRequest req) {
        return UserDto.from(service.upgrade(id, req.plan()));
    }
}
