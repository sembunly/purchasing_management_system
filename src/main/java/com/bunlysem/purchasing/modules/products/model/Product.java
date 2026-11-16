package com.bunlysem.purchasing.modules.products.model;

import jakarta.persistence.*;

@Entity @Table(name = "products")
public class Product {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(name = "category_id", nullable = false) private Long categoryId;
    @Column(nullable = false, unique = true) private String sku;
    @Column(nullable = false) private String name;
    @Column(nullable = false) private String unit = "PCS";
    private String description;
    @Column(name = "is_active", nullable = false) private Integer isActive = 1;
    @Lob @Column(name = "image_data") private byte[] imageData;
    @Column(name = "image_filename") private String imageFilename;
    @Column(name = "image_content_type") private String imageContentType;

    public Long getId() { return id; }
    public Long getCategoryId() { return categoryId; }
    public String getSku() { return sku; }
    public String getName() { return name; }
    public String getUnit() { return unit; }
    public String getDescription() { return description; }
    public Integer getIsActive() { return isActive; }
    public byte[] getImageData() { return imageData; }
    public String getImageFilename() { return imageFilename; }
    public String getImageContentType() { return imageContentType; }
    public void setCategoryId(Long value) { categoryId = value; }
    public void setSku(String value) { sku = value; }
    public void setName(String value) { name = value; }
    public void setUnit(String value) { unit = value; }
    public void setDescription(String value) { description = value; }
    public void setIsActive(Integer value) { isActive = value; }
    public void setImageData(byte[] value) { imageData = value; }
    public void setImageFilename(String value) { imageFilename = value; }
    public void setImageContentType(String value) { imageContentType = value; }
}
