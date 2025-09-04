package com.joshuamemories.nearbymemoriesapp;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


import java.util.ArrayList;
import java.util.List;

public class MapMemoryAdapter extends RecyclerView.Adapter<MapMemoryAdapter.VH> {

    public interface OnClick { void onClick(Memory m); }
    private final OnClick onClick;
    private final List<Memory> items = new ArrayList<>();

    public MapMemoryAdapter(OnClick onClick) { this.onClick = onClick; }

    public void submit(List<Memory> list) {
        items.clear();
        if (list != null) items.addAll(list);
        notifyDataSetChanged();
    }

    @NonNull @Override public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_map_memory, parent, false);
        return new VH(v);
    }

    @Override public void onBindViewHolder(@NonNull VH h, int pos) {
        Memory m = items.get(pos);
        h.title.setText(m.title != null ? m.title : "Untitled");
        String sub = String.format("Lat %.4f, Lon %.4f", m.latitude, m.longitude);
        h.sub.setText(m.placeName != null ? m.placeName : sub);
        h.thumb.setImageResource(android.R.drawable.ic_menu_gallery); // placeholder
        h.itemView.setOnClickListener(v -> onClick.onClick(m));
    }

    @Override public int getItemCount() { return items.size(); }

    static class VH extends RecyclerView.ViewHolder {
        ImageView thumb; TextView title; TextView sub;
        VH(@NonNull View itemView) {
            super(itemView);
            thumb = itemView.findViewById(R.id.imgThumb);
            title = itemView.findViewById(R.id.tvTitle);
            sub = itemView.findViewById(R.id.tvSub);
        }
    }


}
