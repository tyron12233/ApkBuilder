package com.tyron.compiler;

public class CompilerResult {
	
	private boolean isError;
	private String message;
	
	public CompilerResult(String message, boolean error) {
		this.isError = error;
		this.message = message;
	}
	
	public String getMessage() {
		return message;
	}
	
	public boolean isError() {
		return isError;
	}
}
