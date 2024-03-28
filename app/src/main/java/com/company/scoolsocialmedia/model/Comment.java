package com.company.scoolsocialmedia.model;

public class Comment {
    private String postId;
    private String userId;
    private String username;
    private String text;

    public Comment() {
        // Default constructor required for Firebase
    }

    public Comment(String postId, String userId, String username, String text) {
        this.postId = postId;
        this.userId = userId;
        this.username = username;
        this.text = text;
    }

    public String getPostId() {
        return postId;
    }

    public void setPostId(String postId) {
        this.postId = postId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
