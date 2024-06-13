package de.felixstaude.ghgmap.database;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

@Configuration
public class DatabaseGenerator {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public void generateTable(){
        String sql = "CREATE TABLE IF NOT EXISTS userlist (" +
                "userId MEDIUMINT NOT NULL AUTO_INCREMENT," +
                "twitchId VARCHAR(256) UNIQUE" +
                ")";

        jdbcTemplate.execute(sql);

        sql = "CREATE TABLE IF NOT EXISTS pins (" +
                "pinId MEDIUMINT NOT NULL AUTO_INCREMENT," +
                "twitchId VARCHAR(256)," +
                "description VARCHAR(4096)," +
                "lat VARCHAR(256)," +
                "lng VARCHAR(256))";

        jdbcTemplate.execute(sql);
    }
}
