package com.kevalshetta.shetta.Utils;

public class Comments {
    private String date, comment, profileimage, username;

    public Comments() {
    }

    public Comments(String date, String comment, String profileimage, String username) {
        this.date = date;
        this.comment = comment;
        this.profileimage = profileimage;
        this.username = username;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getProfileimage() {
        return profileimage;
    }

    public void setProfileimage(String profileimage) {
        this.profileimage = profileimage;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
