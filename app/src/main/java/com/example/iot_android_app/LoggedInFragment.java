package com.example.iot_android_app;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

public class LoggedInFragment extends Fragment {

    private ImageView uploadPhoto;
    private ActivityResultLauncher<Intent> imagePickerLauncher;
    private SharedViewModel sharedViewModel;

    public LoggedInFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sharedViewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);

        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Uri selectedImageUri = result.getData().getData();

                        try (InputStream inputStream = requireContext().getContentResolver().openInputStream(selectedImageUri)) {
                            File imageFile = new File(requireContext().getFilesDir(), "dispenser_img_" + System.currentTimeMillis() + ".jpg");

                            try (OutputStream outputStream = new FileOutputStream(imageFile)) {
                                byte[] buffer = new byte[1024];
                                int length;
                                while ((length = inputStream.read(buffer)) > 0) {
                                    outputStream.write(buffer, 0, length);
                                }
                            }

                            SharedPreferences prefs = requireActivity().getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
                            int position = prefs.getInt("spinner_position", 0) - 1;
                            prefs.edit().putString("dispenser_image_path_" + position, imageFile.getAbsolutePath()).apply();

                            sharedViewModel.setImagePath(imageFile.getAbsolutePath());

                            // Optional: Show preview
                            if (uploadPhoto != null) {
                                uploadPhoto.setImageBitmap(BitmapFactory.decodeFile(imageFile.getAbsolutePath()));
                            }

                            Toast.makeText(getContext(), "Image uploaded!", Toast.LENGTH_SHORT).show();
                        } catch (Exception e) {
                            Toast.makeText(getContext(), "Failed to upload image", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
        );
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_logged_in, container, false);

        TextView tvWelcome = view.findViewById(R.id.tv_welcome);
        TextView ChooseImage = view.findViewById(R.id.tv_upload_image);
        Button btnLogout = view.findViewById(R.id.btn_logout);
        Button confirm = view.findViewById(R.id.btn_confirm_choice);
        Button uploadButton = view.findViewById(R.id.btn_upload_image);
        uploadPhoto = view.findViewById(R.id.iv_tea_preview);
        Spinner spinner = view.findViewById(R.id.spinner_dispenser);
        EditText myTextField = view.findViewById(R.id.myTextField);
        DBHandler dbHandler = new DBHandler();

        SharedPreferences prefs = requireActivity().getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
        String username = prefs.getString("username", "Guest");
        SharedPreferences.Editor editor = prefs.edit();

        tvWelcome.setText("Welcome, " + username + "!");

        myTextField.setVisibility(View.GONE);
        confirm.setVisibility(View.GONE);

        ArrayAdapter<CharSequence> adapter = new ArrayAdapter<>(getContext(), R.layout.spinner_selected_item, getResources().getStringArray(R.array.dropdown_items));
        spinner.setAdapter(adapter);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                editor.putInt("spinner_position", position);
                editor.apply();

                if (position != 0) {
                    myTextField.setVisibility(View.VISIBLE);
                    ChooseImage.setVisibility(View.VISIBLE);
                    uploadPhoto.setVisibility(View.VISIBLE);
                    uploadButton.setVisibility(View.VISIBLE);
                    confirm.setVisibility(View.VISIBLE);
                } else {
                    myTextField.setVisibility(View.GONE);
                    ChooseImage.setVisibility(View.GONE);
                    uploadPhoto.setVisibility(View.GONE);
                    uploadButton.setVisibility(View.GONE);
                    confirm.setVisibility(View.GONE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {}
        });

        uploadButton.setOnClickListener(s -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            imagePickerLauncher.launch(intent);
        });

        confirm.setOnClickListener(v -> {
            String text = myTextField.getText().toString().trim();
            if (text.isEmpty()) {
                Toast.makeText(getContext(), "Please enter a valid text.", Toast.LENGTH_SHORT).show();
                return;
            }

            text = text.replaceAll(" ", "+");
            editor.putString("selected_dispenser_text", text);
            editor.apply();

            int dispenserNumber = spinner.getSelectedItemPosition();
            String requestUrl = "https://studev.groept.be/api/a24ib2team102/ChangeTeaName/" + text + "/" + dispenserNumber;
            new Thread(() -> dbHandler.makeGETRequest(requestUrl)).start();

            Toast.makeText(getContext(), "Dispenser " + dispenserNumber + " has been changed", Toast.LENGTH_SHORT).show();
            myTextField.setText("");
        });

        btnLogout.setOnClickListener(v -> {
            Toast.makeText(getActivity(), "Logged Out!", Toast.LENGTH_SHORT).show();
            editor.clear();
            editor.apply();

            account_screen loginFragment = new account_screen();
            FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            transaction.replace(R.id.frame_layout, loginFragment);
            transaction.addToBackStack(null);
            transaction.commit();
        });

        return view;
    }
}
