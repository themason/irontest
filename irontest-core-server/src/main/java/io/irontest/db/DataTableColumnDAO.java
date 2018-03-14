package io.irontest.db;

import org.skife.jdbi.v2.sqlobject.SqlUpdate;

import static io.irontest.IronTestConstants.DB_UNIQUE_NAME_CONSTRAINT_NAME_SUFFIX;

/**
 * Created by Zheng on 14/03/2018.
 */
public abstract class DataTableColumnDAO {
    @SqlUpdate("CREATE SEQUENCE IF NOT EXISTS datatable_column_sequence START WITH 1 INCREMENT BY 1 NOCACHE")
    public abstract void createSequenceIfNotExists();

    @SqlUpdate("CREATE TABLE IF NOT EXISTS datatable_column (" +
            "id BIGINT DEFAULT datatable_column_sequence.NEXTVAL PRIMARY KEY, name VARCHAR(200) NOT NULL, " +
            "type VARCHAR(50) NOT NULL, sequence SMALLINT NOT NULL, testcase_id BIGINT NOT NULL, " +
            "created TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, " +
            "updated TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, " +
            "FOREIGN KEY (testcase_id) REFERENCES testcase(id) ON DELETE CASCADE, " +
            "CONSTRAINT DATATABLE_COLUMN_" + DB_UNIQUE_NAME_CONSTRAINT_NAME_SUFFIX + " UNIQUE(testcase_id, name))")
    public abstract void createTableIfNotExists();
}