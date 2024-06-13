package de.felixstaude.ghgmap.api.connection.pin;

import de.felixstaude.ghgmap.database.PinData;
import de.felixstaude.ghgmap.file.ImageProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.web.bind.annotation.*;

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
    public Map<String, Object> addPin(@RequestBody PinRequest pinRequest) {
        String sql = "INSERT INTO pins (twitchId, description, lat, lng) VALUES (?, ?, ?, ?)";
        jdbcTemplate.update(sql, pinRequest.getUserId(), pinRequest.getDescription(), pinRequest.getLat(), pinRequest.getLng());

        sql = "SELECT LAST_INSERT_ID()";
        int pinId = jdbcTemplate.queryForObject(sql, Integer.class);

        try {
            imageProcessor.saveImage(pinRequest.getImage(), String.valueOf(pinId));
        } catch (Exception e) {
            throw new RuntimeException("Failed to process image", e);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("pinId", pinId);
        return response;
    }

    @GetMapping("/all")
    public List<PinData> getAllPins() {
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
}
