package com.example.iot_android_app;

import android.os.Bundle;

import androidx.core.content.ContextCompat;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link history_screen#newInstance} factory method to
 * create an instance of this fragment.
 */
public class history_screen extends Fragment {
    private RecyclerView historyList;
    private ItemAdapter adapter;
    private List<ItemModel> items;

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
        // TODO: use username global parameter
        String user = "ArnoJanssens";

        historyList.setAdapter(adapter);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(historyList.getContext(),
                DividerItemDecoration.VERTICAL);
        dividerItemDecoration.setDrawable(ContextCompat.getDrawable(getContext(), R.drawable.divider));
        historyList.addItemDecoration(dividerItemDecoration);
        return view;
    }

    private class ItemModel {
        private String text;

        public ItemModel(String text) {
            this.text = text;
        }

        public String getText() {
            return text;
        }
    }

    public static class ItemAdapter extends RecyclerView.Adapter<ItemAdapter.ItemViewHolder> {

        private List<ItemModel> itemList;
        private OnItemClickListener listener;

        public interface OnItemClickListener {
            void onItemClick(View view, int position);
        }

        public ItemAdapter(List<ItemModel> itemList, OnItemClickListener listener) {
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
            String text = itemList.get(position).getText();

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