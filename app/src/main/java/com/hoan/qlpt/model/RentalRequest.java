package com.hoan.qlpt.model;

public class RentalRequest {
    private String id, roomId, roomTitle, landlordId, tenantId, tenantName, tenantPhone, message;
    private String status = "pending";
    private long createdAt;

    public RentalRequest() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    public String getRoomTitle() {
        return roomTitle;
    }

    public void setRoomTitle(String roomTitle) {
        this.roomTitle = roomTitle;
    }

    public String getLandlordId() {
        return landlordId;
    }

    public void setLandlordId(String landlordId) {
        this.landlordId = landlordId;
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public String getTenantName() {
        return tenantName;
    }

    public void setTenantName(String tenantName) {
        this.tenantName = tenantName;
    }

    public String getTenantPhone() {
        return tenantPhone;
    }

    public void setTenantPhone(String tenantPhone) {
        this.tenantPhone = tenantPhone;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    private long visitDate;
    private String expectedMoveIn = "", pets = "";
    private int peopleCount, vehicleCount;

    public long getVisitDate() {
        return visitDate;
    }

    public void setVisitDate(long v) {
        visitDate = v;
    }

    public String getExpectedMoveIn() {
        return expectedMoveIn;
    }

    public void setExpectedMoveIn(String v) {
        expectedMoveIn = v;
    }

    public String getPets() {
        return pets;
    }

    public void setPets(String v) {
        pets = v;
    }

    public int getPeopleCount() {
        return peopleCount;
    }

    public void setPeopleCount(int v) {
        peopleCount = v;
    }

    public int getVehicleCount() {
        return vehicleCount;
    }

    public void setVehicleCount(int v) {
        vehicleCount = v;
    }
}