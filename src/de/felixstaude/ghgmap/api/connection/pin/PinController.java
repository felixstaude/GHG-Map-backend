package de.felixstaude.ghgmap.api.connection.pin;

import de.felixstaude.ghgmap.database.PinData;
import de.felixstaude.ghgmap.file.ImageProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/pin")
public class PinController {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private ImageProcessor imageProcessor;

    @PostMapping("/add")
    public Map<String, Object> addPin(@RequestBody Map<String, Object> payload){
        String description = (String) payload.get("description");
        String image = (String) payload.get("image");
        String userId = (String) payload.get("user_id");
        String lat = payload.get("lat").toString();
        String lng = payload.get("lng").toString();

        String sql = "INSERT INTO pins (twitchId, description, lat, lng) VALUES (?, ?, ?, ?)";
        jdbcTemplate.update(sql, userId, description, lat, lng);

        sql = "SELECT LAST_INSERT_ID()";
        int pinId = jdbcTemplate.queryForObject(sql, Integer.class);

        try{
            String base64Image = encodeImageToBase64("resources/images/" + image);
            imageProcessor.saveImage(base64Image, String.valueOf(pinId));
        } catch (Exception e){
            throw new RuntimeException("Failed to process image", e);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("pinId", pinId);
        return response;
    }

    @GetMapping("/all")
    public List<PinData> getAllPins(){
        String sql = "SELECT * FROM pins";
        return jdbcTemplate.query(sql, new RowMapper<PinData>() {
            @Override
            public PinData mapRow(java.sql.ResultSet rs, int rowNum) throws java.sql.SQLException {
                PinData pin = new PinData();
                pin.setPinId(rs.getInt("pinId"));
                pin.setTwitchId(rs.getString("twitchId"));
                pin.setDescription(rs.getString("description"));
                pin.setLat(rs.getString("lat"));
                pin.setLng(rs.getString("lng"));
                return pin;
            }
        });
    }

    private String encodeImageToBase64(String imagePath) throws IOException {
        File imageFile = new File(imagePath);
        try (FileInputStream imageInFile = new FileInputStream(imageFile)) {
            byte[] imageData = new byte[(int) imageFile.length()];
            imageInFile.read(imageData);
            return Base64.getEncoder().encodeToString(imageData);
        }
    }
}
