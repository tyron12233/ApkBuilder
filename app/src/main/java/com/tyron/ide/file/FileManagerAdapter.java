package com.tyron.ide.file;

import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.DiffUtil;

import com.tyron.ide.util.AndroidUtilities;
import com.tyron.ide.util.LayoutHelper;

import java.io.File;
import java.util.Arrays;

public class FileManagerAdapter extends RecyclerView.Adapter<FileManagerAdapter.ViewHolder> {
    
    public interface OnItemClickListener {
        void onItemClick(File file, int position);
    }
    
    private File mFile;
    private File[] childs;
    
    private OnItemClickListener mListener;
    
    public FileManagerAdapter(String file) {
        mFile = new File(file);
        childs = mFile.listFiles();
        if (childs != null) {
            Arrays.sort(childs);
        }
    }
    
    public void submitFile(File newFile) {
        submitFile(newFile, null);
    }
    
    public void submitFile(File newFile, Runnable callback) {
        File[] newChilds = newFile.listFiles();
        if (newChilds != null) {
            Arrays.sort(newChilds);
        }
        /*DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new DiffUtil.Callback() {
	        @Override
	        public int getOldListSize() {
	            return getArrayCount(childs);
	        }
	        
	        @Override
	        public int getNewListSize() {
	            return getArrayCount(newChilds);
	        }
	        
	        @Override
	        public boolean areItemsTheSame(int oldPos, int newPos) {
	            return childs[oldPos].equals(newChilds[newPos]);
	        }
	        
	        @Override
	        public boolean areContentsTheSame(int oldPos, int newPos) {
                return childs[oldPos].length() == childs[newPos].length();
	        }
	    }); 
	    */
	    childs = newChilds;
	    
	    notifyDataSetChanged();
	   // diffResult.dispatchUpdatesTo(this);
	    
	    if (callback != null) {
	        callback.run();
	    }
    }
    
    public void setOnItemClickListener(OnItemClickListener listener) {
        mListener = listener;
    }
    
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        FrameLayout layout = new FrameLayout(parent.getContext());
        ViewHolder holder = new ViewHolder(layout);
        
         layout.setOnClickListener((v) -> {
             int position = holder.getAdapterPosition();
             if (position == RecyclerView.NO_POSITION) {
                return;
             }
             if (mListener != null) {
                mListener.onItemClick(childs[position], position);
             }
         });
        return holder;
    }
    
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        File file = childs[position];
        holder.bind(file);
    }
    
    @Override
    public int getItemCount() {
        return getArrayCount(childs);
    }
    
    private int getArrayCount(File[] file) {
        if (file == null) {
            return 0;
        }
        return file.length;
    }
    
    public static class ViewHolder extends RecyclerView.ViewHolder {
        
        private final LinearLayout root;
        private final ImageView icon;
        private final TextView fileName;
        
        public ViewHolder(View view) {
            super(view);
            FrameLayout parent = (FrameLayout) view;
            
            root = new LinearLayout(view.getContext());
            root.setPadding(AndroidUtilities.dp(8), AndroidUtilities.dp(4), AndroidUtilities.dp(8), AndroidUtilities.dp(4));
            parent.addView(root, LayoutHelper.createFrame(-1, -2));
            
            icon = new ImageView(view.getContext());
            root.addView(icon, LayoutHelper.createLinear(-2, -2));
            
            fileName = new TextView(view.getContext());
            fileName.setTextSize(18);
            root.addView(fileName, LayoutHelper.createLinear(-2, -2, 1));
        }
        
        public void bind(File file) {
            fileName.setText(file.getName());
        }
    }
}