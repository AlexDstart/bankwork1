package com.skypro.simplebanking.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.skypro.simplebanking.IntegrationTestBase;
import com.skypro.simplebanking.dto.CreateUserRequest;
import com.skypro.simplebanking.entity.User;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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

    @AfterEach
    void clearAll() {
        userRepository.deleteAll();
    }

    @DisplayName("Создание пользователя администратором успешно")
    @Test
    @SneakyThrows
    void whenAdminCreateUserIsSuccess() {
        CreateUserRequest createUserRequest = new CreateUserRequest();
        createUserRequest.setUsername("NewUser");
        createUserRequest.setPassword("password");

        String jsonUserRequest = objectMapper.writeValueAsString(createUserRequest);

        mockMvc.perform(post(USER_ENDPOINT, createUserRequest)
                        .header(ADMIN_HEADER, ADMIN_TOKEN)
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
        User user = new User("test_user", HASHED_PASSWORD, new ArrayList<>());
        userRepository.save(user);
        CreateUserRequest createUserRequest = new CreateUserRequest();
        createUserRequest.setUsername("NewUser");
        createUserRequest.setPassword("password");

        String jsonUserRequest = objectMapper.writeValueAsString(createUserRequest);

        mockMvc.perform(post(USER_ENDPOINT, createUserRequest)
                        .header(HttpHeaders.AUTHORIZATION,
                                "Basic " + Base64Utils.encodeToString(USER_CREDENTIALS.getBytes()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonUserRequest))
                .andExpect(status().isForbidden());
    }

    @DisplayName("Пользователь получает список всех пользователей успешно")
    @Test
    @SneakyThrows
    void whenUserGetAllUsersIsSuccess() {
        User user = new User("test_user", HASHED_PASSWORD, new ArrayList<>());
        userRepository.save(user);
        userRepository.save(new User("firstUser", "2236", new ArrayList<>()));
        userRepository.save(new User("secondUser", "2236", new ArrayList<>()));

        mockMvc.perform(get(USER_ENDPOINT + PATH_GET_ALL_USERS)
                        .header(HttpHeaders.AUTHORIZATION,
                                "Basic " + Base64Utils.encodeToString(USER_CREDENTIALS.getBytes())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isNotEmpty())
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$.[0].username").value("test_user"))
                .andExpect(jsonPath("$.[1].username").value("firstUser"))
                .andExpect(jsonPath("$.[2].username").value("secondUser"));
    }

    @DisplayName("Администратор получает список всех пользователей не успешно")
    @Test
    @SneakyThrows
    void whenAdminGetAllUsersIsNotSuccess() {
        userRepository.save(new User("firstUser", "2236", new ArrayList<>()));
        userRepository.save(new User("secondUser", "2236", new ArrayList<>()));

        mockMvc.perform(get(USER_ENDPOINT + PATH_GET_ALL_USERS)
                        .header(ADMIN_HEADER, ADMIN_TOKEN))
                .andExpect(status().isForbidden());
    }

    @DisplayName("Пользователь получает свой профиль успешно")
    @Test
    @SneakyThrows
    void whenUserGetProfileIsSuccess() {
        User user = new User("test_user", HASHED_PASSWORD, new ArrayList<>());
        userRepository.save(user);

        mockMvc.perform(get(USER_ENDPOINT + PATH_GET_MY_PROFILE)
                        .header(HttpHeaders.AUTHORIZATION,
                                "Basic " + Base64Utils.encodeToString(USER_CREDENTIALS.getBytes()))
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
                        .header(ADMIN_HEADER, ADMIN_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

}
