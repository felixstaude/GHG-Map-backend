package test.felixstaude.ghgmap.database;

import de.felixstaude.ghgmap.database.DatabaseConfig;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;

public class DatabaseConfigTest {

    @Test
    public void testDataSource() {
        DatabaseConfig databaseConfig = new DatabaseConfig();
        DataSource dataSource = databaseConfig.dataSource();
        assertNotNull(dataSource);
    }

    @Test
    public void testJdbcTemplate() {
        DataSource mockDataSource = mock(DriverManagerDataSource.class);
        DatabaseConfig databaseConfig = new DatabaseConfig();
        JdbcTemplate jdbcTemplate = databaseConfig.jdbcTemplate(mockDataSource);
        assertNotNull(jdbcTemplate);
    }
}
