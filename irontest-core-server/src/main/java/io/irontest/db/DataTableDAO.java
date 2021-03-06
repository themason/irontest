package io.irontest.db;

import io.irontest.models.DataTable;
import io.irontest.models.DataTableCell;
import io.irontest.models.DataTableColumn;
import io.irontest.models.DataTableColumnType;
import org.skife.jdbi.v2.sqlobject.CreateSqlObject;
import org.skife.jdbi.v2.sqlobject.Transaction;

import java.util.*;

/**
 * Created by Zheng on 17/04/2018.
 */
public abstract class DataTableDAO {
    @CreateSqlObject
    protected abstract DataTableColumnDAO dataTableColumnDAO();

    @CreateSqlObject
    protected abstract DataTableCellDAO dataTableCellDAO();

    @CreateSqlObject
    protected abstract EndpointDAO endpointDAO();

    /**
     * Caption column is the initial column in a data table.
     * @param testcaseId
     */
    public void createCaptionColumn(long testcaseId) {
        DataTableColumn dataTableColumn = new DataTableColumn();
        dataTableColumn.setName(DataTableColumn.COLUMN_NAME_CAPTION);
        dataTableColumn.setSequence((short) 1);
        dataTableColumnDAO().insert(testcaseId, dataTableColumn, DataTableColumnType.STRING.toString());
    }

    /**
     * @param testcaseId
     * @param fetchFirstRowOnly if true, only the first data table row (if exists) will be fetched; if false, all rows will be fetched.
     * @return
     */
    @Transaction
    public DataTable getTestcaseDataTable(long testcaseId, boolean fetchFirstRowOnly) {
        DataTable dataTable = new DataTable();

        List<DataTableColumn> columns = dataTableColumnDAO().findByTestcaseId(testcaseId);

        //  populate the data table rows Java model column by column
        List<LinkedHashMap<String, DataTableCell>> rows = new ArrayList<>();
        Map<Short, LinkedHashMap<String, DataTableCell>> rowSequenceMap = new HashMap<>();  //  map rowSequence to row object (because rowSequence is not consecutive)
        for (DataTableColumn column: columns) {
            List<DataTableCell> cellsInColumn = dataTableCellDAO().findByColumnId(column.getId());
            for (DataTableCell cellInColumn: cellsInColumn) {
                short rowSequence = cellInColumn.getRowSequence();

                if (column.getType() != DataTableColumnType.STRING && cellInColumn.getEndpoint() != null) {
                    cellInColumn.setEndpoint(endpointDAO().findById(cellInColumn.getEndpoint().getId()));
                }

                if (!rowSequenceMap.containsKey(rowSequence)) {
                    LinkedHashMap<String, DataTableCell> row = new LinkedHashMap<>();
                    rowSequenceMap.put(rowSequence, row);
                    rows.add(row);
                }
                rowSequenceMap.get(rowSequence).put(column.getName(), cellInColumn);

                if (fetchFirstRowOnly && rows.size() == 1) {
                    break;
                }
            }
        }

        if (columns.size() > 0) {
            dataTable = new DataTable();
            dataTable.setColumns(columns);
            dataTable.setRows(rows);
        }

        return dataTable;
    }
}
