package com.tyron.ide.editor;

import io.github.rosemoe.editor.widget.EditorAutoCompleteWindow;
import io.github.rosemoe.editor.widget.EditorCompletionAdapter;
import io.github.rosemoe.editor.interfaces.AutoCompleteProvider;
import io.github.rosemoe.editor.struct.CompletionItem;
import io.github.rosemoe.editor.text.CharPosition;
import io.github.rosemoe.editor.text.Cursor;
import io.github.rosemoe.editor.text.TextAnalyzeResult;
import android.widget.ListView;
import org.eclipse.lsp4j.TextEdit;
import org.eclipse.lsp4j.Range;

import com.tyron.ide.util.AndroidUtilities;

import java.util.ArrayList;
import java.util.List;
import java.lang.reflect.Field;

public class CustomAutoCompleteWindow extends EditorAutoCompleteWindow {
    
    private CustomCodeEditor mEditor;
    private ListView mListView;
    
    public CustomAutoCompleteWindow(CustomCodeEditor editor) {
        super(editor);
        
        mEditor = editor;
        
        try {
            Field field = EditorAutoCompleteWindow.class
                    .getDeclaredField("mListView");
            field.setAccessible(true);
            mListView = (ListView) field.get(this);
        } catch (Exception e) {
            throw new RuntimeException("Unable to get ListView field, did you update the library?");
        }
    }
    
    @Override
    public void select(int pos) {
        super.select(pos);
        CompletionItemWrapper item = (CompletionItemWrapper) ((EditorCompletionAdapter) mListView.getAdapter()).getItem(pos);
        Cursor cursor = mEditor.getCursor();
        if (!cursor.isSelected()) {        
            List<TextEdit> additionalTextEdits = item.getAdditionalTextEdits();
            
            if (additionalTextEdits != null) {
                for (TextEdit edit : additionalTextEdits) {
                    String textToInsert = edit.getNewText();
                    Range range = edit.getRange();
                    
                    mEditor.getText().replace(range.getStart().getLine(), range.getStart().getCharacter(), range.getEnd().getLine(), range.getEnd().getCharacter(), textToInsert);              
                }
            }
        }
    }
}