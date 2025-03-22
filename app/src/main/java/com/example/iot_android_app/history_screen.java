package com.example.iot_android_app;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.core.content.ContextCompat;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link history_screen#newInstance} factory method to
 * create an instance of this fragment.
 */
public class history_screen extends Fragment {
    private RecyclerView historyList;
    private ItemAdapter adapter;
    private List<orderModel> items;
    private ArrayList<orderModel> orders;

    public history_screen() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment history_screen.
     */
    // TODO: Rename and change types and number of parameters
    public static history_screen newInstance(String param1, String param2) {
        history_screen fragment = new history_screen();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_history_screen, container, false);
        historyList = view.findViewById(R.id.historyList);
        historyList.setLayoutManager(new LinearLayoutManager(getContext()));
        DBHandler db = new DBHandler();
        items = new ArrayList<>();
        SharedPreferences prefs = getActivity().getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
        String user = prefs.getString("username", "Guest");
        orders = new ArrayList<>();

        adapter = new ItemAdapter(items, new ItemAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                orderModel clickedItem = items.get(position);
                
                new Thread(() -> {
                    db.sendMyFavourite(clickedItem.getType(), clickedItem.getShots(),
                            clickedItem.getSugar(), clickedItem.getTemp());
                }).start();
                Toast.makeText(getContext(), "Added to favourites", Toast.LENGTH_SHORT).show();
            }
        });

        new Thread(() -> {
            String historyJSON = db.getHistory(user);
            try {
                JSONArray array = new JSONArray(historyJSON);
                orders.clear();
                for (int i = 0; i < array.length(); i++) {
                    JSONObject curObject = array.getJSONObject(i);
                    String date = curObject.get("orderTime").toString().substring(5, 16);
                    String type = curObject.getString("type");
                    int shots = curObject.getInt("strength");
                    int sugar = curObject.getInt("sugar");
                    int temperature = curObject.getInt("temperature");
                    orders.add(new orderModel(date, type, shots, sugar, temperature));
                }
                for (orderModel o: orders)
                    items.add(o);

                if (getActivity() == null) return;
                getActivity().runOnUiThread(() -> adapter.notifyDataSetChanged());
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }).start();

        historyList.setAdapter(adapter);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(historyList.getContext(),
                DividerItemDecoration.VERTICAL);
        dividerItemDecoration.setDrawable(ContextCompat.getDrawable(getContext(), R.drawable.divider));
        historyList.addItemDecoration(dividerItemDecoration);

        return view;
    }

    private class orderModel {
        private String type;
        private int shots;
        private int sugar;
        private int temp;
        private String date;

        public orderModel(String date, String type, int shots, int sugar, int temp) {
            this.type = type;
            this.date = date;
            this.shots = shots;
            this.sugar = sugar;
            this.temp = temp;
        }

        @Override
        public String toString() {
            return date + " - " + type + " x" + shots + " (sugar: " +
                    sugar + ") (T: " + temp + ")";
        }

        public int getShots() {
            return shots;
        }

        public int getSugar() {
            return sugar;
        }

        public int getTemp() {
            return temp;
        }

        public String getDate() {
            return date;
        }

        public String getType() {
            return type;
        }
    }

    public static class ItemAdapter extends RecyclerView.Adapter<ItemAdapter.ItemViewHolder> {

        private List<orderModel> itemList;
        private OnItemClickListener listener;

        public interface OnItemClickListener {
            void onItemClick(View view, int position);
        }

        public ItemAdapter(List<orderModel> itemList, OnItemClickListener listener) {
            this.itemList = itemList;
            this.listener = listener;
        }

        @NonNull
        @Override
        public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.list_item, parent, false);
            return new ItemViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ItemViewHolder holder, int position) {
            String text = itemList.get(position).toString();

            TextView textView = holder.textView;
            textView.setText(text);
            textView.setMaxLines(2);
            textView.setEllipsize(TextUtils.TruncateAt.END);
        }

        @Override
        public int getItemCount() {
            return itemList.size();
        }

        public class ItemViewHolder extends RecyclerView.ViewHolder {
            TextView textView;

            public ItemViewHolder(View itemView) {
                super(itemView);
                textView = itemView.findViewById(R.id.pastDrink); // Get reference to TextView

                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        listener.onItemClick(v, getAdapterPosition());
                    }
                });
            }
        }
    }
}