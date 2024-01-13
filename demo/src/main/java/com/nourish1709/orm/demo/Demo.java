package com.nourish1709.orm.demo;

import com.nourish1709.orm.demo.entity.Person;
import com.nourish1709.orm.poc.persistence.PersistenceManager;
import com.nourish1709.orm.poc.persistence.SimplePersistenceManager;

public class Demo {

    public static void main(String[] args) {
        PersistenceManager manager = new SimplePersistenceManager("jdbc:postgresql://localhost:5432/orm_poc", "postgres");

        var person = manager.findById(Person.class, 1);
        System.out.println(person);
    }
}
