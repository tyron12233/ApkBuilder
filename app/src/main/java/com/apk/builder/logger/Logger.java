package com.apk.builder.logger;

import android.app.Activity;
import android.text.style.ForegroundColorSpan;
import android.text.Spannable;
import android.text.SpannableString;

import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelStoreOwner;

import java.util.ArrayList;
import java.util.List;

public class Logger {
	
	private boolean mAttached;
	
	private LogViewModel model;
	
	public void attach(ViewModelStoreOwner activity) {
	    model = new ViewModelProvider(activity).get(LogViewModel.class);
	    mAttached = true;
	}
	
	
	public void d(String tag, String message) {
	    if (!mAttached) {
	        return;
	    }
		add(new Log(tag, message));
	}
	
	public void e(String tag,  String message) {
	    if (!mAttached) {
	        return;
	    }		
	    Spannable messageSpan = new SpannableString(message);
	    messageSpan.setSpan(new ForegroundColorSpan(0xffff0000), 0, message.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		
		add(new Log(tag, messageSpan));						
	}
	
	public void w(String tag,  String message) {
	    if (!mAttached) {
	        return;
	    }
	    Spannable messageSpan = new SpannableString(message);
	    messageSpan.setSpan(new ForegroundColorSpan(0xffff7043), 0, message.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		
		add(new Log(tag, messageSpan)); 
	}
	
	private void add(Log log) {
	    ArrayList<Log> currentList = model.getLogs().getValue();
	    if (currentList == null) {
	        currentList = new ArrayList<>();
	    }
	    currentList.add(log);
	    model.getLogs().postValue(currentList);
	}
	
}