package com.tyron.ide.editor;

import com.tyron.compiler.util.LanguageServerLauncher;

import io.github.rosemoe.editor.text.TextAnalyzeResult;
import io.github.rosemoe.editor.interfaces.AutoCompleteProvider;
import io.github.rosemoe.editor.struct.CompletionItem;
import io.github.rosemoe.editor.widget.CodeEditor;

import java.io.File;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.eclipse.lsp4j.CompletionParams;
import org.eclipse.lsp4j.CompletionList;
import org.eclipse.lsp4j.TextDocumentIdentifier;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.TextDocumentContentChangeEvent;
import org.eclipse.lsp4j.VersionedTextDocumentIdentifier;
import org.eclipse.lsp4j.DidChangeTextDocumentParams;
import org.eclipse.lsp4j.Range;

public class LanguageAutoComplete implements AutoCompleteProvider {
    
    private CustomCodeEditor mEditor;
    
    public LanguageAutoComplete(CustomCodeEditor editor) {
        mEditor = editor;
    }
    
    @Override
    public List<CompletionItem> getAutoCompleteItems(String prefix, boolean isInCodeBlock, TextAnalyzeResult colors, int line) {
        List<CompletionItem> keywords = new ArrayList<>();
        int column = mEditor.getCursor().getLeftColumn();
        
        try {
            CompletableFuture<Either<List<org.eclipse.lsp4j.CompletionItem>, CompletionList>> future = LanguageServerLauncher.getInstance().getServer()
                    .getTextDocumentService()
                            .completion(
                                new CompletionParams(
                                    new TextDocumentIdentifier(mEditor.getPath().toURI().toString()),
                                    new Position(line, column)
                                )
                            );
                            
            Either<List<org.eclipse.lsp4j.CompletionItem>, CompletionList> either = future.get();
            String[] string = new String[]{""};
            
            List<org.eclipse.lsp4j.CompletionItem> items = new ArrayList<>();
            
            if (either.getLeft() != null) {             
                items.addAll(either.getLeft());
            } else {
                items.addAll(either.getRight().getItems());
            }
            
            for (org.eclipse.lsp4j.CompletionItem item : items) {
                keywords.add(CompletionItemWrapper.from(item));
            }           
            
        } catch (Exception ignore) {}
        
        return keywords;
    }
    
}