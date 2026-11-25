package com.auca.lostandfound.ui.announcements;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.auca.lostandfound.model.Item;
import com.auca.lostandfound.model.StatusUpdateRequest;
import com.auca.lostandfound.network.ApiClient;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Shared ViewModel for item lists (Announcements, My Reports, Admin).
 * All Retrofit calls run on background threads; results posted to LiveData
 * which fragments observe on the main thread.
 */
public class ItemViewModel extends ViewModel {

    private final MutableLiveData<List<Item>> itemsLive   = new MutableLiveData<>();
    private final MutableLiveData<Boolean>    loadingLive = new MutableLiveData<>(false);
    private final MutableLiveData<String>     errorLive   = new MutableLiveData<>();
    private final MutableLiveData<Item>       updatedItem = new MutableLiveData<>();
    private final MutableLiveData<Long>       deletedId   = new MutableLiveData<>();

    public LiveData<List<Item>> getItems()       { return itemsLive; }
    public LiveData<Boolean>    isLoading()      { return loadingLive; }
    public LiveData<String>     getError()       { return errorLive; }
    public LiveData<Item>       getUpdatedItem() { return updatedItem; }
    public LiveData<Long>       getDeletedId()   { return deletedId; }

    // ── Load all items (optional type + category filter) ─────────────────────

    public void loadItems(String type, String category) {
        loadingLive.setValue(true);
        ApiClient.get().getItems(type, category).enqueue(new Callback<List<Item>>() {
            @Override
            public void onResponse(Call<List<Item>> call, Response<List<Item>> response) {
                loadingLive.postValue(false);
                if (response.isSuccessful() && response.body() != null) {
                    itemsLive.postValue(response.body());
                } else {
                    errorLive.postValue("Failed to load items.");
                }
            }
            @Override
            public void onFailure(Call<List<Item>> call, Throwable t) {
                loadingLive.postValue(false);
                errorLive.postValue("Network error: " + t.getMessage());
            }
        });
    }

    // ── Keyword search ────────────────────────────────────────────────────────

    public void searchItems(String keyword) {
        loadingLive.setValue(true);
        ApiClient.get().searchItems(keyword).enqueue(new Callback<List<Item>>() {
            @Override
            public void onResponse(Call<List<Item>> call, Response<List<Item>> response) {
                loadingLive.postValue(false);
                if (response.isSuccessful() && response.body() != null) {
                    itemsLive.postValue(response.body());
                } else {
                    errorLive.postValue("Search failed.");
                }
            }
            @Override
            public void onFailure(Call<List<Item>> call, Throwable t) {
                loadingLive.postValue(false);
                errorLive.postValue("Network error.");
            }
        });
    }

    // ── Update status (Mark Returned) ─────────────────────────────────────────

    public void updateStatus(Long itemId, String newStatus,
                             String userId, String role) {
        ApiClient.get()
                .updateStatus(itemId, userId, role, new StatusUpdateRequest(newStatus))
                .enqueue(new Callback<Item>() {
                    @Override
                    public void onResponse(Call<Item> call, Response<Item> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            updatedItem.postValue(response.body());
                        } else {
                            errorLive.postValue("Could not update status.");
                        }
                    }
                    @Override
                    public void onFailure(Call<Item> call, Throwable t) {
                        errorLive.postValue("Network error.");
                    }
                });
    }

    // ── Admin delete ──────────────────────────────────────────────────────────

    public void deleteItem(Long itemId, String userId, String role) {
        ApiClient.get().deleteItem(itemId, userId, role)
                .enqueue(new Callback<Map<String, Object>>() {
                    @Override
                    public void onResponse(Call<Map<String, Object>> call,
                                           Response<Map<String, Object>> response) {
                        if (response.isSuccessful()) {
                            deletedId.postValue(itemId);
                        } else {
                            errorLive.postValue("Delete failed.");
                        }
                    }
                    @Override
                    public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                        errorLive.postValue("Network error.");
                    }
                });
    }
}
