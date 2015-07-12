package au.com.billon.stt.db;

import au.com.billon.stt.models.Teststep;
import org.skife.jdbi.v2.sqlobject.*;
import org.skife.jdbi.v2.sqlobject.customizers.RegisterMapper;

/**
 * Created by Zheng on 7/07/2015.
 */
@RegisterMapper(TeststepMapper.class)
public interface TeststepDAO {
    @SqlUpdate("create table IF NOT EXISTS teststep (" +
            "id INT PRIMARY KEY auto_increment, testcase_id INT, name varchar(200), description clob, " +
            "created timestamp DEFAULT CURRENT_TIMESTAMP, updated timestamp DEFAULT CURRENT_TIMESTAMP, " +
            "type varchar(20), request clob, FOREIGN KEY (testcase_id) REFERENCES testcase(id))")
    void createTableIfNotExists();

    @SqlUpdate("insert into teststep (testcase_id, name, description, request) values (:testcaseId, :name, :description, :request)")
    @GetGeneratedKeys
    long insert(@BindBean Teststep teststep);

    @SqlQuery("select * from teststep where id = :id")
    Teststep findById(@Bind("id") long id);
}