package com.app.database;

import com.app.patterns.LazyInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;

@Service
public class JdbcTemplateFactory {
    private static final Logger logger = LoggerFactory.getLogger(JdbcTemplateFactory.class);
    private static final LazyInitializer<JdbcTemplate, String> jdbcTemplateLazyInitializer =
            new LazyInitializer<>(new LazyInitializer.Creator<JdbcTemplate, String>() {
                @Override
                public JdbcTemplate create(String param) {
                    return new JdbcTemplate(getDataSource(param));
                }
            });

    public JdbcTemplate createTemplate(String database) {

        try {
            return jdbcTemplateLazyInitializer.getOrCreate(database);
        } catch (Exception e) {
            logger.error("jdbcTemplate failed to initialise wit exception ", e);
            throw new RuntimeException("Couldn't initialise jdbc template");
        }
    }

    private static DataSource getDataSource(String database) {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName("com.mysql.jdbc.Driver");
        dataSource.setUrl("jdbc:mysql://localhost:3306/" + database);
        dataSource.setUsername("springuser");
        dataSource.setPassword("ThePassword");

        return dataSource;
    }
}
