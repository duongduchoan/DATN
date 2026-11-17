package com.hoan.qlpt.model;

import java.util.ArrayList;
import java.util.List;

public class Room {
    private String id, landlordId, title, address, district, description;
    private long price, createdAt;
    private double area, lat, lng;
    private List<String> amenities = new ArrayList<>();
    private List<String> imageUrls = new ArrayList<>();
    private String status = "pending";        // pending | approved | rejected
    private String rentStatus = "available";  // available | rented
    private String houseId, houseName, roomNumber;
    private boolean hidden = false;

    public Room() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getLandlordId() {
        return landlordId;
    }

    public void setLandlordId(String landlordId) {
        this.landlordId = landlordId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getDistrict() {
        return district;
    }

    public void setDistrict(String district) {
        this.district = district;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public long getPrice() {
        return price;
    }

    public void setPrice(long price) {
        this.price = price;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public double getArea() {
        return area;
    }

    public void setArea(double area) {
        this.area = area;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLng() {
        return lng;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }

    public List<String> getAmenities() {
        return amenities;
    }

    public void setAmenities(List<String> amenities) {
        this.amenities = amenities;
    }

    public List<String> getImageUrls() {
        return imageUrls;
    }

    public void setImageUrls(List<String> imageUrls) {
        this.imageUrls = imageUrls;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getRentStatus() {
        return rentStatus;
    }

    public void setRentStatus(String rentStatus) {
        this.rentStatus = rentStatus;
    }

    public String getHouseId() {
        return houseId;
    }

    public void setHouseId(String v) {
        houseId = v;
    }

    public String getHouseName() {
        return houseName;
    }

    public void setHouseName(String v) {
        houseName = v;
    }

    public String getRoomNumber() {
        return roomNumber;
    }

    public void setRoomNumber(String v) {
        roomNumber = v;
    }

    public boolean isHidden() {
        return hidden;
    }

    public void setHidden(boolean v) {
        hidden = v;
    }

    private String rejectReason;

    public String getRejectReason() {
        return rejectReason;
    }

    public void setRejectReason(String v) {
        rejectReason = v;
    }

    private String floor = "", toilet = "", curfew = "", washer = "", window = "", balcony = "";
    private String pets = "", parking = "", evCharge = "";
    private long elecPrice, waterPrice, serviceFee, parkingFee;
    private int maxPeople, maxVehicles;

    public String getFloor() {
        return floor;
    }

    public void setFloor(String v) {
        floor = v;
    }

    public String getToilet() {
        return toilet;
    }

    public void setToilet(String v) {
        toilet = v;
    }

    public String getCurfew() {
        return curfew;
    }

    public void setCurfew(String v) {
        curfew = v;
    }

    public String getWasher() {
        return washer;
    }

    public void setWasher(String v) {
        washer = v;
    }

    public String getWindow() {
        return window;
    }

    public void setWindow(String v) {
        window = v;
    }

    public String getBalcony() {
        return balcony;
    }

    public void setBalcony(String v) {
        balcony = v;
    }

    public String getPets() {
        return pets;
    }

    public void setPets(String v) {
        pets = v;
    }

    public String getParking() {
        return parking;
    }

    public void setParking(String v) {
        parking = v;
    }

    public String getEvCharge() {
        return evCharge;
    }

    public void setEvCharge(String v) {
        evCharge = v;
    }

    public long getElecPrice() {
        return elecPrice;
    }

    public void setElecPrice(long v) {
        elecPrice = v;
    }

    public long getWaterPrice() {
        return waterPrice;
    }

    public void setWaterPrice(long v) {
        waterPrice = v;
    }

    public long getServiceFee() {
        return serviceFee;
    }

    public void setServiceFee(long v) {
        serviceFee = v;
    }

    public long getParkingFee() {
        return parkingFee;
    }

    public void setParkingFee(long v) {
        parkingFee = v;
    }

    public int getMaxPeople() {
        return maxPeople;
    }

    public void setMaxPeople(int v) {
        maxPeople = v;
    }

    public int getMaxVehicles() {
        return maxVehicles;
    }

    public void setMaxVehicles(int v) {
        maxVehicles = v;
    }
}