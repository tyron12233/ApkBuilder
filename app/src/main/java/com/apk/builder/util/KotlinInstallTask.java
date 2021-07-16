package com.apk.builder.util;

import android.content.Context;
import android.os.AsyncTask;
import android.net.Uri;
import android.app.AlertDialog;
import android.app.ProgressDialog;

import java.lang.ref.WeakReference;

import static com.apk.builder.ApplicationLoader.getContext;

public class KotlinInstallTask extends AsyncTask<Uri, String, String> {
	
	private WeakReference<Context> ref;
	private ProgressDialog dialog;
	
	public KotlinInstallTask(Context context) {
		ref = new WeakReference<>(context);
	}
	
	@Override
	public void onPreExecute() {
		dialog = new ProgressDialog(ref.get());
		dialog.setIndeterminate(true);
		dialog.setMessage("Preparing");
		dialog.show();
		dialog.setCancelable(false);
	}
     
	@Override
	public String doInBackground(Uri... params) {
		
		try {
			Uri uri = params[0];
		    Decompress.unzip(getContext().getContentResolver().openInputStream(uri), getContext().getFilesDir().getAbsolutePath());
		} catch (Exception e) {
		    return "Error: " + e.getMessage();
	    }
		return "Success";
	}
	
	@Override
	public void onProgressUpdate(String... param) {
		dialog.setMessage(param[0]);
	}
	
	@Override
	public void onPostExecute(String result) {
		dialog.dismiss();
		
		AlertDialog.Builder builder = new AlertDialog.Builder(ref.get());
		builder.setTitle("Result");
		builder.setMessage(result);
		builder.create().show();
	}
}