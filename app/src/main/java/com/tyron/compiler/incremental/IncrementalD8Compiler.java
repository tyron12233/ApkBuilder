package com.tyron.compiler.incremental;

import com.apk.builder.model.Project;
import com.apk.builder.model.Library;
import com.tyron.compiler.Compiler;
import com.tyron.compiler.exception.*;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.android.tools.r8.D8;

public class IncrementalD8Compiler extends Compiler {
    
	private static final String TAG = "Incremental D8";
	
    private Project mProject;
    
    public IncrementalD8Compiler(Project project) {
        mProject = project;
    }
    
    @Override
    public void prepare() {
    
    }
    
    @Override
    public void run() throws CompilerException {
        onProgressUpdate("D8 > Running...");
		mProject.getLogger().d(TAG, "Running...");
     
        List<String> args = new ArrayList<>();
        
        args.add("--release");
        args.add("--min-api"); 
        args.add(String.valueOf(mProject.getMinSdk()));
        args.add("--lib");
        args.add(getAndroidJarFile().getAbsolutePath());
        args.add("--output");
        args.add(mProject.getOutputFile() + "/bin/");
        
        List<File> classes = getClassFiles(new File(mProject.getOutputFile() + "/intermediate/classes/"));
		for (File file : classes) {
		    args.add(file.getAbsolutePath());
		}
		
		for (Library library : mProject.getLibraries()) {
			
			if (library.getDexFiles().isEmpty()) {
				try {
				    dexLibrary(library);
				} catch (Exception e) {
				    throw new CompilerException(e.getMessage());
				}
			}
            for (File dexFile : library.getDexFiles()) {
                args.add(dexFile.getAbsolutePath());
            }
        }
        
        try {
            D8.main(args.toArray(new String[0]));
        } catch (Exception e) {
            throw new CompilerException(e.getMessage());
        }
        
        args.clear();
       /* onProgressUpdate("D8 > Merging dex files");
        
        for (File file : getDexFiles()) {
            args.add(file.getAbsolutePath());
        }
        args.add("--output");
		
		File dexPath = new File(mProject.getWorkingDirectory() + "/bin/dex/");
		dexPath.mkdirs();
        args.add(dexPath.getAbsolutePath());
        
        for (Library library : mProject.getLibraries()) {
            for (File dexFile : library.getDexFiles()) {
                args.add(dexFile.getAbsolutePath());
            }
        }
        try {
            D8.main(args.toArray(new String[0]));
        } catch (Exception e) {
            throw new CompilerException(e.getMessage());
        }*/
        
    }
    
    public List<File> getDexFiles() {
        List<File> files = new ArrayList<>();
        
        File[] fileArr = new File(mProject.getOutputFile(), "/bin/classes/").listFiles();
        
        if (fileArr == null) {
            return files;
        }
        
        for (File file : fileArr) {
            if (file.getName().startsWith("classes") && file.getName().endsWith(".dex")) {
                files.add(file);
            }
        }
        
        return files;
    }
    
    private List<File> getClassFiles(File dir) {
        List<File> files = new ArrayList<>();
        File[] fileArr = dir.listFiles();
        if (fileArr == null) {
            return files;
        }
        
        for (File file : fileArr) {
            if (file.isDirectory()) {
                files.addAll(getClassFiles(file));
            } else {
                files.add(file);
            }
        }
        
        return files;
    }
	
	private void dexLibrary(Library library) throws Exception {
		
		mProject.getLogger().d(TAG, "Library " + library.getName() + " does not have a dex file, generating one");
		
		List<String> args = new ArrayList<>();
		args.add("--release");
        args.add("--min-api"); 
        args.add(String.valueOf(mProject.getMinSdk()));
		args.add("--lib");
		args.add(getAndroidJarFile().getAbsolutePath());
		args.add("--output");
		args.add(library.getPath().getAbsolutePath());
		args.add(library.getClassJarFile().getAbsolutePath());
	
        D8.main(args.toArray(new String[0]));
        
	}
}