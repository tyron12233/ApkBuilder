package com.tyron.ide.editor;

import android.content.Context;
import io.github.rosemoe.editor.widget.CodeEditor;
import java.io.File;
public class CustomCodeEditor extends CodeEditor {

    private File path;
    
    public CustomCodeEditor(Context context) {
        super(context);
    }
    
    public void setPath(File file) {
        path = file;
    }
    
    public File getPath() {
        return path;
    }
}