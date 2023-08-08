package com.skypro.simplebanking.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.skypro.simplebanking.IntegrationTestBase;
import com.skypro.simplebanking.dto.BalanceChangeRequest;
import com.skypro.simplebanking.dto.BankingUserDetails;
import com.skypro.simplebanking.entity.Account;
import com.skypro.simplebanking.entity.AccountCurrency;
import com.skypro.simplebanking.entity.User;
import com.skypro.simplebanking.repository.AccountRepository;
import com.skypro.simplebanking.repository.UserRepository;
import lombok.SneakyThrows;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AccountControllerTest extends IntegrationTestBase {

    private static final String ACCOUNT_ENDPOINT = "/account/";
    private static final String PATH_DEPOSIT_TO_ACCOUNT = "deposit/{id}";
    private static final String PATH_WITHDRAW_FROM_ACCOUNT = "withdraw/{id}";

    @Autowired
    MockMvc mockMvc;
    @Autowired
    AccountRepository accountRepository;
    @Autowired
    UserRepository userRepository;
    @Autowired
    ObjectMapper objectMapper;

    @DisplayName("Пользователь получает свой аккаунт успешно")
    @Test
    @SneakyThrows
    void whenUserGetUserAccountIsSuccess() {
        User user = new User("test_user", "2236", new ArrayList<>());
        userRepository.save(user);

        Account account = new Account();
        account.setAccountCurrency(AccountCurrency.RUB);
        account.setAmount(10000L);
        account.setUser(user);
        accountRepository.save(account);

        BankingUserDetails userDetails = new BankingUserDetails(user.getId(), "test_user", "2236", false);

        mockMvc.perform(get(ACCOUNT_ENDPOINT + "{id}", account.getId())
                        .with(SecurityMockMvcRequestPostProcessors.user(userDetails))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(account.getId()))
                .andExpect(jsonPath("$.amount").value(account.getAmount()))
                .andExpect(jsonPath("$.currency").value("RUB"));
    }

    @DisplayName("Администратор получает свой аккаунт не успешно")
    @Test
    @SneakyThrows
    void whenAdminGetUserAccountIsNotSuccess() {
        User user = new User("test_admin", "2236", new ArrayList<>());
        userRepository.save(user);

        Account account = new Account();
        account.setAccountCurrency(AccountCurrency.RUB);
        account.setAmount(10000L);
        account.setUser(user);
        accountRepository.save(account);

        BankingUserDetails userDetails = new BankingUserDetails(user.getId(), "test_admin", "2236", true);

        mockMvc.perform(get(ACCOUNT_ENDPOINT + "{id}", account.getId())
                        .with(SecurityMockMvcRequestPostProcessors.user(userDetails))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError());
    }

    @DisplayName("Зачисление пользователем на счет пользователя успешно")
    @Test
    @SneakyThrows
    void whenUserDepositToAccountIsSuccess() {
        User user = new User("test_user", "2236", new ArrayList<>());
        userRepository.save(user);

        Account account = new Account();
        account.setAccountCurrency(AccountCurrency.RUB);
        account.setAmount(10000L);
        account.setUser(user);
        accountRepository.save(account);

        BalanceChangeRequest balanceChangeRequest = new BalanceChangeRequest();
        balanceChangeRequest.setAmount(5000L);

        String jsonBalanceChangeRequest = new ObjectMapper().writeValueAsString(balanceChangeRequest);
        BankingUserDetails userDetails = new BankingUserDetails(user.getId(), "test_user", "2236", false);

        mockMvc.perform(post(ACCOUNT_ENDPOINT + PATH_DEPOSIT_TO_ACCOUNT, account.getId())
                        .with(SecurityMockMvcRequestPostProcessors.user(userDetails))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBalanceChangeRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(account.getId()))
                .andExpect(jsonPath("$.amount").value(15000L))
                .andExpect(jsonPath("$.currency").value("RUB"));
    }

    @DisplayName("Зачисление администратором на счет пользователя успешно")
    @Test
    @SneakyThrows
    void whenAdminDepositToAccountIsNotSuccess() {
        User user = new User("test_admin", "2236", new ArrayList<>());
        userRepository.save(user);

        Account account = new Account();
        account.setAccountCurrency(AccountCurrency.RUB);
        account.setAmount(10000L);
        account.setUser(user);
        accountRepository.save(account);

        BalanceChangeRequest balanceChangeRequest = new BalanceChangeRequest();
        balanceChangeRequest.setAmount(5000L);

        String jsonBalanceChangeRequest = new ObjectMapper().writeValueAsString(balanceChangeRequest);
        BankingUserDetails userDetails = new BankingUserDetails(user.getId(), "test_admin", "2236", true);

        mockMvc.perform(post(ACCOUNT_ENDPOINT + PATH_DEPOSIT_TO_ACCOUNT, account.getId())
                        .with(SecurityMockMvcRequestPostProcessors.user(userDetails))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBalanceChangeRequest))
                .andExpect(status().is4xxClientError());
    }

    @DisplayName("Снятие пользователем средств со счета пользователя успешно")
    @Test
    @SneakyThrows
    void whenUserWithdrawFromAccountIsSuccess() {
        User user = new User("test_user", "2236", new ArrayList<>());
        userRepository.save(user);

        Account account = new Account();
        account.setAccountCurrency(AccountCurrency.RUB);
        account.setAmount(10000L);
        account.setUser(user);
        accountRepository.save(account);

        BalanceChangeRequest balanceChangeRequest = new BalanceChangeRequest();
        balanceChangeRequest.setAmount(5000L);

        String jsonBalanceChangeRequest = new ObjectMapper().writeValueAsString(balanceChangeRequest);
        BankingUserDetails userDetails = new BankingUserDetails(user.getId(), "test_user", "2236", false);

        mockMvc.perform(post(ACCOUNT_ENDPOINT + PATH_WITHDRAW_FROM_ACCOUNT, account.getId())
                        .with(SecurityMockMvcRequestPostProcessors.user(userDetails))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBalanceChangeRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(account.getId()))
                .andExpect(jsonPath("$.amount").value(5000L))
                .andExpect(jsonPath("$.currency").value("RUB"));
    }

    @DisplayName("Снятие администратором средств со счета пользователя не успешно")
    @Test
    @SneakyThrows
    void whenAdminWithdrawFromAccountIsNotSuccess() {
        User user = new User("test_admin", "2236", new ArrayList<>());
        userRepository.save(user);

        Account account = new Account();
        account.setAccountCurrency(AccountCurrency.RUB);
        account.setAmount(10000L);
        account.setUser(user);
        accountRepository.save(account);

        BalanceChangeRequest balanceChangeRequest = new BalanceChangeRequest();
        balanceChangeRequest.setAmount(5000L);

        String jsonBalanceChangeRequest = new ObjectMapper().writeValueAsString(balanceChangeRequest);
        BankingUserDetails userDetails = new BankingUserDetails(user.getId(), "test_admin", "2236", true);

        mockMvc.perform(post(ACCOUNT_ENDPOINT + PATH_WITHDRAW_FROM_ACCOUNT, account.getId())
                        .with(SecurityMockMvcRequestPostProcessors.user(userDetails))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBalanceChangeRequest))
                .andExpect(status().is4xxClientError());
    }

}
