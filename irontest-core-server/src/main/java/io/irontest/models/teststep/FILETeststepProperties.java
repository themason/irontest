package io.irontest.models.teststep;

import io.irontest.models.Properties;

/**
 * Created by Richard on 13/04/2018.
 */
public class FILETeststepProperties extends Properties {
	private String fileName;
	private String filePath;
	private String contents;
    
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
}