package com.auca.lostandfound.ui.auth;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.auca.lostandfound.databinding.FragmentRegisterBinding;
import com.auca.lostandfound.model.RegisterRequest;
import com.auca.lostandfound.model.RegisterResponse;
import com.auca.lostandfound.network.ApiClient;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterFragment extends Fragment {

    private FragmentRegisterBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentRegisterBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        binding.btnRegister.setOnClickListener(v -> attemptRegister());
        binding.tvGoLogin.setOnClickListener(v -> {
            if (getActivity() != null) getActivity().onBackPressed();
        });
    }

    private void attemptRegister() {
        String studentId = text(binding.etStudentId);
        String name      = text(binding.etName);
        String email     = text(binding.etEmail);
        String password  = text(binding.etPassword);

        boolean valid = true;

        if (TextUtils.isEmpty(studentId)) {
            binding.tilStudentId.setError("Student ID is required.");
            valid = false;
        } else { binding.tilStudentId.setError(null); }

        if (TextUtils.isEmpty(name)) {
            binding.tilName.setError("Name is required.");
            valid = false;
        } else { binding.tilName.setError(null); }

        if (TextUtils.isEmpty(email) || !email.contains("@")) {
            binding.tilEmail.setError("Enter a valid email address.");
            valid = false;
        } else { binding.tilEmail.setError(null); }

        if (TextUtils.isEmpty(password) || password.length() < 8) {
            binding.tilPassword.setError("Password must be at least 8 characters.");
            valid = false;
        } else { binding.tilPassword.setError(null); }

        if (!valid) return;

        setLoading(true);

        ApiClient.get().register(new RegisterRequest(studentId, name, email, password))
                .enqueue(new Callback<RegisterResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<RegisterResponse> call,
                                           @NonNull Response<RegisterResponse> response) {
                        setLoading(false);
                        if (response.isSuccessful()) {
                            // Registration succeeded — take user back to login
                            showError("Account created! Please log in.");
                            binding.tvError.setTextColor(
                                    requireContext().getColor(
                                            com.auca.lostandfound.R.color.status_available_text));
                            if (getActivity() != null) getActivity().onBackPressed();
                        } else if (response.code() == 409) {
                            showError("That email is already registered.");
                        } else {
                            showError("Registration failed. Please try again.");
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<RegisterResponse> call,
                                         @NonNull Throwable t) {
                        setLoading(false);
                        showError("Network error. Please try again.");
                    }
                });
    }

    private void setLoading(boolean loading) {
        binding.progress.setVisibility(loading ? View.VISIBLE : View.GONE);
        binding.btnRegister.setEnabled(!loading);
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
