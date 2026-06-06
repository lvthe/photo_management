package com.example.photo_management.models;
import java.io.Serializable;

public class PrintOrder implements Serializable {
    private int id;
    private String code;
    private int customerId;
    private int serviceId;
    private double area;
    private int quantity;
    private double totalPrice;
    private String orderDate;
    private String note;

    public PrintOrder() {
    }

    public PrintOrder(int id, String code, int customerId, int serviceId,
                      double area, int quantity, double totalPrice,
                      String orderDate, String note) {
        this.id = id;
        this.code = code;
        this.customerId = customerId;
        this.serviceId = serviceId;
        this.area = area;
        this.quantity = quantity;
        this.totalPrice = totalPrice;
        this.orderDate = orderDate;
        this.note = note;
    }

    public PrintOrder(String code, int customerId, int serviceId,
                      double area, int quantity, double totalPrice,
                      String orderDate, String note) {
        this.code = code;
        this.customerId = customerId;
        this.serviceId = serviceId;
        this.area = area;
        this.quantity = quantity;
        this.totalPrice = totalPrice;
        this.orderDate = orderDate;
        this.note = note;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public int getCustomerId() {
        return customerId;
    }

    public void setCustomerId(int customerId) {
        this.customerId = customerId;
    }

    public int getServiceId() {
        return serviceId;
    }

    public void setServiceId(int serviceId) {
        this.serviceId = serviceId;
    }

    public double getArea() {
        return area;
    }

    public void setArea(double area) {
        this.area = area;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public double getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(double totalPrice) {
        this.totalPrice = totalPrice;
    }

    public String getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(String orderDate) {
        this.orderDate = orderDate;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    @Override
    public String toString() {
        return code + " - " + totalPrice + "đ";
    }
}