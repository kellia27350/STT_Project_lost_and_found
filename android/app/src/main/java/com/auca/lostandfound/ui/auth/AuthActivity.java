package com.auca.lostandfound.ui.auth;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.auca.lostandfound.R;
import com.auca.lostandfound.ui.MainActivity;
import com.auca.lostandfound.util.SessionManager;

/**
 * Single-activity host for the auth flow.
 * Shows LoginFragment first; swaps to RegisterFragment on demand.
 * Redirects to MainActivity immediately when the user is already logged in.
 */
public class AuthActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Skip auth if the user already has a valid session
        if (SessionManager.isLoggedIn(this)) {
            goToMain();
            return;
        }

        setContentView(R.layout.activity_auth);

        if (savedInstanceState == null) {
            showFragment(new LoginFragment(), false);
        }
    }

    // ── Fragment navigation ───────────────────────────────────────────────────

    public void showFragment(Fragment fragment, boolean addToBack) {
        FragmentTransaction tx = getSupportFragmentManager().beginTransaction();
        tx.replace(R.id.auth_container, fragment);
        if (addToBack) tx.addToBackStack(null);
        tx.commit();
    }

    public void goToMain() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }
}
