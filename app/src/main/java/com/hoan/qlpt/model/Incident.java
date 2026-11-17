package com.hoan.qlpt.model;

import java.util.ArrayList;
import java.util.List;

public class Incident {
    private String id, contractId, roomId, roomTitle, landlordId, tenantId, tenantName;
    private String category, title, description, note;
    private List<String> imageUrls = new ArrayList<>();
    private String status = "sent"; // sent | processing | done
    private long createdAt, updatedAt;

    public Incident() {
    }

    public static String statusText(String s) {
        if ("processing".equals(s)) return "Đang xử lý";
        if ("done".equals(s)) return "Đã xong";
        return "Đã gửi";
    }

    public String getId() {
        return id;
    }

    public void setId(String v) {
        id = v;
    }

    public String getContractId() {
        return contractId;
    }

    public void setContractId(String v) {
        contractId = v;
    }

    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String v) {
        roomId = v;
    }

    public String getRoomTitle() {
        return roomTitle;
    }

    public void setRoomTitle(String v) {
        roomTitle = v;
    }

    public String getLandlordId() {
        return landlordId;
    }

    public void setLandlordId(String v) {
        landlordId = v;
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String v) {
        tenantId = v;
    }

    public String getTenantName() {
        return tenantName;
    }

    public void setTenantName(String v) {
        tenantName = v;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String v) {
        category = v;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String v) {
        title = v;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String v) {
        description = v;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String v) {
        note = v;
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

    public long getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(long v) {
        updatedAt = v;
    }
}