package com.nourish1709.hibernatesessioncash.session.entitykey;

public record EntityKey<T>(Class<T> entityClass, Object id) {
}
