package com.auca.lostandfound.ui.myreports;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.auca.lostandfound.databinding.FragmentMyReportsBinding;
import com.auca.lostandfound.model.Item;
import com.auca.lostandfound.ui.MainActivity;
import com.auca.lostandfound.ui.announcements.ItemAdapter;
import com.auca.lostandfound.ui.announcements.ItemViewModel;
import com.auca.lostandfound.ui.detail.ItemDetailFragment;
import com.auca.lostandfound.util.SessionManager;

import java.util.ArrayList;
import java.util.List;

public class MyReportsFragment extends Fragment {

    private FragmentMyReportsBinding binding;
    private ItemViewModel viewModel;
    private ItemAdapter adapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentMyReportsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).setToolbarTitle("My Reports");
        }

        viewModel = new ViewModelProvider(this).get(ItemViewModel.class);

        adapter = new ItemAdapter(requireContext(), this::openDetail, false, null);
        binding.recyclerMyReports.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerMyReports.setAdapter(adapter);

        binding.swipeRefresh.setOnRefreshListener(() -> {
            binding.swipeRefresh.setRefreshing(false);
            loadMyReports();
        });

        viewModel.isLoading().observe(getViewLifecycleOwner(), loading ->
                binding.progress.setVisibility(loading ? View.VISIBLE : View.GONE));

        viewModel.getItems().observe(getViewLifecycleOwner(), items -> {
            // Filter to only the current user's items (client-side filter)
            long myId = SessionManager.getUserId(requireContext());
            List<Item> mine = new ArrayList<>();
            if (items != null) {
                for (Item item : items) {
                    if (item.getCreatorUserId() != null
                            && item.getCreatorUserId() == myId) {
                        mine.add(item);
                    }
                }
            }
            adapter.setItems(mine);
            binding.tvEmpty.setVisibility(mine.isEmpty() ? View.VISIBLE : View.GONE);
        });

        loadMyReports();
    }

    private void loadMyReports() {
        // Load all items and filter client-side — a production backend would
        // expose GET /api/items/mine but this matches the current API surface.
        viewModel.loadItems(null, null);
    }

    private void openDetail(Item item) {
        if (getActivity() instanceof MainActivity) {
            Bundle args = new Bundle();
            args.putLong("itemId",        item.getItemId());
            args.putString("name",        item.getName());
            args.putString("category",    item.getCategory());
            args.putString("description", item.getDescription());
            args.putString("location",    item.getLocation());
            args.putString("reportDate",  item.getReportDate());
            args.putString("reportType",  item.getReportType());
            args.putString("status",      item.getStatus());
            args.putLong("creatorUserId", item.getCreatorUserId() != null
                                              ? item.getCreatorUserId() : -1L);
            ItemDetailFragment detail = new ItemDetailFragment();
            detail.setArguments(args);
            ((MainActivity) getActivity()).loadFragment(detail, true);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
