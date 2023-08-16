package com.skypro.simplebanking.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.skypro.simplebanking.IntegrationTestBase;
import com.skypro.simplebanking.dto.TransferRequest;
import com.skypro.simplebanking.entity.Account;
import com.skypro.simplebanking.entity.AccountCurrency;
import com.skypro.simplebanking.entity.User;
import com.skypro.simplebanking.repository.AccountRepository;
import com.skypro.simplebanking.repository.UserRepository;
import lombok.SneakyThrows;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.util.Base64Utils;

import java.util.ArrayList;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.assertj.core.api.Assertions.*;

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

    @AfterEach
    void clearAll() {
        userRepository.deleteAll();
        accountRepository.deleteAll();
    }

    @DisplayName("Перевод одного пользователя другому успешен")
    @Test
    @SneakyThrows
    void whenUserTransferAnotherUserIsSuccess() {
        String userCredentials = "firstUser:2236";
        Long firstExpectedBalance = 8000L;
        Long secondExpectedBalance = 7000L;
        Account firstUserAccount = new Account();
        Account secondUserAccount = new Account();
        User firstUser = new User("firstUser", HASHED_PASSWORD, new ArrayList<>());
        User secondUser = new User("secondUser", HASHED_PASSWORD, new ArrayList<>());
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

        String jsonTransferRequest = objectMapper.writeValueAsString(transferRequest);

        mockMvc.perform(post(TRANSFER_ENDPOINT, transferRequest)
                        .header(HttpHeaders.AUTHORIZATION,
                                "Basic " + Base64Utils.encodeToString(userCredentials.getBytes()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonTransferRequest))
                .andExpect(status().isOk());

        Account firstUserAccountAfterTransfer = accountRepository.findById(firstUserAccount.getId()).get();
        Account secondUserAccountAfterTransfer = accountRepository.findById(secondUserAccount.getId()).get();
        Long actualFirstBalance = firstUserAccountAfterTransfer.getAmount();
        Long actualSecondBalance = secondUserAccountAfterTransfer.getAmount();

        assertThat(actualFirstBalance).isEqualTo(firstExpectedBalance);
        assertThat(actualSecondBalance).isEqualTo(secondExpectedBalance);
    }

    @DisplayName("Перевод от имени администратора одного пользователя другому не успешен")
    @Test
    @SneakyThrows
    void whenAdminTransferAnotherUserIsNotSuccess() {
        String adminToken = "SUPER_SECRET_KEY_FROM_ADMIN";
        String adminHeader = "X-SECURITY-ADMIN-KEY";
        Account firstUserAccount = new Account();
        Account secondUserAccount = new Account();
        User firstUser = new User("firstUser", HASHED_PASSWORD, new ArrayList<>());
        User secondUser = new User("secondUser", HASHED_PASSWORD, new ArrayList<>());
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

        String jsonTransferRequest = objectMapper.writeValueAsString(transferRequest);

        mockMvc.perform(post(TRANSFER_ENDPOINT, transferRequest)
                        .header(adminHeader, adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonTransferRequest))
                .andExpect(status().isForbidden());
    }
}
