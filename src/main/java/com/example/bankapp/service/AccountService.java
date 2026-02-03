package com.example.bankapp.service;

import com.example.bankapp.model.Account;
import com.example.bankapp.model.Transaction;
import com.example.bankapp.repository.AccountRepository;
import com.example.bankapp.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Set;

@Service
public class AccountService implements UserDetailsService {

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    public Account findAccountByUsername(String username) {
        return accountRepository.findByUsername(username)
                .orElseThrow(() ->
                        new UsernameNotFoundException("Account not found: " + username)
                );
    }


    // ===== REGISTER =====
    public Account registerAccount(String username, String password) {
        if (accountRepository.findByUsername(username).isPresent()) {
            throw new RuntimeException("Username already exists");
        }

        Account account = new Account();
        account.setUsername(username);
        account.setPassword(passwordEncoder.encode(password));
        account.setBalance(BigDecimal.ZERO);

        return accountRepository.save(account);
    }

    // ===== DEPOSIT =====
    public void deposit(Account account, BigDecimal amount) {
        account.setBalance(account.getBalance().add(amount));
        accountRepository.save(account);

        transactionRepository.save(
                new Transaction(amount, "Deposit", LocalDateTime.now(), account)
        );
    }

    // ===== WITHDRAW =====
    public void withdraw(Account account, BigDecimal amount) {
        if (account.getBalance().compareTo(amount) < 0) {
            throw new RuntimeException("Insufficient funds");
        }

        account.setBalance(account.getBalance().subtract(amount));
        accountRepository.save(account);

        transactionRepository.save(
                new Transaction(amount, "Withdraw", LocalDateTime.now(), account)
        );
    }

    // ===== TRANSACTIONS =====
    public List<Transaction> getTransactionHistory(Account account) {
        return transactionRepository.findByAccountId(account.getId());
    }

    // ===== TRANSFER =====
    public void transferAmount(Account fromAccount, String toUsername, BigDecimal amount) {

        if (fromAccount.getBalance().compareTo(amount) < 0) {
            throw new RuntimeException("Insufficient funds");
        }

        Account toAccount = findAccountByUsername(toUsername);

        fromAccount.setBalance(fromAccount.getBalance().subtract(amount));
        toAccount.setBalance(toAccount.getBalance().add(amount));

        accountRepository.save(fromAccount);
        accountRepository.save(toAccount);

        transactionRepository.save(
                new Transaction(amount,
                        "Transfer Out to " + toAccount.getUsername(),
                        LocalDateTime.now(),
                        fromAccount)
        );

        transactionRepository.save(
                new Transaction(amount,
                        "Transfer In from " + fromAccount.getUsername(),
                        LocalDateTime.now(),
                        toAccount)
        );
    }

    // ===== SPRING SECURITY =====
    @Override
    public UserDetails loadUserByUsername(String username)
            throws UsernameNotFoundException {

        Account account = findAccountByUsername(username);

        return User.builder()
                .username(account.getUsername())
                .password(account.getPassword())
                .authorities(authorities())
                .build();
    }

    private Collection<? extends GrantedAuthority> authorities() {
        return Set.of(new SimpleGrantedAuthority("ROLE_USER"));
    }
}
