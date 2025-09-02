package com.joshuamemories.nearbymemoriesapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.joshuamemories.nearbymemoriesapp.R;
import com.joshuamemories.nearbymemoriesapp.Memory;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.List;

public class MemoryAdapter extends RecyclerView.Adapter<MemoryAdapter.VH> {

    public interface OnClick { void onClick(Memory m); }
    private final OnClick onClick;
    private final List<Memory> items = new ArrayList<>();

    public MemoryAdapter(OnClick onClick) { this.onClick = onClick; }

    public void submitList(List<Memory> data) {
        items.clear();
        if (data != null) items.addAll(data);
        notifyDataSetChanged();
    }

    @NonNull @Override public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_memory, parent, false);
        return new VH(v);
    }

    @Override public void onBindViewHolder(@NonNull VH h, int pos) {
        Memory m = items.get(pos);
        h.title.setText(m.title);
        h.coords.setText(String.format("Lat: %.5f, Lon: %.5f", m.latitude, m.longitude));
        h.when.setText(DateFormat.getDateTimeInstance().format(m.createdAt));
        h.itemView.setOnClickListener(v -> onClick.onClick(m));
    }

    @Override public int getItemCount() { return items.size(); }

    static class VH extends RecyclerView.ViewHolder {
        TextView title, coords, when;
        VH(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.tvTitle);
            coords = itemView.findViewById(R.id.tvCoords);
            when = itemView.findViewById(R.id.tvWhen);
        }
    }
}
