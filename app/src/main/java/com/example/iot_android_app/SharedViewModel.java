package com.example.iot_android_app;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class SharedViewModel extends ViewModel {
    private final MutableLiveData<String> imagePath = new MutableLiveData<>();

    public LiveData<String> getImagePath() {
        return imagePath;
    }

    public void setImagePath(String path) {
        imagePath.setValue(path);
    }
}
