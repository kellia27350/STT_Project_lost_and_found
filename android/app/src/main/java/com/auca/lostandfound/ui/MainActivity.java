package com.auca.lostandfound.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.auca.lostandfound.R;
import com.auca.lostandfound.databinding.ActivityMainBinding;
import com.auca.lostandfound.ui.admin.AdminFragment;
import com.auca.lostandfound.ui.announcements.AnnouncementsFragment;
import com.auca.lostandfound.ui.auth.AuthActivity;
import com.auca.lostandfound.ui.myreports.MyReportsFragment;
import com.auca.lostandfound.ui.report.ReportItemFragment;
import com.auca.lostandfound.util.SessionManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;

/**
 * Shell activity with Toolbar + BottomNavigationView.
 *
 * The Admin tab is hidden from STUDENT users — it only appears for ADMIN.
 * Pressing back from any fragment pops the back-stack; if empty, finishes.
 */
public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Safety redirect — should never happen but guards against stale intent
        if (!SessionManager.isLoggedIn(this)) {
            startActivity(new Intent(this, AuthActivity.class));
            finish();
            return;
        }

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.toolbar);

        // Hide admin tab from non-admin users
        if (!SessionManager.isAdmin(this)) {
            binding.bottomNav.getMenu().findItem(R.id.nav_admin).setVisible(false);
        }

        binding.bottomNav.setOnItemSelectedListener(this::onNavItemSelected);

        if (savedInstanceState == null) {
            loadFragment(new AnnouncementsFragment(), false);
            binding.bottomNav.setSelectedItemId(R.id.nav_announcements);
        }

        // Toolbar logout action
        binding.toolbar.inflateMenu(R.menu.toolbar_menu);
        binding.toolbar.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.action_logout) {
                logout();
                return true;
            }
            return false;
        });
    }

    private boolean onNavItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        Fragment fragment;

        if (id == R.id.nav_announcements) {
            fragment = new AnnouncementsFragment();
        } else if (id == R.id.nav_report) {
            fragment = new ReportItemFragment();
        } else if (id == R.id.nav_my_reports) {
            fragment = new MyReportsFragment();
        } else if (id == R.id.nav_admin) {
            fragment = new AdminFragment();
        } else {
            return false;
        }

        loadFragment(fragment, false);
        return true;
    }

    public void loadFragment(Fragment fragment, boolean addToBack) {
        FragmentTransaction tx = getSupportFragmentManager().beginTransaction();
        tx.replace(R.id.fragment_container, fragment);
        if (addToBack) tx.addToBackStack(null);
        tx.commit();
    }

    /** Update toolbar title from any fragment. */
    public void setToolbarTitle(String title) {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(title);
        }
    }

    private void logout() {
        SessionManager.clear(this);
        startActivity(new Intent(this, AuthActivity.class));
        finish();
    }
}
