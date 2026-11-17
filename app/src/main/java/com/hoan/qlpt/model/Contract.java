package com.hoan.qlpt.model;

public class Contract {
    private String id, requestId, roomId, roomTitle, landlordId, tenantId, tenantName;
    private long price, deposit, startDate, endDate, createdAt;
    private String status = "active"; // active | ended

    public Contract() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
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

    public long getPrice() {
        return price;
    }

    public void setPrice(long price) {
        this.price = price;
    }

    public long getDeposit() {
        return deposit;
    }

    public void setDeposit(long deposit) {
        this.deposit = deposit;
    }

    public long getStartDate() {
        return startDate;
    }

    public void setStartDate(long startDate) {
        this.startDate = startDate;
    }

    public long getEndDate() {
        return endDate;
    }

    public void setEndDate(long endDate) {
        this.endDate = endDate;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    private boolean endRequested;
    private long endRequestedAt, endedAt;

    public boolean isEndRequested() {
        return endRequested;
    }

    public void setEndRequested(boolean v) {
        endRequested = v;
    }

    public long getEndRequestedAt() {
        return endRequestedAt;
    }

    public void setEndRequestedAt(long v) {
        endRequestedAt = v;
    }

    public long getEndedAt() {
        return endedAt;
    }

    public void setEndedAt(long v) {
        endedAt = v;
    }
}