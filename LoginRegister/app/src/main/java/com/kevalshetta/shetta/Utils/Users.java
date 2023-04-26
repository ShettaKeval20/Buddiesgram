package com.kevalshetta.shetta.Utils;

public class Users {
    private String countryname, description, fullname, profileimage,status, username;

    public Users() {
    }

    public Users(String countryname, String description, String fullname, String profileimage, String status, String username) {
        this.countryname = countryname;
        this.description = description;
        this.fullname = fullname;
        this.profileimage = profileimage;
        this.status = status;
        this.username = username;
    }

    public String getCountryname() {
        return countryname;
    }

    public void setCountryname(String countryname) {
        this.countryname = countryname;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getFullname() {
        return fullname;
    }

    public void setFullname(String fullname) {
        this.fullname = fullname;
    }

    public String getProfileimage() {
        return profileimage;
    }

    public void setProfileimage(String profileimage) {
        this.profileimage = profileimage;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
