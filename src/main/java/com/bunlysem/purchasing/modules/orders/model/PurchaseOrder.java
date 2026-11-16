package com.bunlysem.purchasing.modules.orders.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity @Table(name = "purchase_orders")
public class PurchaseOrder {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(name = "order_no") private String orderNo;
    @Column(name = "purchase_request_id") private Long purchaseRequestId;
    @Column(name = "supplier_id") private Long supplierId;
    @Column(name = "ordered_by") private Long orderedBy;
    @Column(name = "order_date") private LocalDateTime orderDate;
    private String status;
    private String notes;
    @Column(name = "is_active") private Integer isActive;
    @Lob @Column(name = "report_file") private byte[] reportFile;
    @Column(name = "report_filename") private String reportFilename;
    @Column(name = "report_content_type") private String reportContentType;

    public Long getId() { return id; }
    public String getOrderNo() { return orderNo; }
    public byte[] getReportFile() { return reportFile; }
    public String getReportFilename() { return reportFilename; }
    public void setReportFile(byte[] value) { reportFile = value; }
    public void setReportFilename(String value) { reportFilename = value; }
    public void setReportContentType(String value) { reportContentType = value; }
}
