package io.irontest.db;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.irontest.models.AppMode;
import io.irontest.models.endpoint.Endpoint;
import io.irontest.models.endpoint.MQConnectionMode;
import io.irontest.models.endpoint.MQEndpointProperties;
import io.irontest.models.endpoint.SOAPEndpointProperties;
import io.irontest.models.teststep.Teststep;
import org.skife.jdbi.v2.sqlobject.*;
import org.skife.jdbi.v2.sqlobject.customizers.RegisterMapper;

import java.util.List;

import static io.irontest.IronTestConstants.DB_UNIQUE_NAME_CONSTRAINT_NAME_SUFFIX;
import static io.irontest.IronTestConstants.ENDPOINT_PASSWORD_ENCRYPTION_KEY;

/**
 * Created by Trevor Li on 6/30/15.
 */
@RegisterMapper(EndpointMapper.class)
public abstract class EndpointDAO {
    @SqlUpdate("CREATE SEQUENCE IF NOT EXISTS endpoint_sequence START WITH 1 INCREMENT BY 1 NOCACHE")
    public abstract void createSequenceIfNotExists();

    @SqlUpdate("CREATE TABLE IF NOT EXISTS endpoint (id BIGINT DEFAULT endpoint_sequence.NEXTVAL PRIMARY KEY, " +
            "environment_id int, name varchar(200) NOT NULL DEFAULT CURRENT_TIMESTAMP, type varchar(20) NOT NULL, " +
            "description CLOB, url varchar(1000), username varchar(200), password varchar(500), other_properties CLOB, " +
            "created TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, " +
            "updated TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, " +
            "FOREIGN KEY (environment_id) REFERENCES environment(id) ON DELETE CASCADE, " +
            "CONSTRAINT ENDPOINT_" + DB_UNIQUE_NAME_CONSTRAINT_NAME_SUFFIX + " UNIQUE(environment_id, type, name))")
    public abstract void createTableIfNotExists();

    @SqlUpdate("insert into endpoint (environment_id, type, other_properties) values (:evId, :type, :otherProperties)")
    @GetGeneratedKeys
    protected abstract long _insertManagedEndpoint(@Bind("evId") long environmentId, @Bind("type") String type,
                                                   @Bind("otherProperties") String otherProperties);

    @SqlUpdate("update endpoint set name = :name where id = :id")
    protected abstract long updateNameForInsert(@Bind("id") long id, @Bind("name") String name);

    @Transaction
    public long insertManagedEndpoint(Endpoint endpoint) throws JsonProcessingException {
        String otherProperties = new ObjectMapper().writeValueAsString(endpoint.getOtherProperties());
        long id = _insertManagedEndpoint(endpoint.getEnvironment().getId(), endpoint.getType(), otherProperties);
        updateNameForInsert(id, "Endpoint " + id);
        return id;
    }

    /**
     * Here assuming endpoint.password is already encrypted.
     * @param endpoint
     * @param otherProperties
     * @return
     */
    @SqlUpdate("insert into endpoint (name, type, description, url, username, password, other_properties) " +
               "values (:ep.name, :ep.type, :ep.description, :ep.url, :ep.username, :ep.password, :otherProperties)")
    @GetGeneratedKeys
    protected abstract long _insertUnmanagedEndpoint(@BindBean("ep") Endpoint endpoint,
                                                  @Bind("otherProperties") String otherProperties);

    public Endpoint createUnmanagedEndpoint_NoTransaction(String teststepType, AppMode appMode) throws JsonProcessingException {
        Endpoint endpoint = null;
        if (!Teststep.TYPE_WAIT.equals(teststepType)) {
            endpoint = new Endpoint();
            endpoint.setName("Unmanaged Endpoint");
            if (Teststep.TYPE_SOAP.equals(teststepType)) {
                endpoint.setType(Endpoint.TYPE_SOAP);
                endpoint.setOtherProperties(new SOAPEndpointProperties());
            } else if (Teststep.TYPE_DB.equals(teststepType)) {
                endpoint.setType(Endpoint.TYPE_DB);
            } else if (Teststep.TYPE_MQ.equals(teststepType)) {
                endpoint.setType(Endpoint.TYPE_MQ);
                MQEndpointProperties endpointProperties = new MQEndpointProperties();
                endpointProperties.setConnectionMode(
                        appMode == AppMode.LOCAL ? MQConnectionMode.BINDINGS : MQConnectionMode.CLIENT);
                endpoint.setOtherProperties(endpointProperties);
            } else if (Teststep.TYPE_IIB.equals(teststepType)) {
                endpoint.setType(Endpoint.TYPE_IIB);
            } else if (Teststep.TYPE_FILE.equals(teststepType)) {
            	endpoint.setType(Endpoint.TYPE_FILE);
            }
            long id = insertUnmanagedEndpoint_NoTransaction(endpoint);
            endpoint.setId(id);
        }

        return endpoint;
    }

    public long insertUnmanagedEndpoint_NoTransaction(Endpoint endpoint) throws JsonProcessingException {
        String otherProperties = new ObjectMapper().writeValueAsString(endpoint.getOtherProperties());
        return _insertUnmanagedEndpoint(endpoint, otherProperties);
    }

    @SqlUpdate("update endpoint set environment_id = :evId, name = :ep.name, type = :ep.type, " +
            "description = :ep.description, url = :ep.url, username = :ep.username, password = CASE " +
                "WHEN COALESCE(password, '') <> COALESCE(:ep.password, '') " + // encrypt only when password is changed
                    "THEN ENCRYPT('AES', '" + ENDPOINT_PASSWORD_ENCRYPTION_KEY + "', STRINGTOUTF8(:ep.password)) " +
                "ELSE password END, " +
            "other_properties = :otherProperties, updated = CURRENT_TIMESTAMP where id = :ep.id")
    protected abstract int _update(@BindBean("ep") Endpoint endpoint, @Bind("evId") Long environmentId,
                                   @Bind("otherProperties") String otherProperties);

    public int update(Endpoint endpoint) throws JsonProcessingException {
        String otherProperties = new ObjectMapper().writeValueAsString(endpoint.getOtherProperties());
        Long environmentId = endpoint.getEnvironment() == null ? null : endpoint.getEnvironment().getId();
        return _update(endpoint, environmentId, otherProperties);
    }

    @SqlUpdate("delete from endpoint where id = :id")
    public abstract void deleteById(@Bind("id") long id);

    @SqlQuery(
            "select ep.*, ev.name as environment_name " +
            "from endpoint ep left outer join environment ev on ep.environment_id = ev.id " +
            "where ep.id = :id")
    public abstract Endpoint findById(@Bind("id") long id);

    @SqlQuery("select id, environment_id, name, type, description from endpoint where environment_id = :environmentId")
    public abstract List<Endpoint> findByEnvironmentId_EnvironmentEditView(@Bind("environmentId") long environmentId);

    @SqlQuery(
            "select ep.id, ep.environment_id, ev.name as environment_name, ep.name, ep.type, ep.description " +
            "from endpoint ep left outer join environment ev on ep.environment_id = ev.id " +
            "where ep.type = :endpointType and ep.environment_id is not null")
    public abstract List<Endpoint> findManagedEndpointsByType(@Bind("endpointType") String endpointType);

    @SqlUpdate("delete from endpoint where environment_id is null and id = :id")
    public abstract void deleteUnmanagedEndpointById(@Bind("id") long id);
}
