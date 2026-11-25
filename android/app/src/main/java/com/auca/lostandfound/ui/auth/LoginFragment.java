package com.auca.lostandfound.ui.auth;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.auca.lostandfound.databinding.FragmentLoginBinding;
import com.auca.lostandfound.model.AuthResponse;
import com.auca.lostandfound.model.LoginRequest;
import com.auca.lostandfound.network.ApiClient;
import com.auca.lostandfound.util.SessionManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginFragment extends Fragment {

    private FragmentLoginBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentLoginBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        binding.btnLogin.setOnClickListener(v -> attemptLogin());
        binding.tvGoRegister.setOnClickListener(v -> {
            if (getActivity() instanceof AuthActivity) {
                ((AuthActivity) getActivity()).showFragment(new RegisterFragment(), true);
            }
        });
    }

    private void attemptLogin() {
        String email    = text(binding.etEmail);
        String password = text(binding.etPassword);

        // Client-side validation
        boolean valid = true;
        if (TextUtils.isEmpty(email) || !email.contains("@")) {
            binding.tilEmail.setError("Enter a valid email address.");
            valid = false;
        } else {
            binding.tilEmail.setError(null);
        }
        if (TextUtils.isEmpty(password) || password.length() < 8) {
            binding.tilPassword.setError("Password must be at least 8 characters.");
            valid = false;
        } else {
            binding.tilPassword.setError(null);
        }
        if (!valid) return;

        setLoading(true);

        ApiClient.get().login(new LoginRequest(email, password))
                .enqueue(new Callback<AuthResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<AuthResponse> call,
                                           @NonNull Response<AuthResponse> response) {
                        setLoading(false);
                        if (response.isSuccessful() && response.body() != null) {
                            AuthResponse auth = response.body();
                            SessionManager.save(requireContext(),
                                    auth.getToken(),
                                    auth.getUserId(),
                                    auth.getEmail(),
                                    auth.getRole());
                            if (getActivity() instanceof AuthActivity) {
                                ((AuthActivity) getActivity()).goToMain();
                            }
                        } else {
                            showError("Invalid email or password.");
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<AuthResponse> call,
                                         @NonNull Throwable t) {
                        setLoading(false);
                        showError("Network error. Please try again.");
                    }
                });
    }

    private void setLoading(boolean loading) {
        binding.progress.setVisibility(loading ? View.VISIBLE : View.GONE);
        binding.btnLogin.setEnabled(!loading);
    }

    private void showError(String msg) {
        binding.tvError.setText(msg);
        binding.tvError.setVisibility(View.VISIBLE);
    }

    private String text(com.google.android.material.textfield.TextInputEditText et) {
        return et.getText() != null ? et.getText().toString().trim() : "";
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
