package com.company.scoolsocialmedia.model;

public class ChatMessage {
    private String messageId;
    private String senderId;
    private String messageText;
    private String imageUrl;
    private String pdfUrl;
    private String videoUrl;
    private long timestamp;

    public enum MessageType {
        TEXT, IMAGE, VIDEO, PDF
    }

    private MessageType messageType;

    public MessageType getMessageType() {
        return messageType;
    }

    public void setMessageType(MessageType messageType) {
        this.messageType = messageType;
    }

    public ChatMessage(){

    }

    public ChatMessage(String messageId, String senderId, String messageText, String imageUrl, String pdfUrl, String videoUrl, long timestamp) {
        this.messageId = messageId;
        this.senderId = senderId;
        this.messageText = messageText;
        this.imageUrl = imageUrl;
        this.pdfUrl = pdfUrl;
        this.videoUrl = videoUrl;
        this.timestamp = timestamp;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public String getMessageText() {
        return messageText;
    }

    public void setMessageText(String messageText) {
        this.messageText = messageText;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getPdfUrl() {
        return pdfUrl;
    }

    public void setPdfUrl(String pdfUrl) {
        this.pdfUrl = pdfUrl;
    }

    public String getVideoUrl() {
        return videoUrl;
    }

    public void setVideoUrl(String videoUrl) {
        this.videoUrl = videoUrl;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
