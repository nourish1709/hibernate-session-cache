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
public class DeleteAction implements EntityAction {

    private static final String DELETE_QUERY = "DELETE FROM %s WHERE id = ?";
    private final DataSource dataSource;

    private final Object entity;

    @Override
    public void executeAction() {
        try (final Connection connection = dataSource.getConnection();
             final PreparedStatement statement = connection.prepareStatement(
                     DELETE_QUERY.formatted(EntityUtils.calculateTableName(entity.getClass())))) {
            log.info("SQL: " + statement);

            statement.setObject(1, EntityUtils.calculateIdValue(entity));
            statement.executeUpdate();
        } catch (SQLException exception) {
            log.log(new LogRecord(Level.SEVERE, exception.getMessage()));
            exception.printStackTrace();
        }
    }
}
