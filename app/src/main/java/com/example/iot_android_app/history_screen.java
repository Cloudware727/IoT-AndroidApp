package com.example.iot_android_app;

import android.os.Bundle;

import androidx.core.content.ContextCompat;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class history_screen extends Fragment {
    private RecyclerView historyList;
    private ItemAdapter adapter;
    private List<orderModel> items;
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
        /*SharedPreferences prefs = getActivity().getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
        String user = prefs.getString("username", "Guest");*/
        String user = "shlok";
        loadHistory(db, user);
        db.startSettingsUpdater();
        adapter = new ItemAdapter(items, new ItemAdapter.OnItemClickListener() {
            @Override
            public void favClick(View view, int position) {
                orderModel clickedItem = items.get(position);
                new Thread(() -> {
                    boolean isNowFavorite = db.switchFavorite(user, clickedItem.getType(),
                            clickedItem.getShots(), clickedItem.getSugar(), clickedItem.getTemp());
                    for (int i = 0; i < items.size(); i++) {
                        orderModel item = items.get(i);
                        if (item.getType().equals(clickedItem.getType()) &&
                                item.getShots() == clickedItem.getShots() &&
                                item.getSugar() == clickedItem.getSugar() &&
                                item.getTemp() == clickedItem.getTemp()) {
                            item.setFavorite(isNowFavorite);
                        }
                    }

                    if (getActivity() == null) return;
                    getActivity().runOnUiThread(() -> adapter.notifyDataSetChanged()); // Refresh entire list
                }).start();
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

                        if (getActivity() == null) return;
                        getActivity().runOnUiThread(() -> adapter.notifyDataSetChanged());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }).start();
                new Handler().postDelayed(() ->{
                            db.saveMachineOrderId(getActivity(), getContext());
                        }, 1000
                );
            }
        });

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

    private void loadHistory(DBHandler db, String user) {
        new Thread(() -> {
            String historyJSON = db.getHistory(user);
            List<orderModel> newOrders = new ArrayList<>();

            try {
                JSONArray array = new JSONArray(historyJSON);
                for (int i = 0; i < array.length(); i++) {
                    JSONObject curObject = array.getJSONObject(i);
                    newOrders.add(new orderModel(
                            curObject.getString("orderTime").substring(5, 16),
                            curObject.getString("type"),
                            curObject.getInt("strength"),
                            curObject.getInt("sugar"),
                            curObject.getInt("temperature"),
                            db.canBeOrdered(curObject.getString("type")),
                            db.isFavorite(user, curObject.getString("type"),
                                    curObject.getInt("strength"),
                                    curObject.getInt("sugar"),
                                    curObject.getInt("temperature"))
                    ));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            if (getActivity() == null) return;
            getActivity().runOnUiThread(() -> {
                items.clear();
                items.addAll(newOrders);
                adapter.notifyDataSetChanged();
            });
        }).start();
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
            holder.buttonRe.setEnabled(curItem.CanBeOrdered());
            holder.buttonRe.setCompoundDrawablesWithIntrinsicBounds(curItem.getReIcon(), 0, 0, 0);
            holder.buttonFav.setCompoundDrawablesWithIntrinsicBounds(curItem.getFavIcon(), 0, 0, 0);
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