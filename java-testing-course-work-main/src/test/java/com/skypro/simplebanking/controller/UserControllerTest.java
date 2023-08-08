package com.skypro.simplebanking.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.skypro.simplebanking.IntegrationTestBase;
import com.skypro.simplebanking.dto.BankingUserDetails;
import com.skypro.simplebanking.dto.CreateUserRequest;
import com.skypro.simplebanking.entity.User;
import com.skypro.simplebanking.repository.UserRepository;
import lombok.SneakyThrows;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;

class UserControllerTest extends IntegrationTestBase {

    private static final String USER_ENDPOINT = "/user/";
    private static final String PATH_GET_ALL_USERS = "list";
    private static final String PATH_GET_MY_PROFILE = "me";

    @Autowired
    MockMvc mockMvc;
    @Autowired
    UserRepository userRepository;
    @Autowired
    ObjectMapper objectMapper;


    @DisplayName("Создание пользователя администратором успешно")
    @Test
    @SneakyThrows
    void whenAdminCreateUserIsSuccess() {
        CreateUserRequest createUserRequest = new CreateUserRequest();
        createUserRequest.setUsername("NewUser");
        createUserRequest.setPassword("password");

        String jsonUserRequest = new ObjectMapper().writeValueAsString(createUserRequest);

        mockMvc.perform(post(USER_ENDPOINT, createUserRequest)
                        .with(user("user_admin").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonUserRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("NewUser"))
                .andExpect(jsonPath("$.accounts[0].amount").value(1L))
                .andExpect(jsonPath("$.accounts[1].amount").value(1L))
                .andExpect(jsonPath("$.accounts[2].amount").value(1L));
    }

    @DisplayName("Создание пользователя пользователем не успешно")
    @Test
    @SneakyThrows
    void whenUserCreateUserIsNotSuccess() {
        CreateUserRequest createUserRequest = new CreateUserRequest();
        createUserRequest.setUsername("NewUser");
        createUserRequest.setPassword("password");

        String jsonUserRequest = new ObjectMapper().writeValueAsString(createUserRequest);

        mockMvc.perform(post(USER_ENDPOINT, createUserRequest)
                        .with(user("test_user").roles("USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonUserRequest))
                .andExpect(status().is4xxClientError());
    }

    @DisplayName("Пользователь получает список всех пользователей успешно")
    @Test
    @SneakyThrows
    void whenUserGetAllUsersIsSuccess() {
        userRepository.save(new User("firstUser", "2236", new ArrayList<>()));
        userRepository.save(new User("secondUser", "2236", new ArrayList<>()));

        mockMvc.perform(get(USER_ENDPOINT + PATH_GET_ALL_USERS)
                        .with(user("test_user").roles("USER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isNotEmpty());
    }

    @DisplayName("Администратор получает список всех пользователей не успешно")
    @Test
    @SneakyThrows
    void whenAdminGetAllUsersIsNotSuccess() {
        userRepository.save(new User("firstUser", "2236", new ArrayList<>()));
        userRepository.save(new User("secondUser", "2236", new ArrayList<>()));

        mockMvc.perform(get(USER_ENDPOINT + PATH_GET_ALL_USERS)
                        .with(user("admin").roles("ADMIN")))
                .andExpect(status().is4xxClientError());
    }

    @DisplayName("Пользователь получает свой профиль успешно")
    @Test
    @SneakyThrows
    void whenUserGetProfileIsSuccess() {
        User user = new User("test_user", "2236", new ArrayList<>());
        userRepository.save(user);
        BankingUserDetails userDetails = new BankingUserDetails(user.getId(), "test_user", "2236", false);

        mockMvc.perform(get(USER_ENDPOINT + PATH_GET_MY_PROFILE)
                        .with(SecurityMockMvcRequestPostProcessors.user(userDetails))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(user.getId()))
                .andExpect(jsonPath("$.username").value("test_user"));
    }

    @DisplayName("Администратор получает профиль не успешно")
    @Test
    @SneakyThrows
    void whenAdminGetProfileIsNotSuccess() {
        mockMvc.perform(get(USER_ENDPOINT + PATH_GET_MY_PROFILE)
                        .with(user("admin").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError());
    }

}
