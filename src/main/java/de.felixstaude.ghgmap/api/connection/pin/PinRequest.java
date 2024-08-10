package de.felixstaude.ghgmap.api.connection.pin;

public class PinRequest {
    private String lat;
    private String lng;
    private String description;
    private String image; // Base64-kodiertes Bild als String
    private String userId;
    private String town; // Neu hinzugefügt
    private boolean approved;

    // Getter und Setter

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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getTown() {
        return town;
    }

    public void setTown(String town) {
        this.town = town;
    }

    public Boolean getApproved(){
        return approved;
    }

    public void setApproved(Boolean approved){
        this.approved = approved;
    }
}
