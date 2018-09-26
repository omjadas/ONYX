package com.example.onyx.onyx.events;

public class PushNotificationEvent {
    private String title;
    private String message;
    private String username;
    private String uid;

    public PushNotificationEvent() {
    }

    public PushNotificationEvent(String title, String message, String username, String uid) {
        this.title = title;
        this.message = message;
        this.username = username;
        this.uid = uid;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }
}
