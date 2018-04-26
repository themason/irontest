package io.irontest.models.teststep;

import io.irontest.models.Properties;

/**
 * Created by Richard on 13/04/2018.
 */
public class FILETeststepProperties extends Properties {
	private String fileName;
	private String filePath;
	private String contents;
	private String destinationType;
	private String protocol;
	private String hostname;
	private String port;
	private String username;
	private String password;
    
    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }
    
    public String getContents() {
    	return contents;
    }
    
    public void setContents(String contents) {
    	this.contents = contents;
    }
    
    public String getDestinationType() {
    	return destinationType;
    }
    
    public void setDestinationType(String destinationType) {
    	this.destinationType = destinationType;
    }
    
    public String getProtocol() {
    	return protocol;
    }
    
    public void setProtocol(String protocol) {
    	this.protocol = protocol;
    }
    
    public String getHostname() {
    	return hostname;
    }
    
    public void setHostname(String hostname) {
    	this.hostname = hostname;
    }
    
    public String getPort() {
    	return port;
    }
    
    public void setPort(String port) {
    	this.port = port;
    }
    
    public String getUsername() {
    	return username;
    }
    
    public void setUsername(String username) {
    	this.username = username;
    }
    
    public String getPassword() {
    	return password;
    }
    
    public void setPassword(String password) {
    	this.password = password;
    }
}