package com.example.bankapp.config;

import com.example.bankapp.model.Account;
import com.example.bankapp.repository.AccountRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class AdminAccountInitializer implements CommandLineRunner {

    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminAccountInitializer(AccountRepository accountRepository, PasswordEncoder passwordEncoder) {
        this.accountRepository = accountRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        String username = "ADMIN";
        accountRepository.findByUsername(username).ifPresentOrElse(existing -> {
            if (existing.getRole() == null || !existing.getRole().equals("ROLE_ADMIN")) {
                existing.setRole("ROLE_ADMIN");
                accountRepository.save(existing);
            }
        }, () -> {
            Account admin = new Account();
            admin.setUsername(username);
            admin.setPassword(passwordEncoder.encode("0000"));
            admin.setBalance(BigDecimal.ZERO);
            admin.setRole("ROLE_ADMIN");
            accountRepository.save(admin);
        });
    }
}
