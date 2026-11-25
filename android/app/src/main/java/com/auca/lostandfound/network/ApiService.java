package com.auca.lostandfound.network;

import com.auca.lostandfound.model.AuthResponse;
import com.auca.lostandfound.model.Claim;
import com.auca.lostandfound.model.ClaimRequest;
import com.auca.lostandfound.model.Item;
import com.auca.lostandfound.model.ItemRequest;
import com.auca.lostandfound.model.LoginRequest;
import com.auca.lostandfound.model.RegisterRequest;
import com.auca.lostandfound.model.RegisterResponse;
import com.auca.lostandfound.model.StatusUpdateRequest;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * Maps every backend endpoint to a Retrofit call.
 *
 * Authentication is passed via the X-User-Id and X-User-Role headers
 * (matching the backend's HeaderAuthFilter).  The JWT token is kept
 * in SessionManager and injected by the calling ViewModel.
 */
public interface ApiService {

    // ── Auth ──────────────────────────────────────────────────────────────────

    @POST("api/auth/register")
    Call<RegisterResponse> register(@Body RegisterRequest request);

    @POST("api/auth/login")
    Call<AuthResponse> login(@Body LoginRequest request);

    // ── Items — public read (no auth header needed) ───────────────────────────

    /** Browse / filter: ?type=FOUND&category=Electronics */
    @GET("api/items")
    Call<List<Item>> getItems(
            @Query("type")     String type,
            @Query("category") String category);

    /** Keyword search across name, description, location */
    @GET("api/items/search")
    Call<List<Item>> searchItems(@Query("keyword") String keyword);

    // ── Items — authenticated writes ──────────────────────────────────────────

    @POST("api/items")
    Call<Item> createItem(
            @Header("X-User-Id")   String userId,
            @Header("X-User-Role") String role,
            @Body ItemRequest request);

    @PATCH("api/items/{id}/status")
    Call<Item> updateStatus(
            @Path("id")            Long id,
            @Header("X-User-Id")   String userId,
            @Header("X-User-Role") String role,
            @Body StatusUpdateRequest request);

    // ── Claims ────────────────────────────────────────────────────────────────

    @POST("api/claims/item/{itemId}")
    Call<Claim> submitClaim(
            @Path("itemId")        Long itemId,
            @Header("X-User-Id")   String userId,
            @Header("X-User-Role") String role,
            @Body ClaimRequest request);

    // ── Admin ─────────────────────────────────────────────────────────────────

    @DELETE("api/admin/items/{id}")
    Call<Map<String, Object>> deleteItem(
            @Path("id")            Long id,
            @Header("X-User-Id")   String userId,
            @Header("X-User-Role") String role);
}
