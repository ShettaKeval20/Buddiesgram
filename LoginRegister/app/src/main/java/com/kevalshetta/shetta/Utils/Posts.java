package com.kevalshetta.shetta.Utils;

public class Posts {
    private String date, description, postimage, profileimage, uid, username, countryname;

    public Posts() {}

    public Posts(String date, String description, String postimage, String profileimage, String uid, String username, String countryname) {
        this.date = date;
        this.description = description;
        this.postimage = postimage;
        this.profileimage = profileimage;
        this.uid = uid;
        this.username = username;
        this.countryname = countryname;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPostimage() {
        return postimage;
    }

    public void setPostimage(String postimage) {
        this.postimage = postimage;
    }

    public String getProfileimage() {
        return profileimage;
    }

    public void setProfileimage(String profileimage) {
        this.profileimage = profileimage;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getCountryname() {
        return countryname;
    }

    public void setCountryname(String countryname) {
        this.countryname = countryname;
    }
}
