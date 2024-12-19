package com.example.DescubrAQP;

import static androidx.core.content.ContentProviderCompat.requireContext;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.DescubrAQP.dao.building.Building;
import com.example.DescubrAQP.database.AppDatabase;

import java.util.List;

public class BuildingAdapter extends RecyclerView.Adapter<BuildingAdapter.BuildingViewHolder> {
    private AppDatabase appDatabase;
    private List<Building> buildingList;
    private OnBuildingClickListener onBuildingClickListener;

    public interface OnBuildingClickListener {
        void onBuildingClick(int position);
    }

    public BuildingAdapter(Context context,List<Building> buildingList, OnBuildingClickListener onBuildingClickListener) {
        this.buildingList = buildingList;
        this.onBuildingClickListener = onBuildingClickListener;
        this.appDatabase = AppDatabase.getInstance(context); // Inicializar AppDatabase
    }

    @NonNull
    @Override
    public BuildingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_building, parent, false);
        return new BuildingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BuildingViewHolder holder, int position) {
        Building building = buildingList.get(position);
        holder.title.setText(building.getTitle());
        // Consultar el nombre de la categoría desde la base de datos
        if (building.getCategoryId() != null) {
            new Thread(() -> {
                String categoryName = appDatabase.categoriaDao().getCategory(building.getCategoryId()).getCategoryName();
                holder.itemView.post(() -> holder.category.setText(categoryName));
            }).start();
        } else {
            holder.category.setText("Sin Categoría"); // Texto predeterminado si no hay categoría
        }
        holder.description.setText(building.getDescription());
        holder.image.setImageResource(Integer.parseInt(building.getImageResId()));

        // Manejar el clic en el elemento
        holder.itemView.setOnClickListener(v -> {
            if (onBuildingClickListener != null) {
                onBuildingClickListener.onBuildingClick(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return buildingList.size();
    }

    public static class BuildingViewHolder extends RecyclerView.ViewHolder {
        TextView title, category, description; // Añadido category
        ImageView image;

        public BuildingViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.building_title);
            category = itemView.findViewById(R.id.building_category); // Inicializar categoría
            description = itemView.findViewById(R.id.building_description);
            image = itemView.findViewById(R.id.building_image);
        }
    }
}
