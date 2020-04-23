package com.lawrence.sweeper.ui.records;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class RecordsViewModel extends ViewModel {

    private MutableLiveData<String> mText;

    public RecordsViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is records fragment");
    }

    LiveData<String> getText() {
        return mText;
    }
}