package com.manywho.services.sql.services;

import com.google.common.base.Strings;
import com.manywho.services.sql.ServiceConfiguration;
import org.sql2o.Sql2o;
import org.sql2o.quirks.NoQuirks;
import org.sql2o.quirks.PostgresQuirks;
import org.sql2o.quirks.Quirks;
import java.net.URLEncoder;
import java.util.Objects;

public class ConnectionService {

    private static final String DATABASE_TYPE_POSTGRESQL = "postgresql";
    private static final String CONNECTION_STRING_FORMAT_POSTGRESQL = "jdbc:postgresql://%s:%s/%s";
    private static final String DRIVER_CLASS_POSTGRESQL = "org.postgresql.Driver";

    private static final String DATABASE_TYPE_MYSQL = "mysql";
    private static final String CONNECTION_STRING_FORMAT_MYSQL = "jdbc:mysql://%s:%s/%s";
    private static final String DRIVER_CLASS_MYSQL = "com.mysql.jdbc.Driver";

    private static final String DATABASE_TYPE_SQLSERVER = "sqlserver";
    private static final String CONNECTION_STRING_FORMAT_SQLSERVER = "jdbc:sqlserver://%s:%s;databaseName=%s";
    private static final String DRIVER_CLASS_SQLSERVER = "com.microsoft.sqlserver.jdbc.SQLServerDriver";

    private Sql2o sql2o;

    public ConnectionService() {
        sql2o = null;
    }

    public Sql2o getSql2Object(ServiceConfiguration serviceConfiguration) throws Exception {

        if(sql2o!= null) {
            return sql2o;
        }

        Quirks quirks = new NoQuirks();

        checkDatabaseTypeSupported(serviceConfiguration);
        String connectionStringFormat = CONNECTION_STRING_FORMAT_POSTGRESQL;

        if(Objects.equals(serviceConfiguration.getDatabaseType(), DATABASE_TYPE_MYSQL)) {
            connectionStringFormat = CONNECTION_STRING_FORMAT_MYSQL;
        } else if(Objects.equals(serviceConfiguration.getDatabaseType(), DATABASE_TYPE_SQLSERVER)) {
            connectionStringFormat = CONNECTION_STRING_FORMAT_SQLSERVER;
        } else {
            quirks = new PostgresQuirks();
        }

        String connectionString =  String.format(connectionStringFormat, serviceConfiguration.getHost(), serviceConfiguration.getPort(), serviceConfiguration.getDatabaseName());

        if (!serviceConfiguration.getNoUseSsl()) {
            connectionString = connectionString + addSecurity(serviceConfiguration.getDatabaseType(), serviceConfiguration.getServerPublicCertificate());
        }

        return new Sql2o(
                connectionString,
                serviceConfiguration.getUsername(),
                serviceConfiguration.getPassword(),
                quirks
        );
    }

    private String addSecurity(String databaseType, String serverCertificate) throws Exception {
        if (Strings.isNullOrEmpty(serverCertificate)) {
            throw new RuntimeException("The certificate is mandatory if you use SSL");
        }

        switch (databaseType) {
            case DATABASE_TYPE_POSTGRESQL:
                return "?ssl=true&sslmode=verify-full&sslfactory=org.postgresql.ssl.SingleCertValidatingFactory&sslfactoryarg=" + URLEncoder.encode(serverCertificate, "UTF-8");
            case DATABASE_TYPE_MYSQL:
                return ";SSL Mode=Required";
            case DATABASE_TYPE_SQLSERVER:
                return ";integratedSecurity=true;encrypt=true;trustServerCertificate=true";
            default:
                throw new Exception("database type " + databaseType + "not supported");
        }
    }

    private void checkDatabaseTypeSupported(ServiceConfiguration serviceConfiguration) throws Exception {
        switch(serviceConfiguration.getDatabaseType()) {
            case DATABASE_TYPE_POSTGRESQL:
                checkDriverClass(DRIVER_CLASS_POSTGRESQL, serviceConfiguration.getDatabaseType());
                break;
            case DATABASE_TYPE_MYSQL:
                checkDriverClass(DRIVER_CLASS_MYSQL, serviceConfiguration.getDatabaseType());
                break;
            case DATABASE_TYPE_SQLSERVER:
                checkDriverClass(DRIVER_CLASS_SQLSERVER, serviceConfiguration.getDatabaseType());
                break;
            default:
                throw new Exception("database type " + serviceConfiguration.getDatabaseType() + "not supported");
        }
    }

    private void checkDriverClass(String driverClass, String databaseType) throws Exception {
        try {
            Class.forName(driverClass);
        } catch (ClassNotFoundException e) {
            throw new Exception("driver for database type " + databaseType + "not found");
        }
    }
}
