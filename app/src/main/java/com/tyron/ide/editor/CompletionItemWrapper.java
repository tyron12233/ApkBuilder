package com.tyron.ide.editor;

import io.github.rosemoe.editor.struct.CompletionItem;

import org.eclipse.lsp4j.TextEdit;

import java.util.ArrayList;
import java.util.List;

public class CompletionItemWrapper extends CompletionItem {
    
    private List<TextEdit> additionalTextEdits;
    
    public static CompletionItemWrapper from(org.eclipse.lsp4j.CompletionItem item) {
        CompletionItemWrapper wrapper = new CompletionItemWrapper(item.getLabel(), item.getInsertText(), item.getDetail());
        
        wrapper.additionalTextEdits = item.getAdditionalTextEdits();
        return wrapper;
    }
    
    public CompletionItemWrapper(String label, String commit, String desc) {
        super(label, commit, desc);
    }
    
    public List<TextEdit> getAdditionalTextEdits() {
        return additionalTextEdits;
    }
}