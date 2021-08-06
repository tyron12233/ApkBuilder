package com.tyron.ide.file;

import android.os.Bundle;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.view.LayoutInflater;
import android.view.Gravity;
import android.widget.FrameLayout;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.apk.builder.R;
import com.tyron.ide.util.AndroidUtilities;
import com.tyron.ide.editor.CodeEditorFragment;

import java.io.File;

public class FileManagerFragment extends Fragment {
    
    private FrameLayout root;
    
    private RecyclerView listView;
    private LinearLayoutManager layoutManager;
    private FileManagerAdapter adapter;
    
    /**
    * The file that is considered as the parent, the fragment will 
    * exit if the user pressed the back button and its on the rootFile
    **/
    private File rootFile;
    private File currentFile;
    
    private int lastScrollPosition;
    
    OnBackPressedCallback callback = new OnBackPressedCallback(false) {
        @Override
        public void handleOnBackPressed() {
            openDirectory(currentFile.getParentFile(), () -> {
                layoutManager.scrollToPositionWithOffset(lastScrollPosition, 0);
            });
        }
    };
    
    public static FileManagerFragment newInstance(String path) {
        FileManagerFragment fragment = new FileManagerFragment();
        Bundle args = new Bundle();
        args.putString("path", path);
        fragment.setArguments(args);
        return fragment;
    }
    
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            currentFile = new File(savedInstanceState.getString("path"));
            rootFile = new File(savedInstanceState.getString("root"));
        } else {
            currentFile = new File(getArguments().getString("path"));
            rootFile = new File(currentFile.getAbsolutePath());
        }
        
        requireActivity().getOnBackPressedDispatcher().addCallback(this, callback);
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, @Nullable Bundle savedInstanceState) {
        root = new FrameLayout(parent.getContext());
        listView = new RecyclerView(parent.getContext());
        root.addView(listView);
        return root;
    }
    
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        listView.setLayoutManager(layoutManager = new LinearLayoutManager(requireContext()));
        listView.setAdapter(adapter = new FileManagerAdapter(currentFile.getAbsolutePath()));
        adapter.setOnItemClickListener((file, position) -> {
            if (file.isDirectory()) {
                lastScrollPosition = layoutManager.findFirstCompletelyVisibleItemPosition();
                openDirectory(file);
            } else if(file.isFile()) {
                openFile(file);
            }
        });
        
        
    }
    
    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("path", currentFile.getAbsolutePath());
        outState.putString("root", rootFile.getAbsolutePath());
    }
    
    private void openDirectory(File file) {
        openDirectory(file, null);
    }
    
    private void openDirectory(File file, Runnable cb) {
        adapter.submitFile(file, cb);
        currentFile = new File(file.getAbsolutePath());
        callback.setEnabled(!file.getAbsolutePath().equals(rootFile.getAbsolutePath()));
    }
    
    private void openFile(File file) {
        if (file.getName().endsWith(".java")) {
            getParentFragmentManager().beginTransaction()
                    .addToBackStack(null)
                    .add(R.id.fragment_container, CodeEditorFragment.newInstance(file.getAbsolutePath()))
                    .commit();
        }
    }
}