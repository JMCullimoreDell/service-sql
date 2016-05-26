package com.manywho.services.sql.entities;

import com.manywho.sdk.api.ContentType;
import java.util.HashMap;

/**
 * This object contains the metadata information about a table, it is only used internally
 *
 */
public class TableMetadata {

    private String tableName;
    private HashMap<String, ContentType> columns;
    private HashMap<String, String> columnsDatabaseType;
    private String primaryKeyName;
    private String primaryKeyType;

    public TableMetadata(String tableName){
        this.tableName = tableName;
        this.columns = new HashMap<>();
        this.columnsDatabaseType = new HashMap<>();
    }

    public void setColumn(String columnName, ContentType columnType) {
        columns.put(columnName, columnType);
    }
    public void setColumnsDatabaseType(String columnName, String columnType) {
        columnsDatabaseType.put(columnName, columnType);
    }

    public Boolean needToCastPrimaryKeyToInteger() {

        if(columns.get(primaryKeyName)!= null && columns.get(primaryKeyName) == ContentType.Number) {

            return true;
        }

        return false;
    }


    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public HashMap<String, ContentType> getColumns() {
        return columns;
    }

    public HashMap<String, String> getColumnsDatabaseType() {
        return columnsDatabaseType;
    }

    public String getPrimaryKeyName() {
        return primaryKeyName;
    }

    public void setPrimaryKeyName(String primaryKeyName) {
        this.primaryKeyName = primaryKeyName;
    }

    public String getPrimaryKeyType() {
        return primaryKeyType;
    }

    public void setPrimaryKeyType(String primaryKeyType) {
        this.primaryKeyType = primaryKeyType;
    }

}
