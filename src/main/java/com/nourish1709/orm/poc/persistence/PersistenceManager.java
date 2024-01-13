package com.nourish1709.orm.poc.persistence;

public interface PersistenceManager {

    <T> T findById(Class<T> entityClass, Object id);
}
