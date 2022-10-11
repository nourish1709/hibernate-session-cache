package com.nourish1709.hibernatesessioncash.session.action;

import com.nourish1709.hibernatesessioncash.session.utils.EntityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.LogRecord;

@Log
@RequiredArgsConstructor
public class InsertAction implements EntityAction {

    private static final String INSERT_QUERY = "INSERT INTO %s (%s) VALUES (%s)";
    private final DataSource dataSource;
    private final Object entity;

    @Override
    public void executeAction() {
        try (final Connection connection = dataSource.getConnection()) {

            final String tableName = EntityUtils.calculateTableName(entity.getClass());
            final String columnNames = EntityUtils.calculateEntityColumnNamesForStatement(entity.getClass());
            final String insertParameters = EntityUtils.calculateInsertParameters(entity);

            try (final PreparedStatement statement = connection
                    .prepareStatement(INSERT_QUERY.formatted(tableName, columnNames, insertParameters))) {
                log.info("SQL: " + statement);

                statement.executeUpdate();
            }
        } catch (SQLException exception) {
            log.log(new LogRecord(Level.SEVERE, exception.getMessage()));
            exception.printStackTrace();
        }
    }
}