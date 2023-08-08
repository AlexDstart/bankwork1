package com.skypro.simplebanking.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.skypro.simplebanking.IntegrationTestBase;
import com.skypro.simplebanking.dto.BankingUserDetails;
import com.skypro.simplebanking.dto.TransferRequest;
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
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class TransferControllerTest extends IntegrationTestBase {

    private static final String TRANSFER_ENDPOINT = "/transfer/";

    @Autowired
    MockMvc mockMvc;
    @Autowired
    UserRepository userRepository;
    @Autowired
    AccountRepository accountRepository;
    @Autowired
    ObjectMapper objectMapper;

    @DisplayName("Перевод одного пользователя другому успешен")
    @Test
    @SneakyThrows
    void whenUserTransferAnotherUserIsSuccess() {
        Account firstUserAccount = new Account();
        Account secondUserAccount = new Account();
        User firstUser = new User("firstUser", "2236", new ArrayList<>());
        User secondUser = new User("secondUser", "2236", new ArrayList<>());
        userRepository.save(firstUser);
        userRepository.save(secondUser);

        firstUserAccount.setUser(firstUser);
        firstUserAccount.setAccountCurrency(AccountCurrency.RUB);
        firstUserAccount.setAmount(10000L);
        accountRepository.save(firstUserAccount);
        firstUser.setAccounts(List.of(firstUserAccount));

        secondUserAccount.setUser(secondUser);
        secondUserAccount.setAccountCurrency(AccountCurrency.RUB);
        secondUserAccount.setAmount(5000L);
        accountRepository.save(secondUserAccount);
        secondUser.setAccounts(List.of(secondUserAccount));

        TransferRequest transferRequest = new TransferRequest();
        transferRequest.setFromAccountId(firstUserAccount.getId());
        transferRequest.setToAccountId(secondUserAccount.getId());
        transferRequest.setToUserId(secondUser.getId());
        transferRequest.setAmount(2000L);

        BankingUserDetails userDetails = new BankingUserDetails(firstUser.getId(), "firstUser", "2236", false);

        String jsonTransferRequest = new ObjectMapper().writeValueAsString(transferRequest);

        mockMvc.perform(post(TRANSFER_ENDPOINT, transferRequest)
                        .with(SecurityMockMvcRequestPostProcessors.user(userDetails))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonTransferRequest))
                .andExpect(status().isOk());
    }

    @DisplayName("Перевод от имени администратора одного пользователя другому не успешен")
    @Test
    @SneakyThrows
    void whenAdminTransferAnotherUserIsNotSuccess() {
        Account firstUserAccount = new Account();
        Account secondUserAccount = new Account();
        User firstUser = new User("firstUser", "2236", new ArrayList<>());
        User secondUser = new User("secondUser", "2236", new ArrayList<>());
        userRepository.save(firstUser);
        userRepository.save(secondUser);

        firstUserAccount.setUser(firstUser);
        firstUserAccount.setAccountCurrency(AccountCurrency.RUB);
        firstUserAccount.setAmount(10000L);
        accountRepository.save(firstUserAccount);
        firstUser.setAccounts(List.of(firstUserAccount));

        secondUserAccount.setUser(secondUser);
        secondUserAccount.setAccountCurrency(AccountCurrency.RUB);
        secondUserAccount.setAmount(5000L);
        accountRepository.save(secondUserAccount);
        secondUser.setAccounts(List.of(secondUserAccount));

        TransferRequest transferRequest = new TransferRequest();
        transferRequest.setFromAccountId(firstUserAccount.getId());
        transferRequest.setToAccountId(secondUserAccount.getId());
        transferRequest.setToUserId(secondUser.getId());
        transferRequest.setAmount(2000L);

        BankingUserDetails userDetails = new BankingUserDetails(firstUser.getId(), "firstUser", "2236", true);

        String jsonTransferRequest = new ObjectMapper().writeValueAsString(transferRequest);

        mockMvc.perform(post(TRANSFER_ENDPOINT, transferRequest)
                        .with(SecurityMockMvcRequestPostProcessors.user(userDetails))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonTransferRequest))
                .andExpect(status().is4xxClientError());
    }
}
