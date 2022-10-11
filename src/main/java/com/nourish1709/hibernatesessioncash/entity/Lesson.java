package com.nourish1709.hibernatesessioncash.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

//@Entity
//@Table(name = "lessons")
@ToString
@Setter
@Getter
public class Lesson {

    //    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    @Column(name = "id")
    private Long id;

    //    @Column(name = "name")
    private String name;

    //    @Column(unique = true)
    private String uniqueId;

    //    @ManyToOne
//    @JoinColumn(name = "course_id")
    private Course course;
}
