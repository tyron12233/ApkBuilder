package com.apk.builder.logger;

import android.content.Context;
import android.view.ViewGroup;
import android.view.View;
import android.widget.TextView;
import android.widget.FrameLayout;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.DiffUtil;
import android.text.SpannableStringBuilder;

import java.util.ArrayList;
import java.util.List;

public class LogAdapter extends RecyclerView.Adapter<LogAdapter.ViewHolder> {
    
	private final List<Log> mData = new ArrayList<>();
	
	public LogAdapter() {
		
	}
	
	public void submitList(List<Log> newData) {
	    DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new DiffUtil.Callback() {
	        @Override
	        public int getOldListSize() {
	            return mData.size();
	        }
	        
	        @Override
	        public int getNewListSize() {
	            return newData.size();
	        }
	        
	        @Override
	        public boolean areItemsTheSame(int oldPos, int newPos) {
	            return mData.get(oldPos).getMessage().equals(newData.get(newPos));
	        }
	        
	        @Override
	        public boolean areContentsTheSame(int oldPos, int newPos) {
                return mData.get(oldPos).getMessage().equals(newData.get(newPos));
	        }
	    });
	    
	    mData.clear();
	    mData.addAll(newData);
	    
	    diffResult.dispatchUpdatesTo(this);
	}
	
	@Override
	public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		return new ViewHolder(new FrameLayout(parent.getContext()));
	}
	
	@Override
	public void onBindViewHolder (ViewHolder holder, int position) {
		Log log = mData.get(position);
		
		SpannableStringBuilder sb = new SpannableStringBuilder();
		sb.append("[");
		sb.append(log.getTag());
		sb.append("]");
		sb.append(" ");
		sb.append(log.getMessage());
		
		holder.mText.setText(sb);
	}
	
	@Override
	public int getItemCount() {
		return mData.size();
	}
	
	public static class ViewHolder extends RecyclerView.ViewHolder {
	    
		public TextView mText;
		
		public ViewHolder(View view) {
			super(view);
			
			mText = new TextView(view.getContext());
			((ViewGroup) view).addView(mText);
		}
	}
}