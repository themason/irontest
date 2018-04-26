package io.irontest.resources;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.irontest.core.runner.*;
import io.irontest.db.TeststepDAO;
import io.irontest.db.UserDefinedPropertyDAO;
import io.irontest.db.UtilsDAO;
import io.irontest.models.AppInfo;
import io.irontest.models.DataTable;
import io.irontest.models.UserDefinedProperty;
import io.irontest.models.endpoint.Endpoint;
import io.irontest.models.teststep.*;
import io.irontest.utils.IronTestUtils;
import io.irontest.utils.XMLUtils;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;

import javax.annotation.security.PermitAll;
import javax.ws.rs.*;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.InputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.irontest.IronTestConstants.IMPLICIT_PROPERTY_DATE_TIME_FORMAT;
import static io.irontest.IronTestConstants.IMPLICIT_PROPERTY_NAME_TEST_STEP_START_TIME;

/**
 * Created by Zheng on 11/07/2015.
 */
@Path("/testcases/{testcaseId}/teststeps") @Produces({ MediaType.APPLICATION_JSON })
public class TeststepResource {
    private final AppInfo appInfo;
    private final TeststepDAO teststepDAO;
    private final UserDefinedPropertyDAO udpDAO;
    private final UtilsDAO utilsDAO;

    public TeststepResource(AppInfo appInfo, TeststepDAO teststepDAO, UserDefinedPropertyDAO udpDAO, UtilsDAO utilsDAO) {
        this.appInfo = appInfo;
        this.teststepDAO = teststepDAO;
        this.udpDAO = udpDAO;
        this.utilsDAO = utilsDAO;
    }

    @POST
    @PermitAll
    public Teststep create(Teststep teststep) throws JsonProcessingException {
        preCreationProcess(teststep);

        long teststepId = teststepDAO.insert(teststep, appInfo.getAppMode());

        return teststepDAO.findById(teststepId);
    }

    //  adding more info to the teststep object
    private void preCreationProcess(Teststep teststep) {
        //  create sample request
        String sampleRequest = null;
        if (Teststep.TYPE_DB.equals(teststep.getType())){
            sampleRequest = "select * from ? where ?";
        }
        teststep.setRequest(sampleRequest);

        //  set initial/default property values (in the Properties sub-class)
        if (Teststep.TYPE_SOAP.equals(teststep.getType())) {
            teststep.setOtherProperties(new SOAPTeststepProperties());
        } else if (Teststep.TYPE_MQ.equals(teststep.getType())) {
            teststep.setOtherProperties(new MQTeststepProperties());
        } else if (Teststep.TYPE_FILE.equals(teststep.getType())) {
        	teststep.setOtherProperties(new FILETeststepProperties());
        } else if (Teststep.TYPE_WAIT.equals(teststep.getType())) {
            teststep.setOtherProperties(new WaitTeststepProperties(1000));   //  there is no point to wait for 0 milliseconds
        }
    }

    private void populateParametersInWrapper(TeststepWrapper wrapper) {
        Teststep teststep = wrapper.getTeststep();
        if (Teststep.TYPE_DB.equals(teststep.getType())) {
            boolean isSQLRequestSingleSelectStatement;
            try {
                isSQLRequestSingleSelectStatement = IronTestUtils.isSQLRequestSingleSelectStatement(
                        (String) teststep.getRequest());
            } catch (Exception e) {
                //  the SQL script is invalid, so it can't be a single select statement
                //  swallow the exception to avoid premature error message on UI (user is still editing the SQL script)
                //  the exception will popup on UI when user executes the script (the script will be reparsed at that time)
                isSQLRequestSingleSelectStatement = false;
            }
            wrapper.getParameters().put("isSQLRequestSingleSelectStatement", isSQLRequestSingleSelectStatement);
        }
    }

    @GET @Path("{teststepId}")
    public TeststepWrapper findById(@PathParam("teststepId") long teststepId) throws Exception {
        TeststepWrapper wrapper = new TeststepWrapper();
        Teststep teststep = teststepDAO.findById(teststepId);
        wrapper.setTeststep(teststep);
        populateParametersInWrapper(wrapper);

        return wrapper;
    }

    @PUT @Path("{teststepId}")
    @PermitAll
    public TeststepWrapper update(Teststep teststep) throws Exception {
        Thread.sleep(100);  //  workaround for Chrome's 'Failed to load response data' problem (still exist in Chrome 65)

        TeststepWrapper wrapper = new TeststepWrapper();
        teststep = teststepDAO.update(teststep);
        wrapper.setTeststep(teststep);
        populateParametersInWrapper(wrapper);

        return wrapper;
    }

    @DELETE @Path("{teststepId}")
    @PermitAll
    public void delete(@PathParam("teststepId") long teststepId) {
        teststepDAO.deleteById(teststepId);
    }

    /**
     * Run a test step individually (not as part of test case running).
     * This is a stateless operation, i.e. not persisting anything in database.
     * @param teststep
     * @return
     */
    @POST @Path("{teststepId}/run")
    @PermitAll
    public BasicTeststepRun run(Teststep teststep) throws Exception {
        Thread.sleep(100);  //  workaround for Chrome's 'Failed to load response data' problem (still exist in Chrome 65)

        //  gather referenceable string properties and endpoint properties
        List<UserDefinedProperty> testcaseUDPs = udpDAO.findByTestcaseId(teststep.getTestcaseId());
        Map<String, String> referenceableStringProperties = IronTestUtils.udpListToMap(testcaseUDPs);
        referenceableStringProperties.put(IMPLICIT_PROPERTY_NAME_TEST_STEP_START_TIME,
                IMPLICIT_PROPERTY_DATE_TIME_FORMAT.format(new Date()));
        Map<String, Endpoint> referenceableEndpointProperties = new HashMap<>();
        DataTable dataTable = utilsDAO.getTestcaseDataTable(teststep.getTestcaseId(), true);
        if (dataTable.getRows().size() == 1) {
            referenceableStringProperties.putAll(dataTable.getStringPropertiesInRow(0));
            referenceableEndpointProperties.putAll(dataTable.getEndpointPropertiesInRow(0));
        }

        //  run the test step
        TeststepRunner teststepRunner = TeststepRunnerFactory.getInstance().newTeststepRunner(
                teststep, teststepDAO, utilsDAO, referenceableStringProperties, referenceableEndpointProperties, null);
        BasicTeststepRun basicTeststepRun = teststepRunner.run();

        //  for better display in browser, transform XML response to be pretty-printed
        if (Teststep.TYPE_SOAP.equals(teststep.getType())) {
            SOAPAPIResponse soapAPIResponse = (SOAPAPIResponse) basicTeststepRun.getResponse();
            soapAPIResponse.setHttpBody(XMLUtils.prettyPrintXML(soapAPIResponse.getHttpBody()));
        } else if (Teststep.TYPE_MQ.equals(teststep.getType()) &&
                Teststep.ACTION_DEQUEUE.equals(teststep.getAction())) {
            MQAPIResponse mqAPIResponse = (MQAPIResponse) basicTeststepRun.getResponse();
            mqAPIResponse.setValue(XMLUtils.prettyPrintXML((String) mqAPIResponse.getValue()));
        }

        return basicTeststepRun;
    }

    /**
     * Save the uploaded file as Teststep.request.
     * Use @POST instead of @PUT because ng-file-upload seems not working with PUT.
     * @param teststepId
     * @param inputStream
     * @param contentDispositionHeader
     * @return
     */
    @POST @Path("{teststepId}/requestFile")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @PermitAll
    public Teststep saveRequestFile(@PathParam("teststepId") long teststepId,
                                      @FormDataParam("file") InputStream inputStream,
                                      @FormDataParam("file") FormDataContentDisposition contentDispositionHeader) {
        return teststepDAO.setRequestFile(teststepId, contentDispositionHeader.getFileName(), inputStream);
    }

    /**
     * Download Teststep.request as a file.
     * @param teststepId
     * @return
     */
    @GET @Path("{teststepId}/requestFile")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response getRequestFile(@PathParam("teststepId") long teststepId) {
        Teststep teststep = teststepDAO.findById(teststepId);
        teststep.setRequest(teststepDAO.getBinaryRequestById(teststep.getId()));
        String filename = teststep.getRequestFilename() == null ? "UnknownFilename" : teststep.getRequestFilename();
        return Response.ok(teststep.getRequest())
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .build();
    }

    @POST @Path("{teststepId}/useEndpointProperty")
    @PermitAll
    public Teststep useEndpointProperty(Teststep teststep) {
        teststepDAO.useEndpointProperty(teststep);

        return teststepDAO.findById(teststep.getId());
    }

    @POST @Path("{teststepId}/useDirectEndpoint")
    @PermitAll
    public Teststep useDirectEndpoint(Teststep teststep) throws JsonProcessingException {
        teststepDAO.useDirectEndpoint(teststep, appInfo.getAppMode());

        return teststepDAO.findById(teststep.getId());
    }
}
