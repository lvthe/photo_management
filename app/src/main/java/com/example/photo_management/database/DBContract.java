package com.example.photo_management.database;

public final class DBContract {

    private DBContract() {
    }

    public static final class CustomerTable {
        public static final String TABLE_NAME = "customers";
        public static final String ID = "id";
        public static final String CODE = "code";
        public static final String NAME = "name";
        public static final String PHONE = "phone";
    }

    public static final class ServiceTable {
        public static final String TABLE_NAME = "print_services";
        public static final String ID = "id";
        public static final String CODE = "code";
        public static final String PRINT_TYPE = "print_type";
        public static final String SIZE = "size";
        public static final String PRICE_PER_M2 = "price_per_m2";
    }

    public static final class OrderTable {
        public static final String TABLE_NAME = "print_orders";
        public static final String ID = "id";
        public static final String CODE = "code";
        public static final String CUSTOMER_ID = "customer_id";
        public static final String SERVICE_ID = "service_id";
        public static final String AREA = "area";
        public static final String QUANTITY = "quantity";
        public static final String TOTAL_PRICE = "total_price";
        public static final String ORDER_DATE = "order_date";
        public static final String NOTE = "note";
    }
}