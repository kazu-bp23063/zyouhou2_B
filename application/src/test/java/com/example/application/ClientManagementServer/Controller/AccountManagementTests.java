package com.example.application.ClientManagementServer.Controller;

import com.example.application.ClientManagementServer.Entity.*;
import com.google.gson.Gson;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountManagementTests {

    private AccountManagement accountManagement;
    
    private final Gson gson = new Gson();

    @Mock
    private DatabaseAccess dbAccess;

    @Mock
    private User user;

    @BeforeEach
    void setUp() throws Exception {
        accountManagement = new AccountManagement();
        // private finalのフィールドをモックに差し替え
        Field dbAccessField = AccountManagement.class.getDeclaredField("dbAccess");
        dbAccessField.setAccessible(true);
        dbAccessField.set(accountManagement, dbAccess);
    }

    private String createJson(String username, String password) {
        Map<String, String> map = new HashMap<>();
        if (username != null) map.put("username", username);
        if (password != null) map.put("password", password);
        return gson.toJson(map);
    }

    // LoginのTest
    @Test
    void loginSuccess() {
        String jsonBody = createJson("testUser", "password123");
        when(dbAccess.getUserByUsername("testUser")).thenReturn(user);
        when(user.password()).thenReturn("password123");
        when(dbAccess.getLoginStatusByUsername("testUser")).thenReturn(false);
        Response response = accountManagement.loginApi(jsonBody);
        assertEquals(200, response.getStatus());
        verify(dbAccess).setLoginStatus("testUser", true);
    }

    @Test
    void loginFailureUserNotFound() {
        String jsonBody = createJson("unknownUser", "pass");
        when(dbAccess.getUserByUsername("unknownUser")).thenReturn(null);
        Response response = accountManagement.loginApi(jsonBody);
        assertEquals(401, response.getStatus());
        verify(dbAccess, never()).setLoginStatus(anyString(), anyBoolean());
    }

    @Test
    void loginFailureWrongPassword() {
        String jsonBody = createJson("testUser", "wrongPass");
        when(dbAccess.getUserByUsername("testUser")).thenReturn(user);
        when(user.password()).thenReturn("correctPass");
        Response response = accountManagement.loginApi(jsonBody);
        assertEquals(401, response.getStatus());
        verify(dbAccess, never()).setLoginStatus(anyString(), anyBoolean());
    }

    @Test
    void loginFailureAlreadyLoggedIn() {
        String jsonBody = createJson("testUser", "password123");
        when(dbAccess.getUserByUsername("testUser")).thenReturn(user);
        when(user.password()).thenReturn("password123");
        when(dbAccess.getLoginStatusByUsername("testUser")).thenReturn(true);
        Response response = accountManagement.loginApi(jsonBody);
        assertEquals(401, response.getStatus());
    }

    // RegisterAccountのTest
    @Test
    void registerAccountSuccess() {
        String jsonBody = createJson("newUser", "pass");
        when(dbAccess.getUserByUsername("newUser")).thenReturn(null);
        Response response = accountManagement.registerApi(jsonBody);
        assertEquals(200, response.getStatus());
        verify(dbAccess).createUser(eq("newUser"), eq("pass"), anyString());
    }

    @Test
    void registerAccountFailureUserExists() {
        String jsonBody = createJson("existingUser", "pass");
        when(dbAccess.getUserByUsername("existingUser")).thenReturn(user);
        Response response = accountManagement.registerApi(jsonBody);
        assertEquals(400, response.getStatus());
        verify(dbAccess, never()).createUser(anyString(), anyString(), anyString());
    }

    // LogoutのTest
    @Test
    void testLogout() {
        Map<String, String> req = Map.of("username", "testUser");
        String jsonBody = gson.toJson(req);
        Response response = accountManagement.logoutApi(jsonBody);
        assertEquals(200, response.getStatus());
        verify(dbAccess).setLoginStatus("testUser", false);
    }
}