package com.hoan.qlpt.model;

public class House {
    private String id, landlordId, name, address, district, description;
    private double lat, lng;
    private long createdAt;
    private String status = "pending";

    public House() {}

    public String getId() { return id; }
    public void setId(String v) { id = v; }
    public String getLandlordId() { return landlordId; }
    public void setLandlordId(String v) { landlordId = v; }
    public String getName() { return name; }
    public void setName(String v) { name = v; }
    public String getAddress() { return address; }
    public void setAddress(String v) { address = v; }
    public String getDistrict() { return district; }
    public void setDistrict(String v) { district = v; }
    public String getDescription() { return description; }
    public void setDescription(String v) { description = v; }
    public double getLat() { return lat; }
    public void setLat(double v) { lat = v; }
    public double getLng() { return lng; }
    public void setLng(double v) { lng = v; }
    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long v) { createdAt = v; }
    public String getStatus() { return status; }
    public void setStatus(String v) { status = v; }
    private String rejectReason;

    public String getRejectReason() { return rejectReason; }
    public void setRejectReason(String v) { rejectReason = v; }
}