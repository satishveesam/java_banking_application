package com.example.bankapp.controller;

import com.example.bankapp.model.Account;
import com.example.bankapp.model.Transaction;
import com.example.bankapp.repository.AccountRepository;
import com.example.bankapp.repository.TransactionRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;

    public AdminController(AccountRepository accountRepository, TransactionRepository transactionRepository) {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
    }

    @GetMapping
    public String index() {
        return "redirect:/admin/accounts";
    }

    @GetMapping("/accounts")
    public String accounts(Model model) {
        List<Account> accounts = accountRepository.findAll();
        model.addAttribute("accounts", accounts);
        return "admin-accounts";
    }

    @GetMapping("/transactions")
    public String transactions(Model model) {
        List<Transaction> transactions = transactionRepository.findAllWithAccount();
        List<TransactionView> views = transactions.stream()
                .map(tx -> new TransactionView(
                        tx.getId(),
                        tx.getType(),
                        tx.getAmount(),
                        tx.getTransactionDate(),
                        tx.getAccount() != null ? tx.getAccount().getUsername() : "N/A"
                ))
                .collect(Collectors.toList());
        model.addAttribute("transactions", views);
        return "admin-transactions";
    }

    public static class TransactionView {
        private final Long id;
        private final String type;
        private final BigDecimal amount;
        private final LocalDateTime transactionDate;
        private final String accountUsername;

        public TransactionView(Long id, String type, BigDecimal amount, LocalDateTime transactionDate, String accountUsername) {
            this.id = id;
            this.type = type;
            this.amount = amount;
            this.transactionDate = transactionDate;
            this.accountUsername = accountUsername;
        }

        public Long getId() {
            return id;
        }

        public String getType() {
            return type;
        }

        public BigDecimal getAmount() {
            return amount;
        }

        public LocalDateTime getTransactionDate() {
            return transactionDate;
        }

        public String getAccountUsername() {
            return accountUsername;
        }
    }
}
