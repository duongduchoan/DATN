package com.hoan.qlpt.model;

public class User {
    private String uid, email;
    private int roleId;
    private boolean locked;

    public User() {}
    public User(String uid, String email, int roleId) {
        this.uid = uid; this.email = email; this.roleId = roleId; this.locked = false;
    }
    public String getUid() { return uid; }
    public void setUid(String uid) { this.uid = uid; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public int getRoleId() { return roleId; }
    public void setRoleId(int roleId) { this.roleId = roleId; }
    public boolean isLocked() { return locked; }
    public void setLocked(boolean locked) { this.locked = locked; }
}