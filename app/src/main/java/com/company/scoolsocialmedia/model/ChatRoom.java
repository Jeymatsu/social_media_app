package com.company.scoolsocialmedia.model;

import java.util.List;

public class ChatRoom {
    private String roomId;
    private String name;


    public List<String> getParticipants() {
        return participants;
    }


    public ChatRoom(){

    }

    public void setParticipants(List<String> participants) {
        this.participants = participants;
    }

    private String description;
    private List<String> participants; // List of user IDs participating in the chat room

    public ChatRoom(String roomId, String name, String description, List<String> participants) {
        this.roomId = roomId;
        this.name = name;
        this.description = description;
        this.participants = participants;
    }

    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
