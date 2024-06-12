package de.felixstaude.ghgmap.database;

public class PinData {
    private int pinId;
    private String twitchId;
    private String description;
    private String lat;
    private String lng;

    public int getPinId() {
        return pinId;
    }

    public void setPinId(int pinId) {
        this.pinId = pinId;
    }

    public String getTwitchId() {
        return twitchId;
    }

    public void setTwitchId(String twitchId) {
        this.twitchId = twitchId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLat() {
        return lat;
    }

    public void setLat(String lat) {
        this.lat = lat;
    }

    public String getLng() {
        return lng;
    }

    public void setLng(String lng) {
        this.lng = lng;
    }
}
