package io.irontest.core.runner;

import com.ibm.mq.*;
import com.ibm.mq.constants.CMQC;
import com.ibm.mq.headers.MQDataException;
import com.ibm.mq.headers.MQHeaderIterator;
import io.irontest.models.MQIIBEndpointProperties;
import io.irontest.models.MQTeststepProperties;
import io.irontest.models.Teststep;

import java.io.IOException;
import java.util.Hashtable;

/**
 * Created by Trevor Li on 7/14/15.
 */
public class MQTeststepRunner implements TeststepRunner {
    public Object run(Teststep teststep) throws MQException, IOException, MQDataException {
        Object result = null;
        MQIIBEndpointProperties endpointProperties = (MQIIBEndpointProperties) teststep.getEndpoint().getOtherProperties();
        MQTeststepProperties teststepProperties = (MQTeststepProperties) teststep.getOtherProperties();
        Hashtable qmConnProperties = new Hashtable();
        qmConnProperties.put(CMQC.HOST_NAME_PROPERTY,  endpointProperties.getHost());
        qmConnProperties.put(CMQC.PORT_PROPERTY, endpointProperties.getPort());
        qmConnProperties.put(CMQC.CHANNEL_PROPERTY, endpointProperties.getSvrConnChannelName());
        MQQueueManager queueManager = null;
        MQQueue queue = null;
        int openOptions = CMQC.MQOO_FAIL_IF_QUIESCING + CMQC.MQOO_INPUT_SHARED;
        try {
            //  connect to queue manager
            queueManager = new MQQueueManager(endpointProperties.getQueueManagerName(), qmConnProperties);

            //  open queue
            if (MQTeststepProperties.ACTION_TYPE_CHECK_DEPTH.equals(teststepProperties.getAction())) {
                openOptions += CMQC.MQOO_INQUIRE;
            }
            queue = queueManager.accessQueue(teststepProperties.getQueueName(), openOptions, null, null, null);

            //  do the action
            if (MQTeststepProperties.ACTION_TYPE_CLEAR.equals(teststepProperties.getAction())) {
                clearQueue(queue);
                result = true;
            } else if (MQTeststepProperties.ACTION_TYPE_CHECK_DEPTH.equals(teststepProperties.getAction())) {
                result = queue.getCurrentDepth();
            } else if (MQTeststepProperties.ACTION_TYPE_DEQUEUE.equals(teststepProperties.getAction())) {
                result = dequeue(queue);
            }
        } finally {
            if (queue != null) {
                queue.close();
            }
            if (queueManager != null) {
                queueManager.disconnect();
            }
        }

        return result;
    }

    private String dequeue(MQQueue queue) throws MQException, IOException, MQDataException {
        String result = null;
        MQGetMessageOptions getOptions = new MQGetMessageOptions();
        getOptions.options = CMQC.MQGMO_NO_WAIT + CMQC.MQGMO_FAIL_IF_QUIESCING;
        MQMessage message = new MQMessage();
        try {
            queue.get(message, getOptions);
            MQHeaderIterator it = new MQHeaderIterator(message);
            result = it.getBodyAsText();
        } catch(MQException mqEx) {
            if (mqEx.getCompCode() == CMQC.MQCC_FAILED && mqEx.getReason() == CMQC.MQRC_NO_MSG_AVAILABLE) {
                //  No more message available on the queue
            } else {
                throw mqEx;
            }
        }
        return result;
    }

    private void clearQueue(MQQueue queue) throws MQException {
        MQGetMessageOptions getOptions = new MQGetMessageOptions();
        getOptions.options = CMQC.MQGMO_NO_WAIT + CMQC.MQGMO_FAIL_IF_QUIESCING;
        while (true) {
            //  read message from queue
            MQMessage message = new MQMessage();
            try {
                queue.get(message, getOptions);
            } catch(MQException mqEx) {
                if (mqEx.getCompCode() == CMQC.MQCC_FAILED && mqEx.getReason() == CMQC.MQRC_NO_MSG_AVAILABLE) {
                    //  No more message available on the queue
                    break;
                } else {
                    throw mqEx;
                }
            }
        }
    }
}