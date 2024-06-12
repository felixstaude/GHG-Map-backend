package de.felixstaude.ghgmap.database;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class DatabaseGenerator {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public void generateTable(){
        String sql = "CREATE TABLE IF NOT EXISTS userlist (" +
                "userId MEDIUMINT NOT NULL AUTO_INCREMENT," +
                "twitchId VARCHAR(256) UNIQUE," +
                "PRIMARY KEY (userId)" +
                ")";

        jdbcTemplate.execute(sql);

        sql = "CREATE TABLE IF NOT EXISTS pins (" +
                "pinId MEDIUMINT NOT NULL AUTO_INCREMENT," +
                "twitchId VARCHAR(256)," +
                "description VARCHAR(4096)," +
                "lat VARCHAR(256)," +
                "lng VARCHAR(256)," +
                "PRIMARY KEY (pinId)" +
                ")";

        jdbcTemplate.execute(sql);
    }
}
