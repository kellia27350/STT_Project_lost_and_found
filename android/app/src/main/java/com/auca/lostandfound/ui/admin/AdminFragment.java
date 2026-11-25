package com.auca.lostandfound.ui.admin;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.auca.lostandfound.databinding.FragmentAdminBinding;
import com.auca.lostandfound.model.Item;
import com.auca.lostandfound.ui.MainActivity;
import com.auca.lostandfound.ui.announcements.ItemAdapter;
import com.auca.lostandfound.ui.announcements.ItemViewModel;
import com.auca.lostandfound.util.SessionManager;

/**
 * Admin-only screen.
 * Shows all reports with a Delete button on each card.
 * This fragment is only reachable by ADMIN users (the bottom nav tab is
 * hidden from STUDENT role in MainActivity).
 */
public class AdminFragment extends Fragment {

    private FragmentAdminBinding binding;
    private ItemViewModel viewModel;
    private ItemAdapter adapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentAdminBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).setToolbarTitle("Admin Panel");
        }

        viewModel = new ViewModelProvider(this).get(ItemViewModel.class);

        adapter = new ItemAdapter(requireContext(),
                item -> { /* detail tap — no-op in admin for now */ },
                true,  // showDelete = true
                this::confirmDelete);

        binding.recyclerAdmin.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerAdmin.setAdapter(adapter);

        binding.swipeRefresh.setOnRefreshListener(() -> {
            binding.swipeRefresh.setRefreshing(false);
            loadAll();
        });

        viewModel.isLoading().observe(getViewLifecycleOwner(), loading ->
                binding.progress.setVisibility(loading ? View.VISIBLE : View.GONE));

        viewModel.getItems().observe(getViewLifecycleOwner(), items -> {
            adapter.setItems(items);
            binding.tvEmpty.setVisibility(
                    (items == null || items.isEmpty()) ? View.VISIBLE : View.GONE);
        });

        viewModel.getDeletedId().observe(getViewLifecycleOwner(), id -> {
            if (id != null) adapter.removeItem(id);
        });

        viewModel.getError().observe(getViewLifecycleOwner(), err -> {
            if (err != null) binding.tvEmpty.setText(err);
        });

        loadAll();
    }

    private void loadAll() {
        viewModel.loadItems(null, null);
    }

    private void confirmDelete(Item item) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Remove Report")
                .setMessage("Are you sure you want to remove "" + item.getName() + ""?")
                .setPositiveButton("Delete", (d, w) -> {
                    viewModel.deleteItem(
                            item.getItemId(),
                            SessionManager.getUserIdString(requireContext()),
                            SessionManager.getRole(requireContext()));
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
