package com.apk.builder.logger;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.List;

public class LogViewModel extends ViewModel {
    
    private MutableLiveData<ArrayList<Log>> logs;
    
    public MutableLiveData<ArrayList<Log>> getLogs() {
        if (logs == null) {
            logs = new MutableLiveData<ArrayList<Log>>();
        }
        return logs;
    }
}