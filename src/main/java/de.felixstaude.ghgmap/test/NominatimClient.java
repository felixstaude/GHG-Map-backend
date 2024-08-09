package de.felixstaude.ghgmap.test;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class NominatimClient {

    private static final String NOMINATIM_URL = "https://nominatim.openstreetmap.org/reverse?format=jsonv2&lat=%s&lon=%s";

    public static String getTownFromCoordinates(double lat, double lon) {
        try {
            String urlString = String.format(NOMINATIM_URL, lat, lon);
            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("User-Agent", "Mozilla/5.0");

            if (conn.getResponseCode() != 200) {
                throw new IOException("Failed to get response from Nominatim API");
            }

            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(new InputStreamReader(conn.getInputStream()));
            JsonNode addressNode = root.path("address");

            if (addressNode.has("town")) {
                return addressNode.get("town").asText();
            } else if (addressNode.has("city")) {
                return addressNode.get("city").asText();
            } else if (addressNode.has("village")) {
                return addressNode.get("village").asText();
            } else if (addressNode.has("county")) {
                return addressNode.get("county").asText();
            } else {
                return "Unknown";
            }

        } catch (IOException e) {
            throw new RuntimeException("Failed to fetch town from Nominatim", e);
        }
    }
}
