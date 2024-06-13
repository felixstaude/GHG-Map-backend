package de.felixstaude.ghgmap.database;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import javax.sql.DataSource;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
public class DatabaseIntegrationTest {

    @Autowired
    private DataSource dataSource;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    public void setUp() {
        assertNotNull(dataSource);
        assertNotNull(jdbcTemplate);
    }

    @AfterEach
    public void tearDown() {
        // Clean up the database after each test
        jdbcTemplate.update("DELETE FROM userlist WHERE twitchId = 'test_twitch_id'");
        jdbcTemplate.update("DELETE FROM userlist WHERE twitchId = 'test_twitch_id_to_delete'");
    }

    @Test
    public void testDatabaseConnection() {
        assertNotNull(dataSource);
        assertNotNull(jdbcTemplate);
    }

    @Test
    public void testInsertAndQuery() {
        // Insert a test entry
        String insertSql = "INSERT INTO userlist (twitchId) VALUES (?)";
        jdbcTemplate.update(insertSql, "test_twitch_id");

        // Query the test entry
        String querySql = "SELECT COUNT(*) FROM userlist WHERE twitchId = ?";
        Integer count = jdbcTemplate.queryForObject(querySql, new Object[]{"test_twitch_id"}, Integer.class);
        assertNotNull(count);
        assertTrue(count > 0);
    }

    @Test
    public void testDeleteEntry() {
        // Insert a test entry
        String insertSql = "INSERT INTO userlist (twitchId) VALUES (?)";
        jdbcTemplate.update(insertSql, "test_twitch_id_to_delete");

        // Delete the test entry
        String deleteSql = "DELETE FROM userlist WHERE twitchId = ?";
        jdbcTemplate.update(deleteSql, "test_twitch_id_to_delete");

        // Verify the entry is deleted
        String querySql = "SELECT COUNT(*) FROM userlist WHERE twitchId = ?";
        Integer count = jdbcTemplate.queryForObject(querySql, new Object[]{"test_twitch_id_to_delete"}, Integer.class);
        assertNotNull(count);
        assertTrue(count == 0);
    }
}
