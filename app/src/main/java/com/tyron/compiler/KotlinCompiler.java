package com.tyron.compiler;

import com.apk.builder.model.Project;
import com.apk.builder.model.Library;
import com.tyron.compiler.exception.CompilerException;

import java.io.File;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Map;

import java.io.IOException;

import static com.apk.builder.ApplicationLoader.getContext;

public class KotlinCompiler extends Compiler {
    
    private static final String TAG = "kotlinc";
    
    private Project mProject;
    private ProcessBuilder pb;
    
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
        
        pb = new ProcessBuilder();
        Map<String, String> env = pb.environment();
        env.clear();
        env.put("HOME", getContext().getFilesDir().getAbsolutePath());
		env.put("PATH", System.getenv("PATH"));
		env.put("LANG", "en_US.UTF-8");
		env.put("PWD", getContext().getFilesDir().getAbsolutePath());
		env.put("BOOTCLASSPATH", System.getenv("BOOTCLASSPATH"));
		env.put("ANDROID_ROOT", System.getenv("ANDROID_ROOT"));
		env.put("ANDROID_DATA", System.getenv("ANDROID_DATA"));
		env.put("EXTERNAL_STORAGE", System.getenv("EXTERNAL_STORAGE"));
		env.put("JAVA_HOME", getContext().getFilesDir() + "/openjdk");
		env.put("KOTLIN_HOME", getContext().getFilesDir() + "/kotlinc");
		addToEnvIfPresent(env, "ANDROID_ART_ROOT");
		addToEnvIfPresent(env, "DEX2OATBOOTCLASSPATH");
		addToEnvIfPresent(env, "ANDROID_I18N_ROOT");
		addToEnvIfPresent(env, "ANDROID_RUNTIME_ROOT");
		addToEnvIfPresent(env, "ANDROID_TZDATA_ROOT");
		File tempDir = new File(getContext().getFilesDir(), "temp");
		if (!tempDir.exists()) {
			tempDir.mkdirs();
		}
		env.put("TMPDIR", tempDir.getAbsolutePath());
		pb.directory(getContext().getFilesDir());
		pb.redirectErrorStream(true);
    }
    
    @Override
    public void run() throws CompilerException, IOException {
		
		mProject.getLogger().d(TAG, "Running...");
		
        StringBuilder args = new StringBuilder();
        args.append(getContext().getFilesDir() + "/openjdk/bin/java ");
        args.append("-jar ");
        args.append(getContext().getFilesDir() + "/kotlinc/lib/kotlin-compiler.jar ");
		args.append("-verbose ");
        args.append("-classpath " + classpath() + " ");
		args.append("-d " + mProject.getOutputFile() + "/bin/classes ");
		args.append("-Xplugin=$KOTLIN_HOME/lib/compose-compiler-1.0.0.jar ");
		//args.append("-Xplugin=$KOTLIN_HOME/lib/kotlin-annotation-processing.jar ");
		//args.append("-P plugin:org.jetbrains.kotlin.kapt3:aptMode=aptAndStubs,");
		//args.append("-P plugin:androidx.compose.compiler.plugins.kotlin:kotlinCompilerExtensionVersion=1.0.0-rc01 ");
		
		args.append(mProject.getJavaFile().getAbsolutePath());
		//args.append(" -P plugin:androidx.compose.compiler.plugins.kotlin:kotlinCompilerVersion=1.5.0 ");
        pb.command("/system/bin/sh", "-c", args.toString());
        
        try {
            Process process = pb.start();
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
        File javaFile = new File(getContext().getFilesDir(), "openjdk/bin/java");
        return javaFile.exists();
    }
    
    private boolean isKotlinInstalled() {
        File kotlinFile = new File(getContext().getFilesDir(), "kotlinc/lib/kotlin-compiler.jar");
        return kotlinFile.exists();
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