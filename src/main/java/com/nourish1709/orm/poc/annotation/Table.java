package com.nourish1709.orm.poc.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Specifies information about the table associated with the {@link Entity}
 * <p/>
 * If the current annotation is not present, a table name will be resolved from the class name in lowercase.
 * For example, for the class Person without <code>@Table</code> the table name will be resolved as <code>person</code>.
 * For the class Apples without the <code>@Table</code> annotation the table name will be <code>apples</code>
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Table {

    /**
     * @return entity's table name
     */
    String name();
}
