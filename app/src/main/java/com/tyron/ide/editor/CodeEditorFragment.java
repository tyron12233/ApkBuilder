package com.tyron.ide.editor;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.EditText;
import android.text.Selection;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.UnderlineSpan;
import androidx.fragment.app.Fragment;

import com.apk.builder.FileUtil;
import com.tyron.compiler.util.LanguageServerLauncher;
import com.tyron.ide.util.AndroidUtilities;

import io.github.rosemoe.editor.widget.CodeEditor;
import io.github.rosemoe.editor.struct.Span;
import io.github.rosemoe.editor.text.TextAnalyzeResult;
import io.github.rosemoe.editor.text.LineNumberCalculator;

import io.github.rosemoe.editor.interfaces.EditorEventListener;

import java.io.File;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;

import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.eclipse.lsp4j.*;

public class CodeEditorFragment extends Fragment {
    
    private FrameLayout root;
    private CustomCodeEditor codeView;
    
    public static CodeEditorFragment newInstance(String path) {
        Bundle args = new Bundle();
        args.putString("path", path);
        CodeEditorFragment fragment = new CodeEditorFragment();
        fragment.setArguments(args);
        return fragment;
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        root = new FrameLayout(parent.getContext());
        
        codeView = new CustomCodeEditor(parent.getContext());
        root.addView(codeView);
        
        return root;
    }
    
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        String path = getArguments().getString("path", null);
        setText(path);
        
        codeView.setPath(new File(path));
        codeView.setEditorLanguage(new CustomJavaLanguage(codeView));
        codeView.setEventListener(new EditorEventListener() {
            
            @Override
            public boolean onRequestFormat(CodeEditor editor, boolean async) {
                return true;
            }
            
            @Override
            public boolean onFormatFail(CodeEditor editor, Throwable cause) {
                return true;
            }
            
            @Override
            public void onFormatSucceed(CodeEditor editor) {
            
            }
            
            @Override
            public void onNewTextSet(CodeEditor editor) {
            
            }
            
            @Override
            public void afterDelete(CodeEditor editor, CharSequence content, int startLine, int startColumn, int endLine, int endColumn, CharSequence deletedContent) {
                notifyItemChanged(startLine, endLine, startColumn, endColumn, deletedContent.length(), content);
            }
            
            @Override
            public void afterInsert(CodeEditor editor, CharSequence content, int startLine, int startColumn, int endLine, int endColumn, CharSequence insertedContent) {
                 notifyItemChanged(startLine, endLine, startColumn, endColumn, insertedContent.length(), content);
              // android.widget.Toast.makeText(editor.getContext(), content, android.widget.Toast.LENGTH_LONG).show();
            }
            
            @Override
            public void beforeReplace(CodeEditor editor, CharSequence content) {
                
            }
            
            private void notifyItemChanged(int startLine, int endLine, int startColumn, int endColumn, int length, CharSequence text) {
                new Thread(new Runnable() {
                    @Override 
                    public void run() {
                        LanguageServerLauncher.getInstance().getServer()
                        .getTextDocumentService()
                                .didChange(
                                    new DidChangeTextDocumentParams(
                                        new VersionedTextDocumentIdentifier(
                                            codeView.getPath().toURI().toString(),
                                            12
                                        ),
                                        new ArrayList<>(Arrays.asList(
                                            new TextDocumentContentChangeEvent(                                                                                                                                                                                       
                                                text.toString()
                                            )
                                        ))
                                    )
                                );
                    }
                }).start();
            
                
            }
        });
        
        LanguageServerLauncher.getInstance().getLanguageClient()
                .addPublishDiagnosticsListener(codeView.getPath(), (params) -> {
                    TextAnalyzeResult result = new TextAnalyzeResult();
                    result.addNormalIfNull();
                    
                    LineNumberCalculator helper = new LineNumberCalculator(codeView.getText());
                    
                    List<Diagnostic> diagnostics = params.getDiagnostics();
                    
                    for (Diagnostic diagnostic : diagnostics) {
                        Diagnostic diagnostic = diagnostics.get(i);
                        DiagnosticSeverity severity = diagnostic.getSeverity();
                        Range range = diagnostic.getRange();
                        Position start = range.getStart();
                        Position end = range.getEnd();
                        
                        Span startSpan = Span.obtain(start.getCharacter(), 24);
                        Span endSpan = Span.obtain(end.getCharacter(), 5);
                        
                        
                        switch (severity) {
                            case Error :
                                
                                startSpan.setUnderlineColor(0xffff0000);
                                result.add(start.getLine(), startSpan);
                                result.add(end.getLine(), endSpan);
                                break;
                            case Warning :
                                startSpan.setUnderlineColor(0xffffff00);                      
                                endSpan.setUnderlineColor(0xffffff00);
                                result.add(start.getLine(), startSpan);
                                break;
                        }
                    }
                    result.determine(helper.getLine());
                    codeView.getCustomTextAnalyzer().analyzeFromServer(result);                                                            
                });
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();
        
        LanguageServerLauncher.getInstance().getLanguageClient()
                .removePublishDiagnosticsListener(codeView.getPath());
                
        ExecutorService service = Executors.newSingleThreadExecutor();
        service.submit(new Runnable() {
            @Override 
            public void run() {
                LanguageServerLauncher.getInstance().getServer()
                .getTextDocumentService()
                        .willSave(
                            new WillSaveTextDocumentParams(
                                new TextDocumentIdentifier(new File(getArguments().getString("path")).toURI().toString()),
                                TextDocumentSaveReason.Manual
                            )
                        );
                        
                FileUtil.writeFile(getArguments().getString("path"), codeView.getText().toString());
                LanguageServerLauncher.getInstance().getServer()
                        .getTextDocumentService()
                            .didSave(
                                new DidSaveTextDocumentParams(
                                    new TextDocumentIdentifier(new File(getArguments().getString("path")).toURI().toString())
                                )
                            );                
                            
                LanguageServerLauncher.getInstance().getServer()
                .getTextDocumentService()
                        .didClose(
                            new DidCloseTextDocumentParams(
                                new TextDocumentIdentifier(new File(getArguments().getString("path")).toURI().toString())
                            )
                        );                                                             
            }
        });                
    }
    
    private void setText(String path) {
        ExecutorService service = Executors.newSingleThreadExecutor();
        service.submit(new Runnable() {
            @Override 
            public void run() {
                String contents = FileUtil.readFile(path);
                
                LanguageServerLauncher.getInstance().getServer()
                            .getTextDocumentService()
                                    .didOpen(
                                        new DidOpenTextDocumentParams(
                                            new TextDocumentItem(new File(path).toURI().toString(),
                                                    "java",
                                                    1,
                                                    contents
                                            )
                                        )
                                    );
                codeView.post(() -> {
                    codeView.setText(contents);                   
                });                                                       
            }
        });
    }
    
    
    
}