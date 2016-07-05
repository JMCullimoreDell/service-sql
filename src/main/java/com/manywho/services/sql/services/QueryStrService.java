package com.manywho.services.sql.services;

import com.healthmarketscience.sqlbuilder.*;
import com.manywho.sdk.api.run.elements.type.ListFilter;
import com.manywho.sdk.api.run.elements.type.MObject;
import com.manywho.sdk.api.run.elements.type.ObjectDataType;
import com.manywho.sdk.api.run.elements.type.Property;
import com.manywho.services.sql.ServiceConfiguration;
import com.manywho.services.sql.entities.TableMetadata;
import com.manywho.services.sql.services.filter.QueryFilterConditions;
import com.manywho.services.sql.utilities.ScapeForTablesUtil;

import javax.inject.Inject;
import java.util.Set;

public class QueryStrService {
    private QueryFilterConditions queryFilterConditions;
    private ScapeForTablesUtil scapeForTablesUtil;

    @Inject
    public QueryStrService(QueryFilterConditions queryFilterConditions, ScapeForTablesUtil scapeForTablesUtil) {
        this.queryFilterConditions = queryFilterConditions;
        this.scapeForTablesUtil = scapeForTablesUtil;
    }

    public String createQueryWithParametersForSelectByPrimaryKey(TableMetadata tableMetadata, Set<String> primaryKeyNames, ServiceConfiguration configuration) {

        SelectQuery selectQuery = new SelectQuery().addAllColumns()
                .addCustomFromTable(scapeForTablesUtil.scapeTableName(configuration.getDatabaseType(),tableMetadata.getSchemaName(), tableMetadata.getTableName()));

        for (String key: primaryKeyNames) {
            selectQuery.addCondition(BinaryCondition.equalTo(new CustomSql(key), new CustomSql(":" + key)));
        }

        return selectQuery.validate().toString();
    }

    public String createQueryWithParametersForUpdate(MObject mObject, TableMetadata tableMetadata, Set<String> primaryKeyNames, ServiceConfiguration configuration){

        UpdateQuery updateQuery = new UpdateQuery(
                scapeForTablesUtil.scapeTableName(configuration.getDatabaseType(), tableMetadata.getSchemaName(), tableMetadata.getTableName()));

        for(Property p : mObject.getProperties()) {
            updateQuery.addCustomSetClause(new CustomSql(p.getDeveloperName()), new CustomSql(":" + p.getDeveloperName()));
        }

        for (String key: primaryKeyNames) {
            updateQuery.addCondition(BinaryCondition.equalTo(new CustomSql(key), new CustomSql(":" + key)));
        }

        return updateQuery.validate().toString();
    }

    public String createQueryWithParametersForInsert(MObject mObject, TableMetadata tableMetadata, ServiceConfiguration configuration) {
        InsertQuery insertQuery = new InsertQuery(
                scapeForTablesUtil.scapeTableName(configuration.getDatabaseType(), tableMetadata.getSchemaName(), tableMetadata.getTableName()));

        for(Property p : mObject.getProperties()) {
            insertQuery.addCustomColumn(new CustomSql(p.getDeveloperName()), new CustomSql(":" + p.getDeveloperName()));
        }

        return  insertQuery.validate().toString();
    }

    public String getSqlFromFilter(ServiceConfiguration configuration, ObjectDataType objectDataType, ListFilter filter, TableMetadata tableMetadata) {

        SelectQuery selectQuery = new SelectQuery().addAllColumns()
                .addCustomFromTable(scapeForTablesUtil.scapeTableName(configuration.getDatabaseType(), configuration.getDatabaseSchema(), objectDataType.getDeveloperName()));

        queryFilterConditions.addSearch(selectQuery, filter.getSearch(), objectDataType.getProperties());
        queryFilterConditions.addWhere(selectQuery, filter.getWhere(), filter.getComparisonType());
        queryFilterConditions.addOffset(selectQuery, configuration.getDatabaseType(), filter.getOffset(), filter.getLimit());

        queryFilterConditions.addOrderBy(selectQuery, filter.getOrderByPropertyDeveloperName(), filter.getOrderByDirectionType(), tableMetadata);

        return selectQuery.validate().toString();
    }
}
