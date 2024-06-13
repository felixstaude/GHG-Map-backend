package test.felixstaude.ghgmap.api.connection.pin;

import de.felixstaude.ghgmap.api.connection.pin.PinController;
import de.felixstaude.ghgmap.api.connection.pin.PinRequest;
import de.felixstaude.ghgmap.file.ImageProcessor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.jdbc.core.JdbcTemplate;

import java.io.IOException;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

public class PinControllerTest {

    @InjectMocks
    private PinController pinController;

    @Mock
    private JdbcTemplate jdbcTemplate;

    @Mock
    private ImageProcessor imageProcessor;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testAddPin() throws IOException {
        // Arrange
        String base64Image = "iVBORw0KGgoAAAANSUhEUgAAAAUA" + // Beispiel Base64-kodiertes Bild
                "AAAABCAIAAACQd1PeAAAAC0lEQVR42mP8/w" +
                "==";

        PinRequest pinRequest = new PinRequest();
        pinRequest.setDescription("Test description");
        pinRequest.setUserId("5555");
        pinRequest.setLat("52.5200");
        pinRequest.setLng("13.4050");
        pinRequest.setImage(base64Image);

        when(jdbcTemplate.update(anyString(), eq(pinRequest.getUserId()), anyString(), eq(pinRequest.getLat()), eq(pinRequest.getLng()))).thenReturn(1);
        when(jdbcTemplate.queryForObject(anyString(), eq(Integer.class))).thenReturn(1);
        doNothing().when(imageProcessor).saveImage(anyString(), anyString());

        // Act
        Map<String, Object> response = pinController.addPin(pinRequest);

        // Assert
        assertEquals("success", response.get("status"));
        assertEquals(1, response.get("pinId"));

        verify(jdbcTemplate, times(1)).update(anyString(), eq(pinRequest.getUserId()), anyString(), eq(pinRequest.getLat()), eq(pinRequest.getLng()));
        verify(jdbcTemplate, times(1)).queryForObject(anyString(), eq(Integer.class));
        verify(imageProcessor, times(1)).saveImage(anyString(), anyString());
    }
}
