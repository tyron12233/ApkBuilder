package com.tyron.server;

import android.os.Handler;
import android.os.Looper;

import com.apk.builder.model.Project;

import java.util.concurrent.CompletableFuture;

import org.eclipse.lsp4j.services.LanguageClient;
import org.eclipse.lsp4j.ApplyWorkspaceEditParams;
import org.eclipse.lsp4j.ApplyWorkspaceEditResponse;
import org.eclipse.lsp4j.ConfigurationParams;
import org.eclipse.lsp4j.LogTraceParams;
import org.eclipse.lsp4j.MessageActionItem;
import org.eclipse.lsp4j.MessageParams;
import org.eclipse.lsp4j.ProgressParams;
import org.eclipse.lsp4j.PublishDiagnosticsParams;
import org.eclipse.lsp4j.RegistrationParams;
import org.eclipse.lsp4j.ShowDocumentParams;
import org.eclipse.lsp4j.ShowDocumentResult;
import org.eclipse.lsp4j.ShowMessageRequestParams;
import org.eclipse.lsp4j.UnregistrationParams;
import org.eclipse.lsp4j.WorkDoneProgressCreateParams;
import org.eclipse.lsp4j.WorkspaceFolder;
import org.eclipse.lsp4j.jsonrpc.services.JsonNotification;
import org.eclipse.lsp4j.jsonrpc.services.JsonRequest;

import java.io.File;
import java.util.Map;
import java.util.HashMap;
import java.net.URI;

public class DefaultLanguageClient implements LanguageClient {
    
    public static final String TAG = "LanguageClient";
    
    private Project mProject;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    
    private final Map<File, PublishDiagnosticsListener> publishDiagnosticsListeners = new HashMap<>();
    
    public interface PublishDiagnosticsListener {
        void onPublishDiagnostics(PublishDiagnosticsParams params);
    }
    
    public DefaultLanguageClient(Project project) {
        mProject = project;
    }
    
    @Override
    public void telemetryEvent(Object object) {
    
    }
    
    public void addPublishDiagnosticsListener(File file, PublishDiagnosticsListener listener) {
        publishDiagnosticsListeners.put(file, listener);
    }
    
    public void removePublishDiagnosticsListener(File file) {
        publishDiagnosticsListeners.remove(file);
    }
    
    @Override
    public void publishDiagnostics(PublishDiagnosticsParams params) {
        mainHandler.post(() -> {
            try {
                File reportedFile = new File(new File(new URI(params.getUri())).getAbsolutePath());
                PublishDiagnosticsListener listener = publishDiagnosticsListeners.get(reportedFile);
                if (listener != null) {
                    listener.onPublishDiagnostics(params);
                } else {
                    mProject.getLogger().e(TAG, "Received diagnostics for uri " + reportedFile + " but no listeners we're attached");
                }
            } catch (Exception ignore) {
            
            }
        });
    }
    
    @Override
    public void showMessage(MessageParams message) {
      //  mProject.getLogger().d("LanguageServer", message.getMessage());
    }
    
    @Override
    public void logMessage(MessageParams message) {
       // mProject.getLogger().d("LanguageServer", message.getMessage());
    }
    
    @Override
    public CompletableFuture<MessageActionItem> showMessageRequest(ShowMessageRequestParams requestParams) {
        return null;
    }
    
    
}