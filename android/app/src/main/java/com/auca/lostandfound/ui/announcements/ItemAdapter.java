package com.auca.lostandfound.ui.announcements;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.auca.lostandfound.R;
import com.auca.lostandfound.model.Item;
import com.auca.lostandfound.util.UiUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * RecyclerView adapter used by Announcements, My Reports, and Admin screens.
 * Calls back to the host fragment for item clicks and optional delete actions.
 */
public class ItemAdapter extends RecyclerView.Adapter<ItemAdapter.ItemViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(Item item);
    }

    public interface OnDeleteClickListener {
        void onDeleteClick(Item item);
    }

    private final Context             context;
    private       List<Item>          items    = new ArrayList<>();
    private final OnItemClickListener clickListener;
    private       OnDeleteClickListener deleteListener;  // null = hide delete button
    private final boolean             showDelete;

    public ItemAdapter(Context context,
                       OnItemClickListener clickListener,
                       boolean showDelete,
                       OnDeleteClickListener deleteListener) {
        this.context        = context;
        this.clickListener  = clickListener;
        this.showDelete     = showDelete;
        this.deleteListener = deleteListener;
    }

    public void setItems(List<Item> newItems) {
        this.items = newItems != null ? newItems : new ArrayList<>();
        notifyDataSetChanged();
    }

    public void removeItem(long itemId) {
        for (int i = 0; i < items.size(); i++) {
            if (items.get(i).getItemId() == itemId) {
                items.remove(i);
                notifyItemRemoved(i);
                return;
            }
        }
    }

    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        int layoutId = showDelete
                ? R.layout.item_admin_card
                : R.layout.item_announcement_card;
        View view = LayoutInflater.from(context).inflate(layoutId, parent, false);
        return new ItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder holder, int position) {
        Item item = items.get(position);
        holder.bind(item);
    }

    @Override
    public int getItemCount() { return items.size(); }

    // ── ViewHolder ────────────────────────────────────────────────────────────

    class ItemViewHolder extends RecyclerView.ViewHolder {

        ItemViewHolder(@NonNull View itemView) {
            super(itemView);
        }

        void bind(Item item) {
            // Both card layouts share these IDs
            TextView tvName       = itemView.findViewById(R.id.tv_item_name);
            TextView tvReportType = itemView.findViewById(R.id.tv_report_type);
            TextView tvLocation   = itemView.findViewById(R.id.tv_location);
            TextView tvStatus     = itemView.findViewById(R.id.tv_status);

            if (tvName != null) tvName.setText(item.getName());
            if (tvReportType != null) UiUtils.applyReportTypeBadge(context, tvReportType, item.getReportType());
            if (tvLocation != null) tvLocation.setText(item.getLocation());
            if (tvStatus != null) UiUtils.applyStatusBadge(context, tvStatus, item.getStatus());

            // Announcement card extras
            TextView tvCategory    = itemView.findViewById(R.id.tv_category);
            TextView tvDescription = itemView.findViewById(R.id.tv_description);
            TextView tvDate        = itemView.findViewById(R.id.tv_date);

            if (tvCategory != null) tvCategory.setText(item.getCategory());
            if (tvDescription != null) tvDescription.setText(item.getDescription());
            if (tvDate != null) tvDate.setText(item.getReportDate());

            // Delete button (admin card only)
            View btnDelete = itemView.findViewById(R.id.btn_delete);
            if (btnDelete != null) {
                btnDelete.setVisibility(showDelete ? View.VISIBLE : View.GONE);
                btnDelete.setOnClickListener(v -> {
                    if (deleteListener != null) deleteListener.onDeleteClick(item);
                });
            }

            itemView.setOnClickListener(v -> {
                if (clickListener != null) clickListener.onItemClick(item);
            });
        }
    }
}
