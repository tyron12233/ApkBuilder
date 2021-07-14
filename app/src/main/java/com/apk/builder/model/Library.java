package com.apk.builder.model;

import com.apk.builder.FileUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class Library {
    
    private String mLibraryName;
    private File mPath;
    
    private Pattern mPackagePattern = Pattern.compile("(package\\=\".*\")");
    
    public Library(String path) {
        mPath = new File(path);
		mLibraryName = mPath.getName();
    }
	
    public static List<Library> fromFile(File file){
        
		List<Library> libraries = new ArrayList<>();
		
		if (!file.exists()) {
			return libraries;
		}
		
		File[] childs = file.listFiles();
		if (childs == null) {
			return libraries;
		}
		
		for (File child : childs) {
			if (new File(child, "classes.jar").exists()) {
				libraries.add(new Library(child.getAbsolutePath()));
			}
		}
		return libraries;
    }
	
	public File getPath() {
		return mPath;
	}
	
	public String getName() {
		return mLibraryName;
	}
	
	public File getResourcesFile() {
	    return new File(mPath, "res");
	}
	
	public File getClassJarFile() {
	    return new File(mPath, "classes.jar");
	}
	
	public List<File> getDexFiles() {
	    List<File> files = new ArrayList<>();
	    File[] fileArr = mPath.listFiles();
	    if (fileArr == null) {
	        return files;
	    }
	    
	    for (File file : fileArr) {
	        if (file.getName().endsWith(".dex")) {
	            files.add(file);
	        }
	    }
	    return files;
	}
	
	public boolean requiresResourceFile() {
		return new File(mPath, "res").exists();
	}
	
	public String getPackageName() {
		String manifest = FileUtil.readFile(mPath + "/AndroidManifest.xml");
		Matcher matcher = mPackagePattern.matcher(manifest);
		
		if (matcher.find()) {
			return matcher.group(1).substring(9, matcher.group(1).length() -1);
	    }
		
		return null;
	}
}