package com.auca.lostandfound.ui.announcements;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.auca.lostandfound.R;
import com.auca.lostandfound.databinding.FragmentAnnouncementsBinding;
import com.auca.lostandfound.model.Item;
import com.auca.lostandfound.ui.MainActivity;
import com.auca.lostandfound.ui.detail.ItemDetailFragment;

import java.util.Arrays;
import java.util.List;

public class AnnouncementsFragment extends Fragment {

    private FragmentAnnouncementsBinding binding;
    private ItemViewModel viewModel;
    private ItemAdapter adapter;

    private String selectedType     = null;   // null = all
    private String selectedCategory = null;   // null = all

    private static final List<String> CATEGORIES = Arrays.asList(
            "All Categories", "Electronics", "Books", "Clothing",
            "Documents", "Keys", "Bags", "Sports", "Other");

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentAnnouncementsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).setToolbarTitle("Campus Lost & Found");
        }

        viewModel = new ViewModelProvider(this).get(ItemViewModel.class);

        // RecyclerView
        adapter = new ItemAdapter(requireContext(), this::openDetail, false, null);
        binding.recyclerItems.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerItems.setAdapter(adapter);

        // Category spinner
        ArrayAdapter<String> catAdapter = new ArrayAdapter<>(
                requireContext(), android.R.layout.simple_spinner_item, CATEGORIES);
        catAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerCategory.setAdapter(catAdapter);
        binding.spinnerCategory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View v, int pos, long id) {
                selectedCategory = pos == 0 ? null : CATEGORIES.get(pos);
                applyFilter();
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });

        // Type chips
        binding.chipGroupType.setOnCheckedStateChangeListener((group, ids) -> {
            if (ids.isEmpty()) return;
            int id = ids.get(0);
            if (id == R.id.chip_lost)  selectedType = "LOST";
            else if (id == R.id.chip_found) selectedType = "FOUND";
            else selectedType = null;
            applyFilter();
        });

        // Search
        binding.etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int i, int c, int a) {}
            @Override public void afterTextChanged(Editable s) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String q = s.toString().trim();
                if (q.isEmpty()) applyFilter();
                else viewModel.searchItems(q);
            }
        });

        // Pull-to-refresh
        binding.swipeRefresh.setOnRefreshListener(() -> {
            binding.swipeRefresh.setRefreshing(false);
            applyFilter();
        });

        // Observe
        viewModel.isLoading().observe(getViewLifecycleOwner(), loading -> {
            binding.progress.setVisibility(loading ? View.VISIBLE : View.GONE);
        });

        viewModel.getItems().observe(getViewLifecycleOwner(), items -> {
            adapter.setItems(items);
            binding.tvEmpty.setVisibility(
                    (items == null || items.isEmpty()) ? View.VISIBLE : View.GONE);
        });

        viewModel.getError().observe(getViewLifecycleOwner(), err -> {
            if (err != null) binding.tvEmpty.setText(err);
        });

        applyFilter();
    }

    private void applyFilter() {
        String q = binding.etSearch.getText() != null
                ? binding.etSearch.getText().toString().trim() : "";
        if (!q.isEmpty()) {
            viewModel.searchItems(q);
        } else {
            viewModel.loadItems(selectedType, selectedCategory);
        }
    }

    private void openDetail(Item item) {
        if (getActivity() instanceof MainActivity) {
            Bundle args = new Bundle();
            args.putLong("itemId",          item.getItemId());
            args.putString("name",          item.getName());
            args.putString("category",      item.getCategory());
            args.putString("description",   item.getDescription());
            args.putString("location",      item.getLocation());
            args.putString("reportDate",    item.getReportDate());
            args.putString("reportType",    item.getReportType());
            args.putString("status",        item.getStatus());
            args.putLong("creatorUserId",   item.getCreatorUserId() != null
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
