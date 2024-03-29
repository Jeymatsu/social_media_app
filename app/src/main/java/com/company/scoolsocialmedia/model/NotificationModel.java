package com.company.scoolsocialmedia.model;

public class NotificationModel {

    String message, title,userID;

    public NotificationModel() {
    }

    public NotificationModel(String message, String title, String userID) {
        this.message = message;
        this.title = title;
        this.userID = userID;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
