package com.tyron.ide.editor;

import android.content.Context;
import io.github.rosemoe.editor.widget.CodeEditor;
import java.io.File;

import java.lang.reflect.Field;

public class CustomCodeEditor extends CodeEditor {

    private File path;
    private CustomTextAnalyzer analyzer;
    
    public CustomCodeEditor(Context context) {
        super(context);
        
        try {
            Field completionField = CodeEditor.class
                    .getDeclaredField("mCompletionWindow");
            completionField.setAccessible(true);
            completionField.set(this, new CustomAutoCompleteWindow(this));
            
            Field spannerField = CodeEditor.class
                    .getDeclaredField("mSpanner");
            spannerField.setAccessible(true);
            spannerField.set(this, analyzer = new CustomTextAnalyzer());
        } catch (Exception ignore) {
        
        }
        
    }
    
    @Override
    public void setText(CharSequence text) {
        super.setText(text);
        try {
            Field spannerField = CodeEditor.class
                        .getDeclaredField("mSpanner");
            spannerField.setAccessible(true);
            spannerField.set(this, analyzer);
        } catch (Exception ignore) {}
    }
    
    public void setPath(File file) {
        path = file;
    }
    
    public File getPath() {
        return path;
    }
    
    public CustomTextAnalyzer getCustomTextAnalyzer() {
        return analyzer;
    }
}