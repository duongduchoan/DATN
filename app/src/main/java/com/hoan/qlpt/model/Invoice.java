package com.hoan.qlpt.model;

public class Invoice {
    private String id, contractId, roomId, roomTitle, landlordId, tenantId;
    private int month, year;
    private long roomFee, elecOld, elecNew, elecPrice, waterOld, waterNew, waterPrice;
    private long otherFee, total, dueDate, createdAt;
    private String status = "unpaid"; // unpaid | pending | paid

    // Thông tin nhận tiền sao chép từ chủ trọ lúc tạo hóa đơn
    private String bankBin, bankName, bankAccount, bankHolder;
    private String payMethod = ""; // "" | cash | transfer

    public Invoice() {}

    public String getId() { return id; }
    public void setId(String v) { id = v; }
    public String getContractId() { return contractId; }
    public void setContractId(String v) { contractId = v; }
    public String getRoomId() { return roomId; }
    public void setRoomId(String v) { roomId = v; }
    public String getRoomTitle() { return roomTitle; }
    public void setRoomTitle(String v) { roomTitle = v; }
    public String getLandlordId() { return landlordId; }
    public void setLandlordId(String v) { landlordId = v; }
    public String getTenantId() { return tenantId; }
    public void setTenantId(String v) { tenantId = v; }
    public int getMonth() { return month; }
    public void setMonth(int v) { month = v; }
    public int getYear() { return year; }
    public void setYear(int v) { year = v; }
    public long getRoomFee() { return roomFee; }
    public void setRoomFee(long v) { roomFee = v; }
    public long getElecOld() { return elecOld; }
    public void setElecOld(long v) { elecOld = v; }
    public long getElecNew() { return elecNew; }
    public void setElecNew(long v) { elecNew = v; }
    public long getElecPrice() { return elecPrice; }
    public void setElecPrice(long v) { elecPrice = v; }
    public long getWaterOld() { return waterOld; }
    public void setWaterOld(long v) { waterOld = v; }
    public long getWaterNew() { return waterNew; }
    public void setWaterNew(long v) { waterNew = v; }
    public long getWaterPrice() { return waterPrice; }
    public void setWaterPrice(long v) { waterPrice = v; }
    public long getOtherFee() { return otherFee; }
    public void setOtherFee(long v) { otherFee = v; }
    public long getTotal() { return total; }
    public void setTotal(long v) { total = v; }
    public long getDueDate() { return dueDate; }
    public void setDueDate(long v) { dueDate = v; }
    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long v) { createdAt = v; }
    public String getStatus() { return status; }
    public void setStatus(String v) { status = v; }

    public String getBankBin() { return bankBin; }
    public void setBankBin(String v) { bankBin = v; }
    public String getBankName() { return bankName; }
    public void setBankName(String v) { bankName = v; }
    public String getBankAccount() { return bankAccount; }
    public void setBankAccount(String v) { bankAccount = v; }
    public String getBankHolder() { return bankHolder; }
    public void setBankHolder(String v) { bankHolder = v; }
    public String getPayMethod() { return payMethod; }
    public void setPayMethod(String v) { payMethod = v; }
}