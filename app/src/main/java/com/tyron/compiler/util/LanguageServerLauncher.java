package com.tyron.compiler.util;

import com.apk.builder.logger.Logger;
import com.apk.builder.model.Project;
import com.apk.builder.ApplicationLoader;
import com.tyron.server.DefaultLanguageClient;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.net.Socket;
import java.net.ServerSocket;

import org.eclipse.lsp4j.jsonrpc.Launcher;
import org.eclipse.lsp4j.launch.LSPLauncher;
import org.eclipse.lsp4j.services.LanguageServer;
import org.eclipse.lsp4j.*;

import static com.apk.builder.ApplicationLoader.getContext;

public class LanguageServerLauncher {
    
    public static final int STARTED = 1;
    public static final int STARTING = 2;
    public static final int STOPPED = 3;
    public static final int CRASHED = 4;
    
    private static final String TAG = LanguageServerLauncher.class.getSimpleName();
    
    Project mProject;
    
    private ServerSocket mSocketServer;
    private Socket mSocket;
    
    private LanguageServer mServer;
    private DefaultLanguageClient mClient;
    
    private int mStatus;
    
    public LanguageServerLauncher (Project project) {
        mProject = project;
    }
    
    private static LanguageServerLauncher Instance = null;
    
    public static LanguageServerLauncher getInstance() {
        return Instance;
    }
    
    public void start(int port) throws IOException {
        mSocketServer = new ServerSocket(port);
        mProject.getLogger().d(TAG, "Server started on port: " + port);
        setStatus(STARTING);
       
        new Thread(() -> {
            try {
                final List<String> args = new ArrayList();
                args.add("-Declipse.application=org.eclipse.jdt.ls.core.id1");
                args.add("-Dosgi.bundles.defaultStartLevel=4");
                args.add("-Declipse.product=org.eclipse.jdt.ls.core.product");
                args.add("-Dlog.level=ALL");
                args.add("-DCLIENT_PORT=" + port);
                args.add("-noverify");
                args.add("-Xmx1G");
                args.add("-jar");
                args.add(getContext().getFilesDir() + "/language-server/plugins/org.eclipse.equinox.launcher_1.5.200.v20180922-1751.jar");
                args.add("-configuration");
                args.add(getContext().getFilesDir() + "/language-server/config_linux");
                args.add("-data");
                args.add(getContext().getFilesDir() + "/workspace");
                args.add("--add-modules=ALL-SYSTEM");
                args.add("--add-opens java.base/java.util=ALL-UNNAMED"); 
                args.add("--add-opens java.base/java.lang=ALL-UNNAMED");
                
                JRELauncher launcher = new JRELauncher(ApplicationLoader.getContext());
                launcher.launchJVM(args);
            } catch (Exception e) {
                mProject.getLogger().e(TAG, e.getMessage());
                setStatus(CRASHED);
            }
        }).start();
    }
    
    public void startListening() throws IOException {
        if (mSocketServer == null) {
          throw new IllegalStateException("Server socket is not initialized");
        }
        
        Instance = this;
        
        Thread thread = new Thread() {
            @Override
            public void run() {
                try {
                    mProject.getLogger().d(TAG, "Listening for incoming connections.");
                    mSocket = mSocketServer.accept();
                    mProject.getLogger().d(TAG, "Connected successfully");
        
                    InputStream input = mSocket.getInputStream();
                    OutputStream out = mSocket.getOutputStream();
                                       
                    Launcher<LanguageServer> launcher = LSPLauncher.createClientLauncher(mClient = new DefaultLanguageClient(mProject),
                            input,
                            out);
                    
                    mServer = launcher.getRemoteProxy();
                    launcher.startListening();
                    
                    mServer.initialize(getInitParams());
                } catch (IOException e) {
                    mProject.getLogger().e(TAG, e.getMessage());
                }
                setStatus(STARTED);
            }
        };
        thread.start();
    }
    
    public void shutdown() {
        Instance = null;
    }
    
    public LanguageServer getServer() {
        return mServer;
    }
    
    public DefaultLanguageClient getLanguageClient() {
        return mClient;
    }
    
    public void setStatus(int status) {
        mStatus = status;
    }
    
    public int getStatus() {
        return mStatus;
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
        
        CompletionItemCapabilities completionItemCapabilities = new CompletionItemCapabilities(false);
        completionItemCapabilities.setInsertReplaceSupport(true);
        textDocumentClientCapabilities.setCompletion(new CompletionCapabilities(completionItemCapabilities));
        textDocumentClientCapabilities.setDefinition(new DefinitionCapabilities());
        textDocumentClientCapabilities.setReferences(new ReferencesCapabilities());
        textDocumentClientCapabilities.setRename(new RenameCapabilities());
       // textDocumentClientCapabilities.setSemanticHighlightingCapabilities(new SemanticHighlightingCapabilities(false));
        textDocumentClientCapabilities.setSignatureHelp(new SignatureHelpCapabilities());
        
        PublishDiagnosticsCapabilities publishDiagnosticsCapabilities = new PublishDiagnosticsCapabilities(true);
        textDocumentClientCapabilities.setPublishDiagnostics(publishDiagnosticsCapabilities);
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