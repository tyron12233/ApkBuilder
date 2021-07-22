package com.tyron.compiler;

import android.util.Log;

import com.tyron.compiler.exception.CompilerException;

import com.apk.builder.FileUtil;
import com.apk.builder.BinaryExecutor;
import com.apk.builder.model.Project;
import com.apk.builder.model.Library;
import com.apk.builder.ApplicationLoader;

import java.util.ArrayList;
import java.util.List;
import java.io.File;
import java.io.IOException;

public class AAPT2Compiler extends Compiler {
	
	private static final String TAG = "AAPT2";
	
	private Project mProject;
	private final File mFilesDir;
	private List<Library> mLibraries;
	//we have a separate list to generate R.java for them but not recompile them
	private List<Library> mLibrariesCopy;
    
    private File binDir;
    private File genDir;
    private File outputPath;
    private File resPath;
    
    private BinaryExecutor executor;
    
	public AAPT2Compiler(Project project) {
	    mProject = project;
	    mFilesDir = ApplicationLoader.applicationContext.getFilesDir();
	}
	
    @Override
    public void prepare() throws CompilerException {
		mProject.getLogger().d(TAG, "Preparing");
		onProgressUpdate("Preparing AAPT2...");
		
		mLibraries = new ArrayList<>();
        mLibraries.addAll(mProject.getLibraries());
        mLibrariesCopy = new ArrayList<>(mLibraries);
		
        binDir = new File(mProject.getOutputFile(), "bin");
        genDir = new File(mProject.getOutputFile(), "gen");
        genDir.delete();
        
        for (Library library : new ArrayList<>(mLibraries)) {
            if (new File(binDir, "/res/" + library.getName() + ".zip").exists()) {
				//remove the library from the list so it wont get compiled
                mLibraries.remove(library);
				
				mProject.getLogger().d(TAG, "Removing " + library.getName() + " to speed up compilation");
			}
        }
		
		File[] childs = binDir.listFiles();
	    if (childs != null) {
			for (File child : childs) {
				if (child.getName().equals("res")) {
					continue;
				}
				child.delete();
			}
		}
        
        FileUtil.makeDir(binDir.getPath());
        FileUtil.makeDir(genDir.getPath());
    }
    
    @Override
    public void run() throws CompilerException, IOException {
        
        ArrayList<String> args = new ArrayList<>();
        executor = new BinaryExecutor();
        
		//compile resources
        onProgressUpdate("AAPT2 > Compiling resources");
		mProject.getLogger().d(TAG, "Compiling project resources");
		
        args.add(getAAPT2File().getAbsolutePath());
        args.add("compile");
        args.add("--dir");
        args.add(mProject.getResourcesFile().getAbsolutePath());
        args.add("-o");
		
		resPath = new File(binDir, "res");
		resPath.mkdir();
		
		outputPath = createNewFile(resPath, "project.zip");
		
        args.add(outputPath.getAbsolutePath());
		
        executor.setCommands(args);
        if (!executor.execute().isEmpty()) {
             throw new CompilerException(executor.getLog());
        }
        
        onProgressUpdate("AAPT2 > Compiling libraries");
        compileLibraries();
		
		args.clear();
		
		//link resources
        onProgressUpdate("AAPT2 > Linking resources");
		mProject.getLogger().d(TAG, "Linking resources");
		
		args.add(getAAPT2File().getAbsolutePath());
		args.add("link");
		args.add("--allow-reserved-package-id");
		args.add("--no-version-vectors");
		args.add("--no-version-transitions");
		args.add("--auto-add-overlay");
		args.add("--min-sdk-version");
		args.add(String.valueOf(mProject.getMinSdk()));
		args.add("--target-sdk-version");
		args.add(String.valueOf(mProject.getTargetSdk()));
		args.add("--version-code");
		args.add(String.valueOf(mProject.getVersionCode()));
		args.add("--version-name");
		args.add(String.valueOf(mProject.getVersionName()));
		//TODO: custom android.jar
		args.add("-I");
		args.add(getAndroidJarFile().getAbsolutePath());
		
		if (mProject.getAssetsFile() != null) {
			args.add("-A");
			args.add(mProject.getAssetsFile().getAbsolutePath());
		}
		//add compiled resources
        File[] resources = resPath.listFiles();
        if (resources != null) {
            for (File file : resources) {
                if (file.isDirectory() || file.getName().equals("project.zip")) {
                    continue;
                }
                args.add("-R");
                args.add(file.getAbsolutePath());
            }
        }
		
       File projectZip = new File(resPath, "project.zip");
	   if (projectZip.exists()) {
		   args.add("-R");
		   args.add(projectZip.getAbsolutePath());
	   }
        
       //export generated R.java files to /dir/gen/
       args.add("--java");
       args.add(genDir.getAbsolutePath() +"/");
	   
	   args.add("--manifest");
	   args.add(mProject.getManifestFile().getAbsolutePath());
	   
	   StringBuilder sb = new StringBuilder();
	   for (Library library : mLibrariesCopy) {
		   if (library.requiresResourceFile()) {
		       mProject.getLogger().d(TAG, "Adding extra package: " + library.getPackageName());
			   sb.append(library.getPackageName());
			   sb.append(":");
		   }
	   }
	   
	   if (!sb.toString().isEmpty()) {
		   args.add("--extra-packages");
		   args.add(sb.toString().substring(0, sb.toString().length() -1));
	   }
	   
	   args.add("-o");
	   args.add(createNewFile(binDir, "generated.apk.res").getAbsolutePath());
	   
	   executor.setCommands(args);
	   if (!executor.execute().isEmpty()) {
		   throw new CompilerException(executor.getLog());
	   }
    }
    
    
    private void compileLibraries() throws CompilerException, IOException {
        ArrayList<String> args = new ArrayList<>();
        
        for (Library library : mLibraries) {
            if (!library.getResourcesFile().exists()) {
				//mProject.getLogger().w(TAG, "Resource folder doesn't exist at path " + library.getResourcesFile());
                continue;
            }
			
			mProject.getLogger().d(TAG, "Compiling library: " + library.getName());
			args.clear();
            args.add(getAAPT2File().getAbsolutePath());
            args.add("compile");
            args.add("--dir");
            args.add(library.getResourcesFile().getAbsolutePath());
            args.add("-o");
            File output = createNewFile(resPath, library.getName() + ".zip");
            args.add(output.getAbsolutePath());
            
            executor.setCommands(args);
            if(!executor.execute().isEmpty()) {
                throw new CompilerException("AAPT2: " + executor.getLog());
            }
        }
    }
    
    
    private File createNewFile(File parent, String name) throws IOException {
        File createdFile = new File(parent, name);
        parent.mkdirs();
        createdFile.createNewFile();
        return createdFile;
    }
	
	private File getAAPT2File() throws CompilerException, IOException {
		/*File check = new File(ApplicationLoader.applicationContext.getFilesDir() + "/temp/aapt2");
	    
		if (check.exists()) {
			return check;
		}
		
		check.getParentFile().mkdirs();
	    */
		File nativeLibrary = new File(ApplicationLoader.applicationContext.getApplicationInfo().nativeLibraryDir + "/libaapt2.so");
		
		if (!nativeLibrary.exists()) {
			throw new CompilerException("AAPT2 binary not found");
		}
		
		return nativeLibrary;
	}
    
}
