package com.apk.builder.util;

import android.content.Context;
import android.os.AsyncTask;
import android.net.Uri;
import android.app.AlertDialog;
import android.app.ProgressDialog;

import com.apk.builder.logger.Logger;

import org.apache.commons.compress.compressors.xz.*;
import org.apache.commons.compress.*;
import java.io.File;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;

import java.lang.ref.WeakReference;

import static com.apk.builder.ApplicationLoader.getContext;

public class JDKInstallTask extends AsyncTask<Uri, String, String> {
	
	private WeakReference<Context> ref;
	private Logger mLogger;
	
	//TODO: replace dialog with a custom dialog
	private ProgressDialog dialog;
	
	public JDKInstallTask(Context context, Logger logger) {
		ref = new WeakReference<>(context);
		mLogger = logger;
	}
	
	@Override
	public void onPreExecute() {
		dialog = new ProgressDialog(ref.get());
		dialog.setIndeterminate(true);
		dialog.setMessage("Extracting files");
		dialog.show();
		dialog.setCancelable(false);
	}
	
	@Override
	public String doInBackground(Uri... params) {
		File dest = getContext().getFilesDir();
		Uri uri = params[0];
		
		String name = null;
		
		mLogger.d("Extract", "Extracting to " + dest);
		
		try {
			TarArchiveInputStream tarIn = null;
			
			tarIn = new TarArchiveInputStream(new XZCompressorInputStream(new BufferedInputStream(getContext().getContentResolver().openInputStream(uri))));
			
			TarArchiveEntry tarEntry = tarIn.getNextTarEntry();
			
			name = tarEntry.getName();
			
			while (tarEntry != null) {// create a file with the same name as the tarEntry
				File destPath = new File(dest, tarEntry.getName());
				System.out.println("working: " + destPath.getCanonicalPath());
				if (tarEntry.isDirectory()) {
					destPath.mkdirs();
				} else {
					destPath.createNewFile();
					
					publishProgress("Extracting " + destPath.getName());
					mLogger.d("Extract", "Extracting to: " + destPath);
					//byte [] btoRead = new byte[(int)tarEntry.getSize()];
					byte [] btoRead = new byte[1024];
					//FileInputStream fin 
					//  = new FileInputStream(destPath.getCanonicalPath());
					BufferedOutputStream bout = 
					new BufferedOutputStream(new FileOutputStream(destPath));
					int len = 0;
					
					while((len = tarIn.read(btoRead)) != -1)
					{
						bout.write(btoRead,0,len);
					}
					
					bout.close();
					btoRead = null;
					
				}
				tarEntry = tarIn.getNextTarEntry();
			}
			tarIn.close();
			
			File renameFile = new File(getContext().getFilesDir(), "openjdk");
			renameFile.mkdirs();
			
			mLogger.d("Extract", "Creating file " + renameFile);
			
			new File(getContext().getFilesDir(), name).renameTo(renameFile);
			
			mLogger.d("Extract", "Renamed " + name + " to " + renameFile.getName());
			
			setAllExecutable(renameFile);
			
		} catch (Exception e) {
			return "Error: " + e.getMessage();
		}
		
		return "Success";
	}
	
	@Override
	public void onProgressUpdate(String... update) {
		dialog.setMessage(update[0]);
	}
	
	@Override
	public void onPostExecute(String string) {
		dialog.dismiss();
		
		AlertDialog.Builder ab = new AlertDialog.Builder(ref.get());
		ab.setTitle("Result");
		ab.setMessage(string);
		ab.setPositiveButton("OK", null);
		ab.create().show();
	}
	
	private void setAllExecutable(File root) {
		File[] childs = root.listFiles();
		
		//mLogger.d("Extract", "Setting all files as executable in " + root);
		if (childs != null) {
			for (File child: childs) {
				if (child.isDirectory()) {
					setAllExecutable(child);
				} else {
				    child.setExecutable(true);
			    }
			}
		}
	}
}
