package com.nourish1709.hibernatesessioncash.session;

import lombok.RequiredArgsConstructor;

import javax.sql.DataSource;

@RequiredArgsConstructor
public class SessionFactory {

    private final DataSource dataSource;

    public Session createSession() {
        return new Session(dataSource);
    }
}
