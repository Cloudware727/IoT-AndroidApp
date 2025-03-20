package com.example.iot_android_app;

import static android.app.PendingIntent.getActivity;

import android.content.Context;
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

        // Set the background image dynamically for each coffee
        holder.ivBackground.setImageResource(coffee.getBg());

        // Adjust the tinted overlay height based on the coffee level (percentage)
        int coffeeLevel = coffee.getCoffeeLevel(); // Get coffee level (0 to 100)

        // Calculate the height of the tinted overlay based on the coffee level
        ViewGroup.LayoutParams params = holder.viewCoffeeFill.getLayoutParams();
        float density = holder.itemView.getContext().getResources().getDisplayMetrics().density;
        params.height = (int) Math.round((100-coffeeLevel)*2.5*density);  // 250dp is the height of the card
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

