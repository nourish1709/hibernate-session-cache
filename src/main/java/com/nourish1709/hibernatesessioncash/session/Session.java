package com.nourish1709.hibernatesessioncash.session;

import com.nourish1709.hibernatesessioncash.session.action.ActionQueue;
import com.nourish1709.hibernatesessioncash.session.action.DeleteAction;
import com.nourish1709.hibernatesessioncash.session.action.InsertAction;
import com.nourish1709.hibernatesessioncash.session.entitykey.EntityKey;
import com.nourish1709.hibernatesessioncash.session.entitykey.UpdateEntityKey;
import com.nourish1709.hibernatesessioncash.session.utils.EntityUtils;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.java.Log;

import javax.sql.DataSource;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Log
@RequiredArgsConstructor
public class Session implements AutoCloseable {

    private static final String SELECT_BY_ID_QUERY = "SELECT * FROM %s where id = ?";
    private static final String UPDATE_BY_ID_QUERY = "UPDATE %s SET %s = ? WHERE id = ?";
    private final DataSource dataSource;
    private final Map<EntityKey<?>, Object[]> entityInitialStateMap = new ConcurrentHashMap<>();
    private final Map<EntityKey<?>, Object> entityMap = new ConcurrentHashMap<>();

    private ActionQueue actionQueue = new ActionQueue();

    public <T> T find(Class<T> entityClass, Object id) {
        var key = new EntityKey<>(entityClass, id);
        var entity = entityMap.computeIfAbsent(key, entityKey -> findEntity(entityKey, id));

        return entityClass.cast(entity);
    }

    @Override
    public void close() {
        entityMap.entrySet().stream()
                .map(this::extractChangedFields)
                .filter(updateEntity -> updateEntity.updatedFields().length > 0)
                .forEach(this::performUpdate);

        flush();
    }

    /**
     * stores a new entity
     *
     * @param entity
     */
    public void persist(Object entity) {
        // create InsertAction
        final InsertAction insertAction = new InsertAction(dataSource, entity);

        // add it to the ActionQueue
        actionQueue.addAction(insertAction);
    }

    /**
     * deletes a managed entity
     *
     * @param entity
     */
    public void remove(Object entity) {
        // create DeleteAction
        final DeleteAction deleteAction = new DeleteAction(dataSource, entity);

        // add it to the ActionQueue
        actionQueue.addAction(deleteAction);
    }

    /**
     * it should go through all actions and performs corresponding SQL statements
     */
    private void flush() {
        actionQueue.executeAll();
    }

    @SneakyThrows
    private <T> T findEntity(EntityKey<T> entityKey, Object id) {
        String tableName = EntityUtils.calculateTableName(entityKey.entityClass());

        try (final Connection connection = dataSource.getConnection();
             final PreparedStatement statement = connection.prepareStatement(SELECT_BY_ID_QUERY.formatted(tableName));) {
            statement.setObject(1, id);

            log.info("SQL: " + statement);
            final ResultSet resultSet = statement.executeQuery();
            resultSet.next();

            final T newInstance = constructNewInstance(entityKey, resultSet);

            entityInitialStateMap.computeIfAbsent(entityKey, key -> computeInitialState(key, newInstance));

            return newInstance;
        }
    }

    @SneakyThrows
    private <T> Object[] computeInitialState(EntityKey<?> entityKey, T entity) {
        final Field[] fields = Arrays.stream(entityKey.entityClass().getDeclaredFields())
                .sorted(Comparator.comparing(Field::getName))
                .toArray(Field[]::new);
        final Object[] snapshot = new Object[fields.length];

        for (int i = 0; i < fields.length; i++) {
            fields[i].setAccessible(true);
            final Object fieldValue = fields[i].get(entity);
            snapshot[i] = fieldValue;
        }
        return snapshot;
    }

    @SneakyThrows
    private <T> T constructNewInstance(EntityKey<T> entityKey, ResultSet resultSet) {
        final T newInstance = entityKey.entityClass().getConstructor().newInstance();
        final Field[] declaredFields = newInstance.getClass().getDeclaredFields();
        for (int i = 0; i < declaredFields.length; i++) {
            declaredFields[i].setAccessible(true);
            declaredFields[i].set(newInstance, resultSet.getObject(i + 1));
        }

        return newInstance;
    }

    @SneakyThrows
    private UpdateEntityKey<?> extractChangedFields(Map.Entry<EntityKey<?>, Object> entityEntry) {
        final Object[] initialSnapshot = entityInitialStateMap.get(entityEntry.getKey());
        List<Field> changedFields = new ArrayList<>();
        List<Object> updatedValues = new ArrayList<>();
        if (initialSnapshot != null) {
            final Object entity = entityEntry.getValue();
            final Field[] fields = Arrays.stream(entity.getClass().getDeclaredFields())
                    .sorted(Comparator.comparing(Field::getName))
                    .toArray(Field[]::new);

            for (int i = 0; i < fields.length; i++) {
                fields[i].setAccessible(true);
                if (!fields[i].get(entity).equals(initialSnapshot[i])) {
                    changedFields.add(fields[i]);
                    updatedValues.add(fields[i].get(entity));
                }
            }
        }
        return new UpdateEntityKey<>(entityEntry.getKey().entityClass(),
                entityEntry.getKey().id(), changedFields.toArray(Field[]::new), updatedValues.toArray(Object[]::new));
    }


    @SneakyThrows
    private void performUpdate(UpdateEntityKey<?> updateEntity) {
        try (final Connection connection = dataSource.getConnection()) {
            final String tableName = EntityUtils.calculateTableName(updateEntity.entityClass());
            final Field[] changedFields = updateEntity.updatedFields();
            for (int i = 0; i < changedFields.length; i++) {
                try (final PreparedStatement statement = connection
                        .prepareStatement(UPDATE_BY_ID_QUERY.formatted(tableName, EntityUtils.calculateColumnName(changedFields[i])))) {
                    log.info("SQL: " + statement);

                    statement.setObject(1, updateEntity.updatedValues()[i]);
                    statement.setObject(2, updateEntity.id());

                    statement.executeUpdate();
                }
            }
        }
    }
}
