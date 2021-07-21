package com.apk.builder.apksigner;

import com.apk.builder.model.Project;
import com.apk.builder.ApplicationLoader;
import com.apk.builder.util.Decompress;

import java.util.ArrayList;
import java.io.File;


public class ApkSigner {
	
	public static class Mode {
		public static int TEST = 0;
		//ToDo add more modes
	}
	
	
	private ArrayList<String> commands ;
	private String mApkInputPath ;
	private String mApkOutputPath;
	private Project mProject;
	public ApkSigner(Project project,String inputPath ,String outputPath,int mode){
		commands = new ArrayList<>();
		mProject = project;
		mApkInputPath = inputPath;
		mApkOutputPath = outputPath;
		
	}
	
	public void sign() throws Exception{
		commands.add("sign");
		commands.add("--key");
		commands.add(getTestKeyFilePath());
		commands.add("--cert");
		commands.add(getTestCertFilePath());
		commands.add("--min-sdk-version");
		commands.add(String.valueOf(mProject.getMinSdk()));
		commands.add("--max-sdk-version");
		commands.add(String.valueOf(mProject.getTargetSdk()));
		commands.add("--out");
		commands.add(mApkOutputPath);
		commands.add("--in");
		commands.add(mApkInputPath);
		com.android.apksigner.ApkSignerTool.main(commands.toArray(new String[commands.size()]));
		
	}
	
	
	private String getTestKeyFilePath() {
		File check = new File(ApplicationLoader.applicationContext.getFilesDir() + "/temp/testkey.pk8");
		
		if (check.exists()) {
			return check.getAbsolutePath();
		}
		
		Decompress.unzipFromAssets(ApplicationLoader.applicationContext, "testkey.pk8.zip", check.getParentFile().getAbsolutePath());
		
		return check.getAbsolutePath();
	}
	
	private String getTestCertFilePath() {
		File check = new File(ApplicationLoader.applicationContext.getFilesDir() + "/temp/testkey.x509.pem");
		
		if (check.exists()) {
			return check.getAbsolutePath();
		}
		
		Decompress.unzipFromAssets(ApplicationLoader.applicationContext, "testkey.x509.pem.zip", check.getParentFile().getAbsolutePath());
		
		return check.getAbsolutePath();
	} 
	
}
