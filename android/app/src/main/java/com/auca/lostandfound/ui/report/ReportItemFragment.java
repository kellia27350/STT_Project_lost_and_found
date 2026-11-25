package com.auca.lostandfound.ui.report;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.auca.lostandfound.R;
import com.auca.lostandfound.databinding.FragmentReportItemBinding;
import com.auca.lostandfound.model.Item;
import com.auca.lostandfound.model.ItemRequest;
import com.auca.lostandfound.network.ApiClient;
import com.auca.lostandfound.ui.MainActivity;
import com.auca.lostandfound.util.SessionManager;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ReportItemFragment extends Fragment {

    private FragmentReportItemBinding binding;
    private String selectedReportType = "LOST"; // default

    private static final List<String> CATEGORIES = Arrays.asList(
            "Electronics", "Books", "Clothing",
            "Documents", "Keys", "Bags", "Sports", "Other");

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentReportItemBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).setToolbarTitle("Report an Item");
        }

        // Category spinner
        ArrayAdapter<String> catAdapter = new ArrayAdapter<>(
                requireContext(), android.R.layout.simple_spinner_item, CATEGORIES);
        catAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerCategory.setAdapter(catAdapter);

        // Default date = today
        binding.etDate.setText(
                new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date()));

        // Report type toggle
        binding.toggleReportType.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked) {
                selectedReportType = (checkedId == R.id.btn_lost) ? "LOST" : "FOUND";
            }
        });
        binding.toggleReportType.check(R.id.btn_lost); // default

        binding.btnSubmit.setOnClickListener(v -> attemptSubmit());
    }

    private void attemptSubmit() {
        String itemName  = text(binding.etName);
        String category  = (String) binding.spinnerCategory.getSelectedItem();
        String desc      = text(binding.etDescription);
        String location  = text(binding.etLocation);
        String date      = text(binding.etDate);

        boolean valid = true;

        if (itemName.isEmpty()) {
            binding.tilName.setError("Item name is required.");
            valid = false;
        } else { binding.tilName.setError(null); }

        if (desc.isEmpty()) {
            binding.tilDescription.setError("Description is required.");
            valid = false;
        } else { binding.tilDescription.setError(null); }

        if (location.isEmpty()) {
            binding.tilLocation.setError("Location is required.");
            valid = false;
        } else { binding.tilLocation.setError(null); }

        if (date.isEmpty() || !date.matches("\\d{4}-\\d{2}-\\d{2}")) {
            binding.tilDate.setError("Enter date as YYYY-MM-DD.");
            valid = false;
        } else { binding.tilDate.setError(null); }

        if (!valid) return;

        setLoading(true);

        ApiClient.get().createItem(
                SessionManager.getUserIdString(requireContext()),
                SessionManager.getRole(requireContext()),
                new ItemRequest(itemName, category, desc, location, date, selectedReportType))
                .enqueue(new Callback<Item>() {
                    @Override
                    public void onResponse(@NonNull Call<Item> call,
                                           @NonNull Response<Item> response) {
                        setLoading(false);
                        if (response.isSuccessful()) {
                            showMessage(
                                    "✅ Report submitted! It is now visible on the Browse screen.",
                                    true);
                            resetForm();
                        } else {
                            showMessage("Submission failed. Please check all fields.", false);
                        }
                    }
                    @Override
                    public void onFailure(@NonNull Call<Item> call, @NonNull Throwable t) {
                        setLoading(false);
                        showMessage("Network error. Please try again.", false);
                    }
                });
    }

    private void resetForm() {
        binding.etName.setText("");
        binding.etDescription.setText("");
        binding.etLocation.setText("");
        binding.spinnerCategory.setSelection(0);
        binding.toggleReportType.check(R.id.btn_lost);
        selectedReportType = "LOST";
        binding.etDate.setText(
                new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date()));
    }

    private void setLoading(boolean loading) {
        binding.progress.setVisibility(loading ? View.VISIBLE : View.GONE);
        binding.btnSubmit.setEnabled(!loading);
    }

    private void showMessage(String msg, boolean success) {
        binding.tvMessage.setText(msg);
        binding.tvMessage.setTextColor(requireContext().getColor(
                success ? R.color.status_available_text : R.color.error));
        binding.tvMessage.setVisibility(View.VISIBLE);
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
