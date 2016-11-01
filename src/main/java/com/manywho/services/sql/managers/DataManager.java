package com.manywho.services.sql.managers;

import com.manywho.sdk.api.run.elements.type.ListFilter;
import com.manywho.sdk.api.run.elements.type.MObject;
import com.manywho.sdk.api.run.elements.type.ObjectDataType;
import com.manywho.services.sql.ServiceConfiguration;
import com.manywho.services.sql.entities.TableMetadata;
import com.manywho.services.sql.services.ConnectionService;
import com.manywho.services.sql.services.DataService;
import com.manywho.services.sql.services.QueryStrService;
import org.sql2o.Sql2o;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.List;

public class DataManager {

    private ConnectionService connectionService;
    private DataService dataService;
    private MetadataManager metadataManager;
    private QueryStrService queryStrService;

    @Inject
    public DataManager(ConnectionService connectionService, DataService dataService, MetadataManager metadataManager, QueryStrService queryStrService){
        this.connectionService = connectionService;
        this.dataService = dataService;
        this.metadataManager = metadataManager;
        this.queryStrService = queryStrService;
    }

    public List<MObject> load(ServiceConfiguration configuration, String objectDataType, HashMap<String, String> id) throws Exception {

        return dataService.fetchByPrimaryKey(
                metadataManager.getMetadataTable(configuration, objectDataType),
                        connectionService.getSql2Object(configuration), id, configuration);
    }

    public List<MObject> loadBySearch(ServiceConfiguration configuration, ObjectDataType objectDataType, ListFilter filters) throws Exception {
        TableMetadata tableMetadata =  metadataManager.getMetadataTable(configuration, objectDataType.getDeveloperName());

        return dataService.fetchBySearch(tableMetadata, connectionService.getSql2Object(configuration),
                queryStrService.getSqlFromFilter(configuration, objectDataType, filters, tableMetadata), configuration);
    }

    public MObject update(ServiceConfiguration configuration, MObject mObject, HashMap<String, String> primaryKeyHashMap) throws Exception {
        TableMetadata tableMetadata = metadataManager.getMetadataTable(configuration, mObject.getDeveloperName());
        Sql2o sql2o = connectionService.getSql2Object(configuration);
        dataService.update(mObject, sql2o, tableMetadata, primaryKeyHashMap, configuration);

        return dataService.fetchByPrimaryKey(tableMetadata, sql2o, primaryKeyHashMap, configuration).get(0);
    }

    public MObject create(ServiceConfiguration configuration, MObject mObject) throws Exception {
        return dataService.insert(mObject, connectionService.getSql2Object(configuration),
                metadataManager.getMetadataTable(configuration, mObject.getDeveloperName()), configuration);
    }

    public void delete(ServiceConfiguration configuration, String developerName, HashMap<String, String> id) throws Exception {
        dataService.deleteByPrimaryKey(
                metadataManager.getMetadataTable(configuration, developerName),
                connectionService.getSql2Object(configuration), id, configuration);
    }
}
