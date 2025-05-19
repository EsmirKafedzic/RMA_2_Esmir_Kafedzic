package com.esmirkafedzic.places;

import java.util.ArrayList;
import java.util.List;

public class Place {

    private String name;
    private double averageRating;
    private List<String> ratedBy;    // Lista korisničkih ID-eva koji su ocijenili mjesto
    private String userId;           // ID korisnika koji je kreirao mjesto
    private String imageUrl;         // Putanja do slike (lokalna putanja u tvom slučaju)
    private String imageName;        // Ime slike (npr. UUID.jpg)
    private String description;      // Kratki opis mjesta

    public Place() {
        this.ratedBy = new ArrayList<>();
    }

    // Puni konstruktor s opisom
    public Place(String name, double averageRating, List<String> ratedBy, String userId, String imageUrl, String imageName, String description) {
        this.name = name;
        this.averageRating = averageRating;
        this.ratedBy = ratedBy != null ? ratedBy : new ArrayList<>();
        this.userId = userId;
        this.imageUrl = imageUrl;
        this.imageName = imageName;
        this.description = description;
    }

    // Getteri i setteri

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getAverageRating() {
        return averageRating;
    }

    public void setAverageRating(double averageRating) {
        this.averageRating = averageRating;
    }

    public List<String> getRatedBy() {
        return ratedBy;
    }

    public void setRatedBy(List<String> ratedBy) {
        this.ratedBy = ratedBy;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    /*
        imageUrl je sad lokalna putanja do slike, npr:
        /data/data/com.esmirkafedzic.places/files/images/uuid.jpg
    */
    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getImageName() {
        return imageName;
    }

    public void setImageName(String imageName) {
        this.imageName = imageName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
