package com.apk.builder;

import android.app.Application;
import android.app.PendingIntent;
import android.app.AlarmManager;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;


public class ApplicationLoader extends Application {
	
	private Thread.UncaughtExceptionHandler uncaughtExceptionHandler;
	
	public static Context applicationContext;
	public static volatile Handler applicationHandler;
	
	@Override
	public void onCreate() {
		this.uncaughtExceptionHandler = Thread.getDefaultUncaughtExceptionHandler();
		
		Thread.setDefaultUncaughtExceptionHandler((thread, ex) -> {
			Intent intent = new Intent(getApplicationContext(), DebugActivity.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);

			String error = getStackTrace(ex);
			
			FileUtil.writeFile("/sdcard/.1TapSlide/log.txt", error);
			/*intent.putExtra("error", error);


			PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 11111, intent, PendingIntent.FLAG_ONE_SHOT);


			AlarmManager am = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
			am.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, 1000, pendingIntent);
			*/
			android.os.Process.killProcess(android.os.Process.myPid());
			System.exit(2);
			
			uncaughtExceptionHandler.uncaughtException(thread, ex);
		});
		super.onCreate();
		
		applicationContext = this;
		applicationHandler = new Handler(applicationContext.getMainLooper());
		
	}
	
	private String getStackTrace(Throwable th){
		final Writer result = new StringWriter();
		
		final PrintWriter printWriter = new PrintWriter(result);
		Throwable cause = th;
		
		while(cause != null){
			cause.printStackTrace(printWriter);
			cause = cause.getCause();
		}
		final String stacktraceAsString = result.toString();
		printWriter.close();
		
		return stacktraceAsString;
	}
	
	public static Context getContext() {
	    return applicationContext;
	}
	
}
