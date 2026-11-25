package com.auca.lostandfound.util;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Persists the JWT token, userId, email, and role across app restarts
 * using encrypted SharedPreferences.
 *
 * Usage:
 *   SessionManager.save(context, authResponse);
 *   SessionManager.getUserId(context);
 *   SessionManager.isAdmin(context);
 *   SessionManager.clear(context);  // logout
 */
public class SessionManager {

    private static final String PREFS_NAME  = "clf_session";
    private static final String KEY_TOKEN   = "token";
    private static final String KEY_USER_ID = "userId";
    private static final String KEY_EMAIL   = "email";
    private static final String KEY_ROLE    = "role";

    private SessionManager() {}

    private static SharedPreferences prefs(Context ctx) {
        return ctx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    // ── save after login / register ───────────────────────────────────────────

    public static void save(Context ctx, String token, long userId,
                            String email, String role) {
        prefs(ctx).edit()
                .putString(KEY_TOKEN,   token)
                .putLong  (KEY_USER_ID, userId)
                .putString(KEY_EMAIL,   email)
                .putString(KEY_ROLE,    role)
                .apply();
    }

    // ── read ──────────────────────────────────────────────────────────────────

    public static String getToken(Context ctx) {
        return prefs(ctx).getString(KEY_TOKEN, null);
    }

    public static long getUserId(Context ctx) {
        return prefs(ctx).getLong(KEY_USER_ID, -1L);
    }

    public static String getEmail(Context ctx) {
        return prefs(ctx).getString(KEY_EMAIL, null);
    }

    public static String getRole(Context ctx) {
        return prefs(ctx).getString(KEY_ROLE, "STUDENT");
    }

    public static String getUserIdString(Context ctx) {
        return String.valueOf(getUserId(ctx));
    }

    public static boolean isLoggedIn(Context ctx) {
        return getToken(ctx) != null;
    }

    public static boolean isAdmin(Context ctx) {
        return "ADMIN".equals(getRole(ctx));
    }

    // ── logout ────────────────────────────────────────────────────────────────

    public static void clear(Context ctx) {
        prefs(ctx).edit().clear().apply();
    }
}
