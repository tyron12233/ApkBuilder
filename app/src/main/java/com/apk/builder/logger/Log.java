package com.apk.builder.logger;

public class Log {
	
	private String mTag;
	private CharSequence mMessage;
	
	public Log(String tag, CharSequence message) {
		mTag = tag;
		mMessage = message;
	}
	
	public String getTag() {
		return mTag;
	}
	
	public CharSequence getMessage() {
		return mMessage;
	}
}