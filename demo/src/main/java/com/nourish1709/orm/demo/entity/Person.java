package com.nourish1709.orm.demo.entity;

import com.nourish1709.orm.poc.annotation.Column;
import com.nourish1709.orm.poc.annotation.Entity;
import com.nourish1709.orm.poc.annotation.Id;
import com.nourish1709.orm.poc.annotation.Table;

@Entity
@Table(name = "persons")
public class Person {

    @Id(column = "id")
    private int id;
    @Column(name = "first_name")
    private String firstName;
    @Column(name = "last_name")
    private String lastName;

    @Override
    public String toString() {
        return "Person{" +
               "id=" + id +
               ", firstName='" + firstName + '\'' +
               ", lastName='" + lastName + '\'' +
               '}';
    }
}
