package com.iverson.toby.rhealth;

/**
 * Created by Toby on 4/4/2015.
 *
 * Place class holds places loaded from Google map places API
 */
public class Place {
    public String vicinity;
    public float[] geometry; //array(0 => lat, 1 => lng)
    public String id;
    public String name;
    public float rating;
    public String reference;
    public String[] types;
    public int violationRating;
    public int idForV;

    public int getIdForV() {return idForV;}

    public void setIdForV(int idForV) {
        this.idForV = idForV;
    }

    public String getVicinity() {
        return vicinity;
    }

    public void setVicinity(String vicinity) {
        this.vicinity = vicinity;
    }

    public float[] getGeometry() {
        return geometry;
    }

    public void setGeometry(float[] geometry) {
        this.geometry = geometry;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public float getRating() {
        return rating;
    }

    public void setRating(float rating) {
        this.rating = rating;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public String[] getTypes() {
        return types;
    }

    public void setTypes(String[] types) {
        this.types = types;
    }

    public int getVRating() { return violationRating; }

    public void setVRating(int i) {this.violationRating = i;}

}