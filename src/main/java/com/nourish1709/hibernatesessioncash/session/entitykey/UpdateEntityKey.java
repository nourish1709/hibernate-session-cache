package com.nourish1709.hibernatesessioncash.session.entitykey;

import java.lang.reflect.Field;

public record UpdateEntityKey<T>(Class<T> entityClass, Object id, Field[] updatedFields, Object[] updatedValues) {
}
