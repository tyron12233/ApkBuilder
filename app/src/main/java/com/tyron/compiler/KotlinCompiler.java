package com.tyron.compiler;

import com.apk.builder.model.Project;
import com.apk.builder.model.Library;
import com.tyron.compiler.exception.CompilerException;
import com.tyron.compiler.util.JRELauncher;
import com.tyron.compiler.util.LanguageServerLauncher;

import java.io.File;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;


import java.io.IOException;

import static com.apk.builder.ApplicationLoader.getContext;

public class KotlinCompiler extends Compiler {
    
    private static final String TAG = "kotlinc";
    
    private Project mProject;
    private JRELauncher jreLauncher;
    
    public KotlinCompiler(Project project) {
        mProject = project;
    }
    
    @Override
    public void prepare() throws CompilerException {
        if (!isOpenJDKInstalled()) {
            throw new CompilerException("OpenJDK binary is not installed.");
        }
        
        if (!isKotlinInstalled()) {
            throw new CompilerException("Kotlin compiler is not installed");
        }
               
        Map<String, String> env = new HashMap<>();
        
        env.put("HOME", getContext().getFilesDir().getAbsolutePath() + "/workspace");
		env.put("JAVA_HOME", getContext().getFilesDir() + "/openjdk");
		env.put("KOTLIN_HOME", getContext().getFilesDir() + "/kotlinc");
		env.put("LD_LIBRARY_PATH", getContext().getFilesDir() + "/openjdk/lib:"
		        + getContext().getFilesDir() + "/openjdk/lib/jli:" 
				+ getContext().getFilesDir() + "/openjdk/lib/server:"
				+ getContext().getFilesDir() + "/openjdk/lib/hm:");
		jreLauncher = new JRELauncher(getContext());
		jreLauncher.setEnvironment(env);
    }
    
    @Override
    public void run() throws CompilerException, IOException {
		
		mProject.getLogger().d(TAG, "Running...");
		
		/*LanguageServerLauncher server = new LanguageServerLauncher(mProject);
		server.startSocket(6969);
		
		new Thread() {
		    @Override
		    public void run() {
    		    try {
    		        server.startListening();
    		    } catch (Exception e) {
    		    
    		    }
		    }
		}.start();    
        */
        
        List<String> args = new ArrayList<>();
        args.add(getContext().getFilesDir() + "/kotlinc/lib/kotlin-compiler.jar");		
        args.add("-classpath");
		args.add(classpath());
		args.add("-d");
		args.add(mProject.getOutputFile() + "/bin/classes");
	//	args.add("-Xplugin=$KOTLIN_HOME/lib/compose-compiler-1.0.0.jar");
		
		args.add(mProject.getJavaFile().getAbsolutePath());
        
        try {
            Process process = jreLauncher.launchJVM(args);
            loadStream(process.getInputStream(), false);
            loadStream(process.getErrorStream(), true);
            int rc = process.waitFor();
            
            if (rc != 0) {
                throw new CompilerException("Compilation failed, check output for more details.");
            }
        } catch (Exception e) {
            throw new CompilerException(e.getMessage());
        }
    }
    
    private boolean isOpenJDKInstalled() {
        File javaFile = new File("/data/data/com.apk.builder/files/openjdk/bin/java");
        if (!javaFile.exists()) {
            mProject.getLogger().w("JAVA", "Java file.exists returns false!");
        }
        return true;
    }
    
    private boolean isKotlinInstalled() {
        File kotlinFile = new File(getContext().getFilesDir(), "/kotlinc/lib/kotlin-compiler.jar");
        return true;
    }
    
    private String classpath() {
        StringBuilder sb = new StringBuilder();
        for (Library library : mProject.getLibraries()) {
            File classFile = library.getClassJarFile();
			if (classFile.exists()) {
				sb.append(classFile.getAbsolutePath());
				sb.append(":");
			}
        }
		sb.append(getAndroidJarFile().getAbsolutePath());
		sb.append(":");
		
        return sb.toString();
    }
    
    private void loadStream(InputStream s, boolean error) throws Exception {
        BufferedReader br = new BufferedReader(new InputStreamReader(s));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null) {
            if (error) {
                mProject.getLogger().e(TAG, line);
            } else {
                mProject.getLogger().d(TAG, line);
            }
        }
	}
	
	private static void addToEnvIfPresent(Map<String, String> map, String env) {
		String value = System.getenv(env);
		if (value != null) {
			map.put(env, value);
		}
	}
}