package com.example.photo_management.models;
import java.io.Serializable;

public class PrintService implements Serializable {
    private int id;
    private String code;
    private String printType;   // Mau / Den trang
    private String size;        // A3 / A4 / A5
    private double pricePerM2;

    public PrintService() {
    }

    public PrintService(int id, String code, String printType, String size, double pricePerM2) {
        this.id = id;
        this.code = code;
        this.printType = printType;
        this.size = size;
        this.pricePerM2 = pricePerM2;
    }

    public PrintService(String code, String printType, String size, double pricePerM2) {
        this.code = code;
        this.printType = printType;
        this.size = size;
        this.pricePerM2 = pricePerM2;
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

    public String getPrintType() {
        return printType;
    }

    public void setPrintType(String printType) {
        this.printType = printType;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public double getPricePerM2() {
        return pricePerM2;
    }

    public void setPricePerM2(double pricePerM2) {
        this.pricePerM2 = pricePerM2;
    }

    @Override
    public String toString() {
        return code + " - " + printType + " - " + size + " - " + pricePerM2 + "đ/m²";
    }
}