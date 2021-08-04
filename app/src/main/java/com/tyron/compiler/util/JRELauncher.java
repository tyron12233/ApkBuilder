package com.tyron.compiler.util;

import android.content.Context;

import java.io.File;
import java.util.ArrayList;
import java.util.Map;
import java.util.List;
import java.util.HashMap;

public class JRELauncher {
    
    private Context mContext;
    private ProcessBuilder pb;
    private Map<String, String> customEnv;
    
    public JRELauncher(Context context) {
        mContext = context.getApplicationContext();
    }
    
    public void setEnvironment(Map<String, String> map) {
        customEnv = map;
    }
    
    public void prepare() {
        
        pb = new ProcessBuilder();
        Map<String, String> env = pb.environment();
        env.clear();
        env.put("HOME", mContext.getFilesDir().getAbsolutePath() + "/openjdk");
		env.put("PATH", System.getenv("PATH"));
		env.put("LANG", "en_US.UTF-8");
		env.put("PWD", mContext.getFilesDir().getAbsolutePath());
		env.put("BOOTCLASSPATH", System.getenv("BOOTCLASSPATH"));
		env.put("ANDROID_ROOT", System.getenv("ANDROID_ROOT"));
		env.put("ANDROID_DATA", System.getenv("ANDROID_DATA"));
		env.put("EXTERNAL_STORAGE", System.getenv("EXTERNAL_STORAGE"));
		env.put("JAVA_HOME", mContext.getFilesDir() + "/openjdk");
		addToEnvIfPresent(env, "ANDROID_ART_ROOT");
		addToEnvIfPresent(env, "DEX2OATBOOTCLASSPATH");
		addToEnvIfPresent(env, "ANDROID_I18N_ROOT");
		addToEnvIfPresent(env, "ANDROID_RUNTIME_ROOT");
		addToEnvIfPresent(env, "ANDROID_TZDATA_ROOT");
		File tempDir = new File(mContext.getCacheDir(), "temp");
		if (!tempDir.exists()) {
			tempDir.mkdirs();
		}
		env.put("TMPDIR", tempDir.getAbsolutePath());
		
		if (customEnv != null) {
		    env.putAll(customEnv);
		}
		
		pb.redirectErrorStream(true);
    }
    
    public Process launchJVM(List<String> args) throws Exception {
        prepare();
        
        List<String> arguments = new ArrayList<>();
        arguments.add(mContext.getFilesDir() + "/openjdk/bin/java");
        arguments.addAll(args);
        pb.command(arguments);
        return pb.start();
    }
    
    private static void addToEnvIfPresent(Map<String, String> map, String env) {
		String value = System.getenv(env);
		if (value != null) {
			map.put(env, value);
		}
	}
}