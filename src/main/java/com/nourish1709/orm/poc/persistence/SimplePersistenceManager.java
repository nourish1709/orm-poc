package com.nourish1709.orm.poc.persistence;

import com.nourish1709.orm.poc.annotation.Column;
import com.nourish1709.orm.poc.annotation.Id;
import com.nourish1709.orm.poc.annotation.Table;
import com.nourish1709.orm.poc.exception.DatabaseOperationException;
import com.nourish1709.orm.poc.exception.EntityInstantiationException;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Optional;
import java.util.Properties;

import static java.util.Objects.nonNull;
import static java.util.Objects.requireNonNull;

public class SimplePersistenceManager implements PersistenceManager {

    private final String jdbcUrl;
    private final Properties connectionProperties;

    public SimplePersistenceManager(String jdbcUrl, String user) {
        this(jdbcUrl, user, null);
    }

    public SimplePersistenceManager(String jdbcUrl, String user, String password) {
        this.jdbcUrl = jdbcUrl;

        var properties = new Properties();
        if (nonNull(user)) properties.setProperty("user", user);
        if (nonNull(password)) properties.setProperty("password", password);
        this.connectionProperties = properties;
    }

    @Override
    public <T> T findById(Class<T> entityClass, Object id) {
        var table = getTableName(entityClass);
        var idColumn = getIdColumn(entityClass);

        return selectEntity(entityClass, table, idColumn, requireNonNull(id));
    }

    private <T> String getTableName(Class<T> entityClass) {
        return Optional.ofNullable(entityClass.getAnnotation(Table.class))
                .map(Table::name)
                .orElseGet(() -> entityClass.getSimpleName().toLowerCase());
    }

    private <T> String getIdColumn(Class<T> entityClass) {
        return getIdField(entityClass).getDeclaredAnnotation(Id.class).column();
    }

    private Field getIdField(Class<?> entityClass) {
        return Arrays.stream(entityClass.getDeclaredFields())
                .filter(f -> f.isAnnotationPresent(Id.class))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Entity should have only one @Id field"));
    }

    private <T> T selectEntity(Class<T> entityClass, String tableName, String idColumn, Object id) {
        var query = "SELECT * FROM %s WHERE %s = ?".formatted(tableName, idColumn);

        try (var connection = DriverManager.getConnection(jdbcUrl, connectionProperties);
             var statement = connection.prepareStatement(query)) {
            statement.setObject(1, id);

            var resultSet = statement.executeQuery();
            return constructEntity(entityClass, resultSet);
        } catch (SQLException e) {
            var message = "Exception occurred while selecting from table %s by column %s with value %s".formatted(tableName, idColumn, id.toString());
            throw new DatabaseOperationException(message, e);
        }
    }

    private <T> T constructEntity(Class<T> entityClass, ResultSet resultSet) throws SQLException {
        var entity = getNewInstance(entityClass);

        try {
            if (resultSet.next()) {
                for (Field field : entityClass.getDeclaredFields()) {
                    field.setAccessible(true);

                    String column;
                    if (field.isAnnotationPresent(Id.class))
                        column = field.getAnnotation(Id.class).column();
                    else
                        column = Optional.ofNullable(field.getAnnotation(Column.class))
                                .map(Column::name)
                                .orElseGet(() -> field.getName().toLowerCase());

                    var value = resultSet.getObject(column);
                    field.set(entity, value);
                }

                return entity;
            }
        } catch (IllegalAccessException e) {
            throw new EntityInstantiationException(
                    String.format("Failed to construct entity %s from result set", entityClass.getSimpleName()),
                    e
            );
        }

        return null;
    }

    private <T> T getNewInstance(Class<T> entityClass) {
        try {
            return entityClass.getConstructor().newInstance();
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                 NoSuchMethodException e) {
            throw new EntityInstantiationException(
                    String.format("Failed to instantiate entity %s. " +
                                  "Make sure the entity has a public no-args constructor", entityClass.getSimpleName()),
                    e
            );
        }
    }
}
