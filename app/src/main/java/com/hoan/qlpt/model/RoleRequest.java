package com.hoan.qlpt.model;

import java.util.ArrayList;
import java.util.List;

public class RoleRequest {
    private String id, uid, fullName, phone, email, cccd, houseAddress, description, rejectReason;
    private int houseCount, roomCount;
    private List<String> imageUrls = new ArrayList<>();
    private String status = "pending"; // pending | approved | rejected
    private long createdAt;

    public RoleRequest() {
    }

    public String getId() {
        return id;
    }

    public void setId(String v) {
        id = v;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String v) {
        uid = v;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String v) {
        fullName = v;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String v) {
        phone = v;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String v) {
        email = v;
    }

    public String getCccd() {
        return cccd;
    }

    public void setCccd(String v) {
        cccd = v;
    }

    public String getHouseAddress() {
        return houseAddress;
    }

    public void setHouseAddress(String v) {
        houseAddress = v;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String v) {
        description = v;
    }

    public String getRejectReason() {
        return rejectReason;
    }

    public void setRejectReason(String v) {
        rejectReason = v;
    }

    public int getHouseCount() {
        return houseCount;
    }

    public void setHouseCount(int v) {
        houseCount = v;
    }

    public int getRoomCount() {
        return roomCount;
    }

    public void setRoomCount(int v) {
        roomCount = v;
    }

    public List<String> getImageUrls() {
        return imageUrls;
    }

    public void setImageUrls(List<String> v) {
        imageUrls = v;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String v) {
        status = v;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long v) {
        createdAt = v;
    }
}