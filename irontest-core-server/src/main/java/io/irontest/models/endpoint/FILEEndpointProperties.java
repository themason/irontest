package io.irontest.models.endpoint;

import io.irontest.models.Properties;

/**
 * Created by Zheng on 29/07/2017.
 */
public class FILEEndpointProperties extends Properties {
	private FileDestinationType destinationType = FileDestinationType.LOCAL_NETWORK;		// LOCAL_NETWORK is the default file transfer mode
	private FileTransferProtocol transferProtocol = FileTransferProtocol.FTP;		// FTP is the default transfer protocol mode
	private String fileName;
    private String filePath;
    private String contents;
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
    
    public FileDestinationType getDestinationType() {
    	return destinationType;
    }
    
    public void setDestinationType(FileDestinationType destinationType) {
    	this.destinationType = destinationType;
    }
    
    public FileTransferProtocol getTransferProtocol() {
    	return transferProtocol;
    }
    
    public void setTransferProtocol(FileTransferProtocol transferProtocol) {
    	this.transferProtocol = transferProtocol;
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