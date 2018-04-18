package io.irontest.models;

import com.fasterxml.jackson.annotation.JsonView;
import io.irontest.resources.ResourceJsonViews;

/**
 * Created by Zheng on 16/03/2018.
 */
public class DataTableColumn {
    public static String COLUMN_NAME_CAPTION = "Caption";
    @JsonView(ResourceJsonViews.DataTableUIGrid.class)
    private long id;
    @JsonView(ResourceJsonViews.DataTableUIGrid.class)
    private String name;
    @JsonView(ResourceJsonViews.DataTableUIGrid.class)
    private DataTableColumnType type;
    private short sequence;

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

    public DataTableColumnType getType() {
        return type;
    }

    public void setType(DataTableColumnType type) {
        this.type = type;
    }

    public short getSequence() {
        return sequence;
    }

    public void setSequence(short sequence) {
        this.sequence = sequence;
    }
}
