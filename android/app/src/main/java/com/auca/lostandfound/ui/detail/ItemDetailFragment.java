package com.auca.lostandfound.ui.detail;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.auca.lostandfound.databinding.FragmentItemDetailBinding;
import com.auca.lostandfound.model.Claim;
import com.auca.lostandfound.model.ClaimRequest;
import com.auca.lostandfound.network.ApiClient;
import com.auca.lostandfound.ui.MainActivity;
import com.auca.lostandfound.ui.announcements.ItemViewModel;
import com.auca.lostandfound.util.SessionManager;
import com.auca.lostandfound.util.UiUtils;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ItemDetailFragment extends Fragment {

    private FragmentItemDetailBinding binding;

    // Item fields passed via arguments bundle
    private long   itemId;
    private String name, category, description, location, reportDate, reportType, status;
    private long   creatorUserId;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentItemDetailBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        Bundle args = getArguments();
        if (args == null) return;

        itemId        = args.getLong("itemId");
        name          = args.getString("name",        "");
        category      = args.getString("category",    "");
        description   = args.getString("description", "");
        location      = args.getString("location",    "");
        reportDate    = args.getString("reportDate",  "");
        reportType    = args.getString("reportType",  "");
        status        = args.getString("status",      "");
        creatorUserId = args.getLong("creatorUserId", -1L);

        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).setToolbarTitle(name);
        }

        // Populate fields
        binding.tvName.setText(name);
        binding.tvCategory.setText(category);
        binding.tvLocation.setText(location);
        binding.tvDate.setText(reportDate);
        binding.tvDescription.setText(description);
        UiUtils.applyReportTypeBadge(requireContext(), binding.tvReportType, reportType);
        UiUtils.applyStatusBadge(requireContext(), binding.tvStatus, status);

        long myUserId = SessionManager.getUserId(requireContext());
        boolean isAdmin   = SessionManager.isAdmin(requireContext());
        boolean isOwner   = (myUserId == creatorUserId);
        boolean available = "AVAILABLE".equalsIgnoreCase(status);
        boolean isFound   = "FOUND".equalsIgnoreCase(reportType);

        // Show claim controls only on FOUND + AVAILABLE + not my own report
        if (isFound && available && !isOwner) {
            binding.tilExplanation.setVisibility(View.VISIBLE);
            binding.btnClaim.setVisibility(View.VISIBLE);
            binding.btnClaim.setOnClickListener(v -> submitClaim());
        }

        // Show "Mark Returned" to report owner or admin when still AVAILABLE
        if (available && (isOwner || isAdmin)) {
            binding.btnMarkReturned.setVisibility(View.VISIBLE);
            binding.btnMarkReturned.setOnClickListener(v -> markReturned());
        }
    }

    // ── Submit claim ──────────────────────────────────────────────────────────

    private void submitClaim() {
        String explanation = binding.etExplanation.getText() != null
                ? binding.etExplanation.getText().toString().trim() : "";
        if (explanation.isEmpty()) {
            binding.tilExplanation.setError(
                    "Please describe how you can prove this item is yours.");
            return;
        }
        binding.tilExplanation.setError(null);
        binding.btnClaim.setEnabled(false);

        ApiClient.get().submitClaim(
                itemId,
                SessionManager.getUserIdString(requireContext()),
                SessionManager.getRole(requireContext()),
                new ClaimRequest(explanation))
                .enqueue(new Callback<Claim>() {
                    @Override
                    public void onResponse(@NonNull Call<Claim> call,
                                           @NonNull Response<Claim> response) {
                        if (response.isSuccessful()) {
                            showResult("✅ Claim submitted. You will be contacted privately.");
                            binding.btnClaim.setVisibility(View.GONE);
                            binding.tilExplanation.setVisibility(View.GONE);
                        } else if (response.code() == 409) {
                            showResult("You already have an active claim on this item.");
                            binding.btnClaim.setEnabled(true);
                        } else {
                            showResult("Could not submit claim. Please try again.");
                            binding.btnClaim.setEnabled(true);
                        }
                    }
                    @Override
                    public void onFailure(@NonNull Call<Claim> call, @NonNull Throwable t) {
                        showResult("Network error. Please try again.");
                        binding.btnClaim.setEnabled(true);
                    }
                });
    }

    // ── Mark returned ─────────────────────────────────────────────────────────

    private void markReturned() {
        binding.btnMarkReturned.setEnabled(false);
        ItemViewModel vm = new ViewModelProvider(this).get(ItemViewModel.class);
        vm.getUpdatedItem().observe(getViewLifecycleOwner(), item -> {
            if (item != null) {
                UiUtils.applyStatusBadge(requireContext(), binding.tvStatus, item.getStatus());
                binding.btnMarkReturned.setVisibility(View.GONE);
                binding.btnClaim.setVisibility(View.GONE);
                binding.tilExplanation.setVisibility(View.GONE);
                showResult("✅ Item marked as RETURNED.");
            }
        });
        vm.getError().observe(getViewLifecycleOwner(), err -> {
            if (err != null) {
                showResult(err);
                binding.btnMarkReturned.setEnabled(true);
            }
        });
        vm.updateStatus(itemId,
                "RETURNED",
                SessionManager.getUserIdString(requireContext()),
                SessionManager.getRole(requireContext()));
    }

    private void showResult(String msg) {
        binding.tvResult.setText(msg);
        binding.tvResult.setVisibility(View.VISIBLE);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
