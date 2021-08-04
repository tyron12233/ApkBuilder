package com.tyron.compiler.util;

import com.apk.builder.logger.Logger;
import com.apk.builder.model.Project;
import com.tyron.server.DefaultLanguageClient;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.File;
import java.net.Socket;
import java.net.ServerSocket;

import org.eclipse.lsp4j.jsonrpc.Launcher;
import org.eclipse.lsp4j.launch.LSPLauncher;
import org.eclipse.lsp4j.services.LanguageServer;
import org.eclipse.lsp4j.*;

public class LanguageServerLauncher {
    
    private static final String TAG = LanguageServer.class.getSimpleName();
    
    Project mProject;
    
    private ServerSocket mSocketServer;
    private Socket mSocket;
    
    private LanguageServer mServer;
    
    public LanguageServerLauncher (Project project) {
        mProject = project;
    }
    
    private static LanguageServerLauncher Instance = null;
    
    public static LanguageServerLauncher getInstance() {
        return Instance;
    }
    
    public void startSocket(int port) throws IOException {
        mSocketServer = new ServerSocket(port);
        mProject.getLogger().d(TAG, "Server started on port: " + port);
    }
    
    public void startListening() throws IOException {
        if (mSocketServer == null) {
          throw new IllegalStateException("Server socket is not initialized");
        }
        
        Instance = this;
        
        mProject.getLogger().d(TAG, "Listening for incoming connections.");
        mSocket = mSocketServer.accept();
        mProject.getLogger().d(TAG, "Connected successfully");
        
        Thread thread = new Thread() {
            @Override
            public void run() {
                try {
                    InputStream input = mSocket.getInputStream();
                    OutputStream out = mSocket.getOutputStream();
                                       
                    Launcher<LanguageServer> launcher = LSPLauncher.createClientLauncher(new DefaultLanguageClient(mProject),
                            input,
                            out);
                    
                    mServer = launcher.getRemoteProxy();
                    launcher.startListening();
                    
                    mServer.initialize(getInitParams());
                } catch (IOException e) {
                    mProject.getLogger().e(TAG, e.getMessage());
                }
            }
        };
        thread.start();
    }
    
    public LanguageServer getServer() {
        return mServer;
    }
    
    private InitializeParams getInitParams() {
        InitializeParams initParams = new InitializeParams();
        
        initParams.setRootUri(new File("/sdcard/.1TapSlide/GradleTest/").toURI().toString());
        
        WorkspaceClientCapabilities workspaceClientCapabilities = new WorkspaceClientCapabilities();
        workspaceClientCapabilities.setApplyEdit(true);
        workspaceClientCapabilities.setDidChangeWatchedFiles(new DidChangeWatchedFilesCapabilities());
        workspaceClientCapabilities.setExecuteCommand(new ExecuteCommandCapabilities());
        workspaceClientCapabilities.setWorkspaceEdit(new WorkspaceEditCapabilities());
//        workspaceClientCapabilities.setSymbol(new SymbolCapabilities());
        workspaceClientCapabilities.setWorkspaceFolders(true);
        workspaceClientCapabilities.setConfiguration(true);

        TextDocumentClientCapabilities textDocumentClientCapabilities = new TextDocumentClientCapabilities();
        textDocumentClientCapabilities.setCodeAction(new CodeActionCapabilities());
        textDocumentClientCapabilities.setCompletion(new CompletionCapabilities(new CompletionItemCapabilities(false)));
        textDocumentClientCapabilities.setDefinition(new DefinitionCapabilities());
        textDocumentClientCapabilities.setReferences(new ReferencesCapabilities());
        textDocumentClientCapabilities.setRename(new RenameCapabilities());
       // textDocumentClientCapabilities.setSemanticHighlightingCapabilities(new SemanticHighlightingCapabilities(false));
        textDocumentClientCapabilities.setSignatureHelp(new SignatureHelpCapabilities());
        textDocumentClientCapabilities.setSynchronization(new SynchronizationCapabilities(true, true, true));
        initParams.setCapabilities(
            new ClientCapabilities(
                workspaceClientCapabilities,
                textDocumentClientCapabilities,
                null
            )
        );
        return initParams;
    }
}