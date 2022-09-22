package com.nourish1709.hibernatesessioncash.entity;

import com.nourish1709.hibernatesessioncash.annotation.Column;
import com.nourish1709.hibernatesessioncash.annotation.Table;
import lombok.Getter;
import lombok.Setter;

@Table("courses")
@Getter
@Setter
public class Course {

    @Column("id")
    private Integer id;

    @Column("name")
    private String name;
}
