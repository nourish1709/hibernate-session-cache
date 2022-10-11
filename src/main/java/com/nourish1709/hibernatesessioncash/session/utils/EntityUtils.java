package com.nourish1709.hibernatesessioncash.session.utils;

import com.nourish1709.hibernatesessioncash.annotation.Column;
import com.nourish1709.hibernatesessioncash.annotation.Id;
import com.nourish1709.hibernatesessioncash.annotation.Table;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@UtilityClass
public class EntityUtils {

    private static final Logger log = Logger.getLogger(EntityUtils.class.getName());

    public String calculateTableName(Class<?> entityClass) {
        if (entityClass.isAnnotationPresent(Table.class)) {
            return entityClass.getDeclaredAnnotation(Table.class).value();
        } else {
            return entityClass.getSimpleName();
        }
    }

    public String calculateEntityColumnNamesForStatement(Class<?> entityClass) {
        return Arrays.stream(entityClass.getDeclaredFields())
                .map(EntityUtils::calculateColumnName)
                .collect(Collectors.joining(", "));
    }

    public String calculateColumnName(Field changedField) {
        if (changedField.isAnnotationPresent(Column.class)) {
            return changedField.getAnnotation(Column.class).value();
        }
        return changedField.getName();
    }

    public String calculateInsertParameters(final Object entity) {
        return Arrays.stream(entity.getClass().getDeclaredFields())
                .map(field -> getValueAsString(entity, field))
                .collect(Collectors.joining(", "));
    }

    public Object calculateIdValue(Object entity) {
        final Class<?> entityClass = entity.getClass();
        return Arrays.stream(entityClass.getDeclaredFields())
                .filter(field -> field.isAnnotationPresent(Id.class))
                .findAny()
                .map(idField -> getValue(entity, idField))
                .orElseThrow(() -> {
                    final String errorMessage = "No @id annotation is present for entity %s. Use %s for %s"
                            .formatted(entityClass.getSimpleName(), Id.class.getName(), entityClass.getName());
                    log.log(new LogRecord(Level.SEVERE, errorMessage));
                    return new IllegalArgumentException(errorMessage);
                });
    }

    @SneakyThrows
    private Object getValue(Object entity, Field idField) {
        idField.setAccessible(true);
        return idField.get(entity);
    }

    @SneakyThrows
    private String getValueAsString(Object entity, Field field) {
        field.setAccessible(true);
        return "'" + field.get(entity).toString() + "'";
    }
}
