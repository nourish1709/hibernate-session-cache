package com.nourish1709.hibernatesessioncash;

import com.nourish1709.hibernatesessioncash.entity.Course;
import com.nourish1709.hibernatesessioncash.session.SessionFactory;
import org.postgresql.ds.PGSimpleDataSource;

import javax.sql.DataSource;

public class StartApp {

    public static void main(String[] args) {

        DataSource dataSource = constructNewDataSource();

        final var sessionFactory = new SessionFactory(dataSource);
        try (final var session = sessionFactory.createSession()) {
//            final Course course = new Course();
//            course.setId(3);
//            course.setName("Intro to JPA");
//            session.persist(course);

            final Course course = session.find(Course.class, 3);
            System.out.println(course);
//            session.remove(course);
        }
    }

    private static DataSource constructNewDataSource() {
        final PGSimpleDataSource dataSource = new PGSimpleDataSource();

        dataSource.setUrl("jdbc:postgresql://localhost:5432/deadlock");
        dataSource.setUser("postgres");
        dataSource.setPassword("postgres");

        return dataSource;
    }
}
