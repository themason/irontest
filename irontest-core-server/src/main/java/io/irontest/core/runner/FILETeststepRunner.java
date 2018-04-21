package io.irontest.core.runner;

import java.io.File;
import java.io.PrintStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.irontest.models.endpoint.Endpoint;
import io.irontest.models.endpoint.FILEEndpointProperties;
import io.irontest.models.teststep.Teststep;

/**
 * Created by Richard Mason on 18/04/18.
 */
public class FILETeststepRunner extends TeststepRunner {
    private static final Logger LOGGER = LoggerFactory.getLogger(FILETeststepRunner.class);

    protected BasicTeststepRun run(Teststep teststep) throws Exception {
        BasicTeststepRun basicTeststepRun = new BasicTeststepRun();
        Endpoint endpoint = teststep.getEndpoint();
        FILEEndpointProperties endpointProperties = (FILEEndpointProperties) endpoint.getOtherProperties(); 
        
        if (teststep.getAction().equals(Teststep.ACTION_WRITE)) {
        	File fileOut = new File(endpointProperties.getFilePath() + "\\" + endpointProperties.getFileName());
    		PrintStream fileOutStream = new PrintStream(fileOut);
    		fileOutStream.println(endpointProperties.getContents());
    		fileOutStream.flush();
    		fileOutStream.close();
        }
        
		return basicTeststepRun;
    }
}
