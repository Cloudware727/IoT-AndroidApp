package com.example.iot_android_app;

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
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class history_screen extends Fragment {
    private RecyclerView historyList;
    private ItemAdapter adapter;
    private List<orderModel> items;
    private ArrayList<orderModel> orders;
    private int disableThr = 5;

    public history_screen() {
        // Required empty public constructor
    }

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
        View buttonsView = inflater.inflate(R.layout.list_item, container, false);
        historyList = view.findViewById(R.id.historyList);

        historyList.setLayoutManager(new LinearLayoutManager(getContext()));

        DBHandler db = new DBHandler();
        items = new ArrayList<>();
        //SharedPreferences prefs = getActivity().getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
        //String user = prefs.getString("username", "Guest");
        String user = "shlok";
        orders = new ArrayList<>();

        adapter = new ItemAdapter(items, new ItemAdapter.OnItemClickListener() {
            @Override
            public void favClick(View view, int position) {
                orderModel clickedItem = items.get(position);
                new Thread(() -> {
                    db.sendMyFavourite(clickedItem.getType(), clickedItem.getShots(),
                            clickedItem.getSugar(), clickedItem.getTemp());
                }).start();
                Toast.makeText(getActivity(), "Added to favorites!", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void reClick(View view, int position) {
                orderModel clickedItem = items.get(position);
                new Thread(() -> {
                    String settingsJSON = db.getSettings();
                    try {
                        JSONArray array = new JSONArray(settingsJSON);
                        for (int i = 0; i < 3; i++) {
                            JSONObject curObject = array.getJSONObject(i);
                            String neededType = clickedItem.getType();
                            String curType = curObject.getString("name");
                            if (curType.equals(neededType) &&
                                    curObject.getInt("level") > disableThr) {
                                BrewConfiguration drink =
                                        new BrewConfiguration(curObject.getInt("dispenser"),
                                        clickedItem.getType(), clickedItem.getShots(),
                                        clickedItem.getSugar(), clickedItem.getTemp());
                                drink.sendOrder(getActivity());
                                return;
                            }
                        }

                        if (getActivity()==null) return;
                        getActivity().runOnUiThread(() -> {
                            adapter.notifyDataSetChanged();
                        });
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }).start();
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
                    boolean canBeOrdered = db.canBeOrdered(type);
                    boolean isFav = db.isFavorite(user, type, shots, sugar, temperature);
                    orders.add(new orderModel(date, type, shots, sugar, temperature, canBeOrdered, isFav));
                }
                items.clear();
                items.addAll(orders);

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

        /*//checks if user is logged in
        prefs = requireActivity().getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
        Boolean isLoggedIn = prefs.getBoolean("isLoggedIn", false);

        if (isLoggedIn == false) {
            requireActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.frame_layout, new BlockedScreen())
                    .commit();
            return new View(requireContext());
        } else {

            // Inflate the layout for this fragment
            return view;
        }*/
        return view;
    }

    private class orderModel {
        private String type;
        private int shots;
        private int sugar;
        private int temp;
        private String date;
        private boolean canBeOrdered;
        private boolean isFavorite;

        public orderModel(String date, String type, int shots, int sugar, int temp, boolean cbo, boolean fav) {
            this.type = type;
            this.date = date;
            this.shots = shots;
            this.sugar = sugar;
            this.temp = temp;
            canBeOrdered = cbo;
            isFavorite = fav;
        }

        @Override
        public String toString() {
            return date + '\n' + type + " (" + shots + " shot" + (shots == 1?")":"s)") +
                    "\nSugar level: " + sugar + "\nTemperature: " + temp + "\u00B0C";
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

        public boolean isCanBeOrdered() {
            return canBeOrdered;
        }

        public boolean isFavorite() {
            return isFavorite;
        }

        public void setFavorite(boolean favorite) {
            isFavorite = favorite;
        }
    }

    public static class ItemAdapter extends RecyclerView.Adapter<ItemAdapter.ItemViewHolder> {

        private List<orderModel> itemList;
        private OnItemClickListener listener;

        public interface OnItemClickListener {
            void favClick(View view, int position);
            void reClick(View view, int position);
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
            orderModel curItem = itemList.get(position);

            TextView textView = holder.textView;
            textView.setText(curItem.toString());
            textView.setMaxLines(5);
            textView.setEllipsize(TextUtils.TruncateAt.END);
            holder.buttonRe.setEnabled(curItem.isCanBeOrdered());
            if (curItem.isFavorite())
                holder.buttonFav.setCompoundDrawablesWithIntrinsicBounds(R.drawable.save_to_fav_icon, 0, 0, 0);
            else
                holder.buttonFav.setCompoundDrawablesWithIntrinsicBounds(R.drawable.remove_from_fav, 0, 0, 0);
        }

        @Override
        public int getItemCount() {
            return itemList.size();
        }

        public class ItemViewHolder extends RecyclerView.ViewHolder {
            TextView textView;
            Button buttonFav;
            Button buttonRe;

            public ItemViewHolder(View itemView) {
                super(itemView);
                textView = itemView.findViewById(R.id.pastDrink);
                buttonFav = itemView.findViewById(R.id.historyFav);
                buttonRe = itemView.findViewById(R.id.historyRe);
                buttonFav.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        listener.favClick(v, getAdapterPosition()); // Notify button click
                    }
                });
                buttonRe.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        listener.reClick(v, getAdapterPosition()); // Notify button click
                    }
                });
            }
        }
    }
}