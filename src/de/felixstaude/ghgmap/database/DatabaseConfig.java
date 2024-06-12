package de.felixstaude.ghgmap.database;

import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;
import java.io.FileInputStream;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.util.Properties;
import java.util.TimeZone;

public class DatabaseConfig {

    @Bean
    public DataSource dataSource(){
        Properties properties = new Properties();
        String propertiesFileName = "config.properties";

        try{
            String jarPath = Paths.get(DatabaseConfig.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getParent().toString();
            properties.load(new FileInputStream(jarPath + "/" + propertiesFileName));

        } catch (Exception e) {
            throw new RuntimeException("Unable to load database credentials", e);
        }

        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName("com.mysql.cj.jdbc.Driver");
        dataSource.setUrl("jdbc:mysql://localhost:3306/ghgMap?serverTimezone=UTC");
        dataSource.setUsername(properties.getProperty("db.username"));
        dataSource.setPassword(properties.getProperty("db.password"));

        return dataSource;
    }

    @Bean
    public JdbcTemplate jdbcTemplate(DataSource dataSource){
        return new JdbcTemplate(dataSource);
    }

}
