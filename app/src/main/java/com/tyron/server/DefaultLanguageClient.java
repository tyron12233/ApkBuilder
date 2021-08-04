package com.tyron.server;

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

public class DefaultLanguageClient implements LanguageClient {
    
    private Project mProject;
    
    public DefaultLanguageClient(Project project) {
        mProject = project;
    }
    
    @Override
    public void telemetryEvent(Object object) {
    
    }
    
    @Override
    public void publishDiagnostics(PublishDiagnosticsParams params) {
    
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