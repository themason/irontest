package io.irontest.models.endpoint;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonView;
import io.irontest.models.Environment;
import io.irontest.models.Properties;
import io.irontest.resources.ResourceJsonViews;

/**
 * Created by Trevor Li on 6/30/15.
 */
public class Endpoint {
    public static final String TYPE_SOAP = "SOAP";
    public static final String TYPE_DB = "DB";
    public static final String TYPE_MQ = "MQ";
    public static final String TYPE_IIB = "IIB";
    public static final String TYPE_FILE = "FILE";
    private long id;
    @JsonView(ResourceJsonViews.DataTableUIGrid.class)
    private Environment environment;
    @JsonView(ResourceJsonViews.DataTableUIGrid.class)
    private String name;
    private String type;
    private String description;
    private String url;                //  can be SOAP address, JDBC URL, etc.; not used by MQ or IIB endpoint
    private String username;
    private String password;
    @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXTERNAL_PROPERTY,
            property = "type", visible = true, defaultImpl = Properties.class)
    @JsonSubTypes({
            @JsonSubTypes.Type(value = SOAPEndpointProperties.class, name = Endpoint.TYPE_SOAP),
            @JsonSubTypes.Type(value = MQEndpointProperties.class, name = Endpoint.TYPE_MQ),
            @JsonSubTypes.Type(value = IIBEndpointProperties.class, name = Endpoint.TYPE_IIB),
            @JsonSubTypes.Type(value = FILEEndpointProperties.class, name = Endpoint.TYPE_FILE)
    })
    private Properties otherProperties = new Properties();

    public Endpoint() {}

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
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

    public Environment getEnvironment() {
        return environment;
    }

    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    public Properties getOtherProperties() {
        return otherProperties;
    }

    public void setOtherProperties(Properties otherProperties) {
        this.otherProperties = otherProperties;
    }

    @JsonIgnore
    public boolean isManaged() {
        return this.environment != null;
    }
}
