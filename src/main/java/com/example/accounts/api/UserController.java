package com.example.accounts.api;

import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.PAYMENT_REQUIRED;

import com.example.accounts.service.UpgradeResult.NotFound;
import com.example.accounts.service.UpgradeResult.PaymentRequired;
import com.example.accounts.service.UpgradeResult.Suspended;
import com.example.accounts.service.UpgradeResult.Upgraded;
import com.example.accounts.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
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
    ResponseEntity<?> upgrade(@PathVariable long id, @Valid @RequestBody UpgradeRequest req) {
        return switch (service.upgrade(id, req.plan())) {
            case Upgraded(var user)      -> ResponseEntity.ok(UserDto.from(user));
            case NotFound(var uid)       -> problem(NOT_FOUND, "User %d not found".formatted(uid));
            case Suspended(var uid)      -> problem(CONFLICT, "User %d is suspended".formatted(uid));
            case PaymentRequired(var uid) -> problem(PAYMENT_REQUIRED, "No valid payment method");
        };
    }

    private static ResponseEntity<ProblemDetail> problem(HttpStatus status, String detail) {
        return ResponseEntity.status(status).body(ProblemDetail.forStatusAndDetail(status, detail));
    }
}
