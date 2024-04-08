package com.company.scoolsocialmedia.model;

public class ChatMessageModel {
    private String msgKey;
    private String date;
    private String message;
    private String sender_id;
    private String status;
    private String type;
    private String imageUrl; // URL for image message
    private String videoThumbnailUrl; // URL for video thumbnail

    public String getImageUrl() {
        return imageUrl;
    }

    public String getVideoThumbnailUrl() {
        return videoThumbnailUrl;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public ChatMessageModel() {
    }

    public ChatMessageModel(String msgKey, String date, String message, String sender_id, String status, String type, String imageUrl, String videoThumbnailUrl) {
        this.msgKey = msgKey;
        this.date = date;
        this.message = message;
        this.sender_id = sender_id;
        this.status = status;
        this.type = type;
        this.imageUrl = imageUrl;
        this.videoThumbnailUrl = videoThumbnailUrl;
    }

    public ChatMessageModel(String date, String message, String sender_id, String status) {
        this.date = date;
        this.message = message;
        this.sender_id = sender_id;
        this.status = status;
    }

    public String getMsgKey() {
        return msgKey;
    }

    public void setMsgKey(String msgKey) {
        this.msgKey = msgKey;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getSender_id() {
        return sender_id;
    }

    public void setSender_id(String sender_id) {
        this.sender_id = sender_id;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
