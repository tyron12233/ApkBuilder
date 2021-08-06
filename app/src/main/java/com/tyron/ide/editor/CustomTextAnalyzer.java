package com.tyron.ide.editor;

import android.util.Log;

import io.github.rosemoe.editor.text.TextAnalyzer;
import io.github.rosemoe.editor.text.TextAnalyzeResult;
import io.github.rosemoe.editor.text.Content;
import io.github.rosemoe.editor.interfaces.CodeAnalyzer;
import io.github.rosemoe.editor.struct.BlockLine;
import io.github.rosemoe.editor.struct.Span;

public class CustomTextAnalyzer extends TextAnalyzer {
    
    
    private TextAnalyzeResult mResult;
    private Callback mCallback;
    
    public CustomTextAnalyzer() {
        super((content, colors, thread) -> {
        
        });
        mResult = new TextAnalyzeResult();
        mResult.addNormalIfNull();
    }
    
    @Override
    public synchronized void analyze(Content text) {
        //do nothing, analysis is done on the language server
    }
    
    @Override
    public void setCallback(Callback callback) {
        mCallback = callback;
    }
    
    public synchronized void analyzeFromServer(TextAnalyzeResult result) {
        mResult = result;
        if (mCallback != null) {
            mCallback.onAnalyzeDone(CustomTextAnalyzer.this);
        }
    }
    
    @Override
    public TextAnalyzeResult getResult() {
        return mResult;
    }
}