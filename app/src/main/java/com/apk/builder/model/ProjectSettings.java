package com.apk.builder.model;

import java.util.Map;
import java.util.HashMap;

public class ProjectSettings {
    
    public static final String KOTLIN_ENABLED = "kotlinEnabled";
    
	private Map<String, Object> map = new HashMap<>();
	
    public ProjectSettings() {
    
    }
	
	public boolean getBoolean(String key, boolean def) {
		Boolean bool = (Boolean) get(key);
		if (bool == null) {
			return def;
		}
		return def;
	}
	
    public boolean getBoolean(String key) {
        return (Boolean) get(key);
    }
	
	public void put(String key, Object val) {
		map.put(key, val);
	}
	
	public Object get(String key) {
	    return map.get(key);
	}
}