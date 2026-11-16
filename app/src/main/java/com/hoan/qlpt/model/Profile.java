package com.hoan.qlpt.model;

public class Profile {
    private String uid, fullName, phone, avatarUrl;

    public Profile() {}
    public Profile(String uid, String fullName, String phone) {
        this.uid = uid; this.fullName = fullName; this.phone = phone; this.avatarUrl = "";
    }
    public String getUid() { return uid; }
    public void setUid(String uid) { this.uid = uid; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getAvatarUrl() { return avatarUrl; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }
}