package com.hoan.qlpt.model;

public class AppNotification {
    private String id, userId, title, body;
    private boolean read;
    private long createdAt;

    public AppNotification() {
    }

    public String getId() {
        return id;
    }

    public void setId(String v) {
        id = v;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String v) {
        userId = v;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String v) {
        title = v;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String v) {
        body = v;
    }

    public boolean isRead() {
        return read;
    }

    public void setRead(boolean v) {
        read = v;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long v) {
        createdAt = v;
    }

    private String type = "", refId = "";

    public String getType() {
        return type;
    }

    public void setType(String v) {
        type = v;
    }

    public String getRefId() {
        return refId;
    }

    public void setRefId(String v) {
        refId = v;
    }
}