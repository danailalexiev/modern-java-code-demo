package com.example.accounts.api;

import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.PAYMENT_REQUIRED;

import com.example.accounts.domain.UserId;
import com.example.accounts.service.AccountService;
import com.example.accounts.service.Result.Err;
import com.example.accounts.service.Result.Ok;
import com.example.accounts.service.UpgradeError.AccountSuspended;
import com.example.accounts.service.UpgradeError.NotFound;
import com.example.accounts.service.UpgradeError.NotVerified;
import com.example.accounts.service.UpgradeError.PaymentRequired;
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
class AccountController {

    private final AccountService service;

    AccountController(AccountService service) {
        this.service = service;
    }

    @GetMapping("/{id}")
    ResponseEntity<?> get(@PathVariable long id) {
        return service.findById(new UserId(id))
            .<ResponseEntity<?>>map(account -> ResponseEntity.ok(AccountDto.from(account)))
            .orElseGet(() -> problem(NOT_FOUND, "User %d not found".formatted(id)));
    }

    @PostMapping("/{id}/upgrade")
    ResponseEntity<?> upgrade(@PathVariable long id, @Valid @RequestBody UpgradeRequest req) {
        return switch (service.upgrade(new UserId(id), req.plan())) {
            case Ok(var account)                     -> ResponseEntity.ok(AccountDto.from(account));
            case Err(NotFound(var uid))               -> problem(NOT_FOUND, "No user " + uid);
            case Err(AccountSuspended(_, var since))  -> problem(CONFLICT, "Suspended since " + since);
            case Err(NotVerified _)                   -> problem(FORBIDDEN, "Email not verified");
            case Err(PaymentRequired _)               -> problem(PAYMENT_REQUIRED, "No valid payment method");
        };
    }

    private static ResponseEntity<ProblemDetail> problem(HttpStatus status, String detail) {
        return ResponseEntity.status(status).body(ProblemDetail.forStatusAndDetail(status, detail));
    }
}
