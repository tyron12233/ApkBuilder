package com.tyron.compiler;

import android.os.AsyncTask;
import android.content.Context;
import android.app.Dialog;
import android.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.view.LayoutInflater;
import android.widget.TextView;
import android.net.Uri;

import com.android.sdklib.build.ApkBuilder;
import com.android.sdklib.build.ApkCreationException;
import com.android.sdklib.build.DuplicateFileException;
import com.android.sdklib.build.SealedApkException;

import com.apk.builder.R;
import com.apk.builder.model.*;
import com.apk.builder.apksigner.ApkSigner;
import com.tyron.compiler.exception.*;
import com.tyron.compiler.incremental.IncrementalECJCompiler;
import com.tyron.compiler.incremental.IncrementalD8Compiler;

import java.lang.ref.WeakReference;
import java.io.IOException;
import java.io.File;

public class CompilerAsyncTask extends AsyncTask<Project, String, CompilerResult> {
	
	private final WeakReference<Context> mContext;
	
	private TextView progress;
	private Dialog dialog;
	
	private long startTime;
	
	public CompilerAsyncTask(Context context) {
		mContext = new WeakReference<>(context);
	}
	
	@Override
	public void onPreExecute() {
		Context context = mContext.get();
		
		startTime = System.currentTimeMillis();
	}
	
	@Override
	public CompilerResult doInBackground(Project... params) {
	
	    Project project = params[0];
	    try {
			
	        Compiler aapt2Compiler = new AAPT2Compiler(project);
			aapt2Compiler.setProgressListener(args -> {
				publishProgress(args);
			});
	        aapt2Compiler.prepare();
	        aapt2Compiler.run();
	        
			if (project.getSettings().getBoolean(ProjectSettings.KOTLIN_ENABLED)) {
			    Compiler kotlinCompiler = new KotlinCompiler(project);
			    kotlinCompiler.prepare();
			    kotlinCompiler.run();
			}
			
			
			Compiler ecjCompiler = new ECJCompiler(project);
			ecjCompiler.setProgressListener(args -> {
				publishProgress(args);
			});
			ecjCompiler.prepare();
			ecjCompiler.run();
			
			Compiler d8Compiler = new D8Compiler(project);
			d8Compiler.setProgressListener(args -> {
				publishProgress(args);
			});
			d8Compiler.prepare();
			d8Compiler.run();
		    
		    publishProgress("Packaging APK...");
			project.getLogger().d("APK Builder", "Packaging APK");
			
		    File binDir = new File(project.getOutputFile(), "bin");
		    File apkPath = new File(binDir, "gen.apk");
		    apkPath.createNewFile();
		    
		    File resPath = new File(binDir, "generated.apk.res");
		    
		    File dexFile = new File(binDir, "classes.dex");
		    ApkBuilder builder = new ApkBuilder(apkPath, resPath, dexFile, null, null);
			
			File[] binFiles = binDir.listFiles();
			for (File file : binFiles) {
			    if (!file.getName().equals("classes.dex") && file.getName().endsWith(".dex")) {
			        builder.addFile(file, Uri.parse(file.getAbsolutePath()).getLastPathSegment());
					project.getLogger().d("APK Builder", "Adding dex file " + file.getName() + " to APK.");
			    }
			}
			for (Library library : project.getLibraries()) {
			    builder.addResourcesFromJar(new File(library.getPath(), "classes.jar"));
				
				project.getLogger().d("APK Builder", "Adding resources of " + library.getName() + " to the APK");
			}
			
			File nativeLibs = project.getNativeLibrariesFile();
			if (nativeLibs != null && nativeLibs.exists()) {
				builder.addNativeLibraries(nativeLibs);
			}
			builder.setDebugMode(false);
			builder.sealApk();
		        
	        project.getLogger().d("APK Signer", "Signing APK");
			
			 //sign the app
	        new ApkSigner(project,apkPath.getAbsolutePath(),binDir + "/signed.apk" , ApkSigner.Mode.TEST).sign();
			
			
			long time = System.currentTimeMillis() - startTime;
			
			project.getLogger().d("APK Builder", "Build success, took " + time + "ms");
			
	    } catch (Exception e) {
		      return new CompilerResult(android.util.Log.getStackTraceString(e), true);
		}
	    return new CompilerResult("Success", false);
	}
	
	@Override
	public void onProgressUpdate(String... update) {
	 //   progress.setText(update[0]);
	}
	
	@Override
	public void onPostExecute(CompilerResult result) {
		
		if (result.isError()) {
			AlertDialog.Builder builder = new AlertDialog.Builder(mContext.get());
			builder.setTitle("Compilation error");
			builder.setMessage(result.getMessage());
			builder.setPositiveButton("CLOSE", null);
			builder.create().show();
		}
	}
}
