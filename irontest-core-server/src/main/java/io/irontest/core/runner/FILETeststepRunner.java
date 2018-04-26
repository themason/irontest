package io.irontest.core.runner;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.ChannelSftp.LsEntry;
import com.jcraft.jsch.ChannelSftp.LsEntrySelector;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;

import io.irontest.models.endpoint.Endpoint;
import io.irontest.models.endpoint.FILEEndpointProperties;
import io.irontest.models.endpoint.FileDestinationType;
import io.irontest.models.endpoint.FileTransferProtocol;
import io.irontest.models.teststep.Teststep;

/**
 * Created by Richard Mason on 18/04/18.
 */
public class FILETeststepRunner extends TeststepRunner {
    private static final Logger LOGGER = LoggerFactory.getLogger(FILETeststepRunner.class);
    private FTPClient client;
    private ChannelSftp sftpChannel;
    private Session session;
    private Channel channel;

    protected BasicTeststepRun run(Teststep teststep) throws Exception {
        BasicTeststepRun basicTeststepRun = new BasicTeststepRun();
        Endpoint endpoint = teststep.getEndpoint();
        FILEEndpointProperties endpointProperties = (FILEEndpointProperties) endpoint.getOtherProperties(); 
        String fileName = endpointProperties.getFileName();
        String filePath = endpointProperties.getFilePath();
        String contents = (String) teststep.getRequest();
        final FILEAPIResponse apiResponse = new FILEAPIResponse();
        
        if (endpointProperties.getDestinationType().equals(FileDestinationType.FTP_SFTP)) {
        	if (endpointProperties.getTransferProtocol().equals(FileTransferProtocol.SFTP)) {
        		initialiseSFTPConnection(endpointProperties);
        		if (teststep.getAction().equals(Teststep.ACTION_WRITE)) {
            		writeSFTP(fileName, filePath, contents);
            		apiResponse.setValue(null);
            	} else if (teststep.getAction().equals(Teststep.ACTION_READ)) {
            		ByteArrayOutputStream stream = (ByteArrayOutputStream) readSFTP(fileName, filePath);
            		apiResponse.setValue(stream.toString());
            	}
            	closeSFTPConnection();
        	} else if (endpointProperties.getTransferProtocol().equals(FileTransferProtocol.FTP)) {
        		initialiseFTPConnection(endpointProperties);
        		if (teststep.getAction().equals(Teststep.ACTION_WRITE)) {
            		writeFTP(endpointProperties);
            		apiResponse.setValue(null);
            	} else if (teststep.getAction().equals(Teststep.ACTION_READ)) {
            		ByteArrayOutputStream stream = (ByteArrayOutputStream) readFTP(fileName, filePath);
            		apiResponse.setValue(stream.toString());
            	}
            	closeFTPConnection();
        	}
        } else {
	        if (teststep.getAction().equals(Teststep.ACTION_WRITE)) {
	        	File fileOut = new File(filePath + "\\" + fileName);
	    		PrintStream fileOutStream = new PrintStream(fileOut);
	    		fileOutStream.println(contents);
	    		fileOutStream.flush();
	    		fileOutStream.close();
	        }
        }
        
        basicTeststepRun.setResponse(apiResponse);
		return basicTeststepRun;
    }

	/**
	 * Sends a messages to the FTP server
	 * @param msg
	 * @throws IOException
	 */
	public boolean writeFTP(FILEEndpointProperties endpointProperties) throws IOException{
		String fileName = endpointProperties.getFileName();
		String filePath = endpointProperties.getFilePath();
		String contents = endpointProperties.getContents();

		InputStream inStream = null;
		
		boolean processed = false;

        try {
        	inStream = new ByteArrayInputStream(contents.getBytes((StandardCharsets.UTF_8)));	
            client.storeFile(filePath + fileName, inStream);
        		
        	processed = true;
        } finally {
			if (inStream != null) {
				inStream.close();
			}
			client.logout();
		}

		return processed;
	}
	
    /**
	 * Sends a messages to the FTP server
	 * @param msg
	 * @throws IOException
	 */
	public boolean writeSFTP(String fileName, String filePath, String contents) throws IOException{
		InputStream inStream = null;
		
		boolean processed = false;

        try {
            	inStream = new ByteArrayInputStream(contents.getBytes((StandardCharsets.UTF_8)));	
            	
            	channel.connect();            	
        		sftpChannel.put(inStream, filePath + fileName);
        		
        		processed = true;
        } catch (JSchException e1) {
			LOGGER.info(e1.getMessage());
			e1.printStackTrace();
		} catch (SftpException e1) {
			LOGGER.info(e1.getMessage());
		} finally {
			if (inStream != null) {
				inStream.close();
			}
		}

		return processed;
	}
	
	private OutputStream readSFTP(final String fileName, final String filePath) {	
		OutputStream outStream = null;

		try {
			channel.connect();
		} catch (JSchException e) {
			LOGGER.info(e.getMessage());
		}
    	outStream = new ByteArrayOutputStream();
    	
    	final List<LsEntry> files = new ArrayList<LsEntry>();
    	
    	try {
    		LsEntrySelector selector = new ChannelSftp.LsEntrySelector() {				
				@Override
				public int select(LsEntry lsEntry) {
					if (lsEntry.getFilename().equals(fileName)) {
						files.add(lsEntry);
					}
					return CONTINUE;
				}
			};
			sftpChannel.ls(filePath, selector);
			if (files.size() > 0) {
				sftpChannel.get(filePath + fileName, outStream);
				sftpChannel.rm(filePath + fileName);
			}			
		} catch (SftpException e) {
			LOGGER.info(e.getMessage());
		}
    	try {
			outStream.flush();
		} catch (IOException e) {
			LOGGER.info(e.getMessage());
		}
    	
		return outStream;
	}
	
	private OutputStream readFTP(String fileName, String filePath) {		
		OutputStream outStream = new ByteArrayOutputStream();

		try {
			client.retrieveFile(filePath + fileName, outStream);
			outStream.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			LOGGER.info("Failed to retrieve file from destination: " + e.getMessage());
		} finally {
			try {
				outStream.close();
			} catch (IOException e) {
				LOGGER.info("Failed to close output stream");
			}
		}
    	
		return outStream;
	}
	
	private void initialiseFTPConnection(FILEEndpointProperties endpointProperties) {
		String hostname = endpointProperties.getHostname();
		String port = endpointProperties.getPort();
		String username = endpointProperties.getUsername();
		String password = endpointProperties.getPassword();
		
		client = new FTPClient();

        try {
        	/* create the connection to the server */
            client.connect(hostname, Integer.parseInt(port));
            client.login(username, password);
            client.setFileTransferMode(FTP.STREAM_TRANSFER_MODE);
            client.setFileType(FTP.ASCII_FILE_TYPE);
        } catch (IOException e) {
            LOGGER.info("Failed to create connection" + e.getMessage());
        }
	}
	
	private void initialiseSFTPConnection(FILEEndpointProperties endpointProperties) {
		String hostname = endpointProperties.getHostname();
		String port = endpointProperties.getPort();
		String username = endpointProperties.getUsername();
		String password = endpointProperties.getPassword();
		
		JSch jsch = new JSch();

        try {
        	jsch.setKnownHosts( "known_hosts" );

    		session = jsch.getSession(username, hostname, Integer.parseInt(port));
    		session.setPassword(password);
    		session.connect();
    		
    		channel = session.openChannel("sftp");
    		sftpChannel = (ChannelSftp) channel;
        } catch (JSchException e1) {
			LOGGER.info(e1.getMessage());
		} catch(Exception e2) {
			LOGGER.info(e2.getMessage());
		}
	}
	
	private void closeFTPConnection() {
		try {
			client.disconnect();
		} catch (IOException e) {
			LOGGER.info("Failed to disconnect from FTP client");
		}
	}
	
	private void closeSFTPConnection() {
		session.disconnect();
	}
}
