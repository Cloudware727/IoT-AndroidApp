package com.example.iot_android_app;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class CoffeeCarouselAdapter extends RecyclerView.Adapter<CoffeeCarouselAdapter.ViewHolder> {

    private List<Coffee> coffeeList;

    public CoffeeCarouselAdapter(List<Coffee> coffeeList) {
        this.coffeeList = coffeeList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_coffee_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Coffee coffee = coffeeList.get(position);
        holder.tvCoffeeName.setText(coffee.getName());

        String path = coffee.getImagePath();
        if (path != null) {
            Bitmap bitmap = BitmapFactory.decodeFile(path);
            if (bitmap != null) {
                holder.ivBackground.setImageBitmap(bitmap);
            } else {
                holder.ivBackground.setImageResource(coffee.getImageResId());
            }
        } else {
            holder.ivBackground.setImageResource(coffee.getImageResId());
        }


        // Adjust the tinted overlay height based on the coffee level (0 to 100)
        int coffeeLevel = coffee.getCoffeeLevel();
        ViewGroup.LayoutParams params = holder.viewCoffeeFill.getLayoutParams();
        float density = holder.itemView.getContext().getResources().getDisplayMetrics().density;
        params.height = (int) Math.round((100 - coffeeLevel) * 2.5 * density);
        holder.viewCoffeeFill.setLayoutParams(params);
    }

    @Override
    public int getItemCount() {
        return coffeeList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvCoffeeName;
        View viewCoffeeFill;
        ImageView ivBackground;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCoffeeName = itemView.findViewById(R.id.tvCoffeeName);
            viewCoffeeFill = itemView.findViewById(R.id.viewCoffeeFill);
            ivBackground = itemView.findViewById(R.id.ivBackground);
        }
    }
}
