package com.hoan.qlpt.model;

public class ApprovalItem {
    public String id, title, line1, line2;
    public Object data;

    public ApprovalItem(String id, String title, String line1, String line2, Object data) {
        this.id = id; this.title = title; this.line1 = line1; this.line2 = line2; this.data = data;
    }

    public interface OnItemClick { void onClick(ApprovalItem item); }

    private boolean showActions = true;
    private OnItemClick onItemClick;

    public void setShowActions(boolean show) { showActions = show; }
    public void setOnItemClick(OnItemClick l) { onItemClick = l; }
}