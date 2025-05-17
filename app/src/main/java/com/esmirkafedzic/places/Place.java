package com.esmirkafedzic.places;

public class Place {
    private String name;
    private String imageUrl;
    private double rating;

    public Place() {
        // potreban prazan konstruktor za Firestore
    }

    public Place(String name, String imageUrl, double rating) {
        this.name = name;
        this.imageUrl = imageUrl;
        this.rating = rating;
    }

    public String getName() {
        return name;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public double getRating() {
        return rating;
    }
}
