package com.nourish1709.hibernatesessioncash;

import com.nourish1709.hibernatesessioncash.entity.Course;
import com.nourish1709.hibernatesessioncash.session.SessionFactory;
import lombok.Cleanup;
import org.postgresql.ds.PGSimpleDataSource;

import javax.sql.DataSource;

public class StartApp {

    public static void main(String[] args) {

        DataSource dataSource = constructNewDataSource();

        final var sessionFactory = new SessionFactory(dataSource);
        @Cleanup final var session = sessionFactory.createSession();

        final var course1 = session.find(Course.class, 1L);

        final var course2 = session.find(Course.class, 1L);

        System.out.println(course1 == course2);

        course1.setName("newName");

        final var course3 = session.find(Course.class, 2L);

        System.out.println(course1 == course3);
    }

    private static DataSource constructNewDataSource() {
        final PGSimpleDataSource dataSource = new PGSimpleDataSource();

        dataSource.setUrl("jdbc:postgresql://localhost:5432/deadlock");
        dataSource.setUser("postgres");
        dataSource.setPassword("postgres");

        return dataSource;
    }
}
