package com.tyron.ide.editor;

import io.github.rosemoe.editor.interfaces.AutoCompleteProvider;
import io.github.rosemoe.editor.interfaces.CodeAnalyzer;
import io.github.rosemoe.editor.text.Content;
import io.github.rosemoe.editor.text.TextAnalyzeResult;
import io.github.rosemoe.editor.text.TextAnalyzer;
import io.github.rosemoe.editor.langs.java.JavaLanguage;
import io.github.rosemoe.editor.widget.CodeEditor;

public class CustomJavaLanguage extends JavaLanguage {
    
    private CustomCodeEditor mEditor;
    
    public CustomJavaLanguage(CustomCodeEditor editor) {
        mEditor = editor;
    }
    
    @Override
    public CodeAnalyzer getAnalyzer() {
        CodeAnalyzer analyzer = (content, colors, thread) -> {
        
        };
        return analyzer;
    }
    
    @Override
    public AutoCompleteProvider getAutoCompleteProvider() {
        return new LanguageAutoComplete(mEditor);
    }
}