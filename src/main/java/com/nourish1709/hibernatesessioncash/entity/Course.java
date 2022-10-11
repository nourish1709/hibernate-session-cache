package com.nourish1709.hibernatesessioncash.entity;

import com.nourish1709.hibernatesessioncash.annotation.Column;
import com.nourish1709.hibernatesessioncash.annotation.Id;
import com.nourish1709.hibernatesessioncash.annotation.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Table("courses")
@ToString
@Getter
@Setter
public class Course {

    @Id
    @Column("id")
    private Integer id;

    @Column("name")
    private String name;
}
