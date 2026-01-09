package com.example.application.ClientManagementServer.Controller;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import com.example.application.ClientManagementServer.Entity.*;
import com.google.gson.Gson;
import java.util.Map;
import java.util.UUID;

/**
 * 管理サーバー用：Jersey (JAX-RS) 形式のコントローラー
 */
@Path("/auth") // RESTのベースパス。Launcherの /api と組み合わさり /api/auth となります
public class AccountManagement {
    private final DatabaseAccess dbAccess = new DatabaseAccess();
    private final Gson gson = new Gson();

    public AccountManagement() {
        System.out.println("[AccountManagement] JAX-RS インスタンスが作成されました");
    }

    // POST /api/auth/login
    @Path("/login")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response loginApi(String jsonBody) {
        // Springの @RequestBody の代わりに Gson で手動解析します
        Map<String, String> req = gson.fromJson(jsonBody, Map.class);
        String username = req.get("username");
        String password = req.get("password");

        User user = authenticate(username, password);
        if (user != null) {
            // ResponseEntity の代わりに Response を使用します
            return Response.ok(gson.toJson(user)).build();
        }
        System.out.println("[AccountManagement] Login failed for user: " + username);
        return Response.status(Response.Status.UNAUTHORIZED).build();
    }

    // POST /api/auth/register
    @Path("/register")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response registerApi(String jsonBody) {
        Map<String, String> req = gson.fromJson(jsonBody, Map.class);
        boolean success = registerAccount(req.get("username"), req.get("password"));
        return success ? Response.ok("Success").build() : Response.status(Response.Status.BAD_REQUEST).build();
    }

    // POST /api/auth/logout
    @Path("/logout")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response logoutApi(String jsonBody) {
        Map<String, String> req = gson.fromJson(jsonBody, Map.class);
        logout(req.get("username"));
        return Response.ok("Logged out").build();
    }

    // GET /api/auth/score?username=...
    @Path("/score")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getScore(@QueryParam("username") String username) {
        try {
            RankRecord record = dbAccess.getRankRecordByUsername(username);
            return Response.ok(gson.toJson(record)).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    // --- 内部ロジック (ClientManagementControllerからも利用可能) ---
    public User authenticate(String username, String password) {
        try {
            User user = dbAccess.getUserByUsername(username);
            if (user != null && user.password().equals(password)) {
                if (dbAccess.getLoginStatusByUsername(username)) return null;
                dbAccess.setLoginStatus(username, true);
                return user;
            }
        } catch (Exception e) { e.printStackTrace(); }
        return null;
    }

    public boolean registerAccount(String username, String password) {
        try {
            if (dbAccess.getUserByUsername(username) != null) return false;
            dbAccess.createUser(username, password, UUID.randomUUID().toString());
            return true;
        } catch (Exception e) { return false; }
    }

    public void logout(String username) {
        if (username != null) dbAccess.setLoginStatus(username, false);
    }
}