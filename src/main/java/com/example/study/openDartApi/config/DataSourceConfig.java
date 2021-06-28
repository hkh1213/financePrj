package com.example.study.openDartApi.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
public class DataSourceConfig {

    @Bean
    @ConfigurationProperties(prefix = "spring.datasource")
    public DataSource dataSource() {
        String username="admin";
        String password="12131213";
        String jdbcUrl="jdbc:mysql://database-3.crvzrkxtwlmg.ap-northeast-2.rds.amazonaws.com:3306/spring";
        String driverClass="com.mysql.jdbc.Driver";

        DataSourceBuilder dataSourceBuilder=DataSourceBuilder.create();
        dataSourceBuilder.username(username);
        dataSourceBuilder.password(password);
        dataSourceBuilder.url(jdbcUrl);
        dataSourceBuilder.driverClassName(driverClass);

        return dataSourceBuilder.build();
    }
}
