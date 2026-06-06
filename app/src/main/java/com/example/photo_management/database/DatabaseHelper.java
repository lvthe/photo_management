package com.example.photo_management.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.photo_management.models.Customer;
import com.example.photo_management.models.OrderDetail;
import com.example.photo_management.models.PrintOrder;
import com.example.photo_management.models.PrintService;

import static com.example.photo_management.database.DBContract.CustomerTable;
import static com.example.photo_management.database.DBContract.ServiceTable;
import static com.example.photo_management.database.DBContract.OrderTable;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "print_service_db";
    private static final int DATABASE_VERSION = 1;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createCustomersTable = "CREATE TABLE " + CustomerTable.TABLE_NAME + " ("
                + CustomerTable.ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + CustomerTable.CODE + " TEXT UNIQUE, "
                + CustomerTable.NAME + " TEXT NOT NULL, "
                + CustomerTable.PHONE + " TEXT NOT NULL"
                + ")";

        String createServicesTable = "CREATE TABLE " + ServiceTable.TABLE_NAME + " ("
                + ServiceTable.ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + ServiceTable.CODE + " TEXT UNIQUE, "
                + ServiceTable.PRINT_TYPE + " TEXT NOT NULL, "
                + ServiceTable.SIZE + " TEXT NOT NULL, "
                + ServiceTable.PRICE_PER_M2 + " REAL NOT NULL"
                + ")";

        String createOrdersTable = "CREATE TABLE " + OrderTable.TABLE_NAME + " ("
                + OrderTable.ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + OrderTable.CODE + " TEXT UNIQUE, "
                + OrderTable.CUSTOMER_ID + " INTEGER NOT NULL, "
                + OrderTable.SERVICE_ID + " INTEGER NOT NULL, "
                + OrderTable.AREA + " REAL NOT NULL, "
                + OrderTable.QUANTITY + " INTEGER NOT NULL, "
                + OrderTable.TOTAL_PRICE + " REAL NOT NULL, "
                + OrderTable.ORDER_DATE + " TEXT, "
                + OrderTable.NOTE + " TEXT, "
                + "FOREIGN KEY(" + OrderTable.CUSTOMER_ID + ") REFERENCES " + CustomerTable.TABLE_NAME + "(" + CustomerTable.ID + "), "
                + "FOREIGN KEY(" + OrderTable.SERVICE_ID + ") REFERENCES " + ServiceTable.TABLE_NAME + "(" + ServiceTable.ID + ")"
                + ")";

        db.execSQL(createCustomersTable);
        db.execSQL(createServicesTable);
        db.execSQL(createOrdersTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + OrderTable.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + ServiceTable.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + CustomerTable.TABLE_NAME);
        onCreate(db);
    }

    // =========================
    // CODE GENERATORS
    // =========================

    public String generateNextCustomerCode() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT MAX(CAST(SUBSTR(" + CustomerTable.CODE + ", 3) AS INTEGER)) FROM " + CustomerTable.TABLE_NAME
                        + " WHERE " + CustomerTable.CODE + " LIKE 'KH%'", null);
        int nextSeq = 1;
        if (cursor.moveToFirst() && !cursor.isNull(0)) {
            nextSeq = cursor.getInt(0) + 1;
        }
        cursor.close();
        return String.format("KH%03d", nextSeq);
    }

    public String generateNextServiceCode() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT MAX(CAST(SUBSTR(" + ServiceTable.CODE + ", 3) AS INTEGER)) FROM " + ServiceTable.TABLE_NAME
                        + " WHERE " + ServiceTable.CODE + " LIKE 'DV%'", null);
        int nextSeq = 1;
        if (cursor.moveToFirst() && !cursor.isNull(0)) {
            nextSeq = cursor.getInt(0) + 1;
        }
        cursor.close();
        return String.format("DV%03d", nextSeq);
    }

    public String generateNextOrderCode() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT MAX(CAST(SUBSTR(" + OrderTable.CODE + ", 3) AS INTEGER)) FROM " + OrderTable.TABLE_NAME
                        + " WHERE " + OrderTable.CODE + " LIKE 'DH%'", null);
        int nextSeq = 1;
        if (cursor.moveToFirst() && !cursor.isNull(0)) {
            nextSeq = cursor.getInt(0) + 1;
        }
        cursor.close();
        return String.format("DH%03d", nextSeq);
    }

    // =========================
    // CONSTRAINT CHECKS
    // =========================

    public boolean isPhoneExists(String phone, int excludeCustomerId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT COUNT(*) FROM " + CustomerTable.TABLE_NAME
                        + " WHERE " + CustomerTable.PHONE + " = ? AND " + CustomerTable.ID + " != ?",
                new String[]{phone, String.valueOf(excludeCustomerId)});
        boolean exists = false;
        if (cursor.moveToFirst()) {
            exists = cursor.getInt(0) > 0;
        }
        cursor.close();
        return exists;
    }

    public boolean customerHasOrders(int customerId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT COUNT(*) FROM " + OrderTable.TABLE_NAME + " WHERE " + OrderTable.CUSTOMER_ID + " = ?",
                new String[]{String.valueOf(customerId)});
        boolean hasOrders = false;
        if (cursor.moveToFirst()) {
            hasOrders = cursor.getInt(0) > 0;
        }
        cursor.close();
        return hasOrders;
    }

    public boolean isServiceExists(String printType, String size, int excludeServiceId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT COUNT(*) FROM " + ServiceTable.TABLE_NAME
                        + " WHERE " + ServiceTable.PRINT_TYPE + " = ? AND " + ServiceTable.SIZE + " = ?"
                        + " AND " + ServiceTable.ID + " != ?",
                new String[]{printType, size, String.valueOf(excludeServiceId)});
        boolean exists = false;
        if (cursor.moveToFirst()) {
            exists = cursor.getInt(0) > 0;
        }
        cursor.close();
        return exists;
    }

    public boolean serviceHasOrders(int serviceId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT COUNT(*) FROM " + OrderTable.TABLE_NAME + " WHERE " + OrderTable.SERVICE_ID + " = ?",
                new String[]{String.valueOf(serviceId)});
        boolean hasOrders = false;
        if (cursor.moveToFirst()) {
            hasOrders = cursor.getInt(0) > 0;
        }
        cursor.close();
        return hasOrders;
    }

    // =========================
    // CUSTOMER CRUD
    // =========================

    public long insertCustomer(Customer customer) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(CustomerTable.CODE, customer.getCode());
        values.put(CustomerTable.NAME, customer.getName());
        values.put(CustomerTable.PHONE, customer.getPhone());
        return db.insert(CustomerTable.TABLE_NAME, null, values);
    }

    public int updateCustomer(Customer customer) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(CustomerTable.CODE, customer.getCode());
        values.put(CustomerTable.NAME, customer.getName());
        values.put(CustomerTable.PHONE, customer.getPhone());
        return db.update(CustomerTable.TABLE_NAME, values, CustomerTable.ID + "=?",
                new String[]{String.valueOf(customer.getId())});
    }

    public int deleteCustomer(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(CustomerTable.TABLE_NAME, CustomerTable.ID + "=?",
                new String[]{String.valueOf(id)});
    }

    public List<Customer> getAllCustomers() {
        List<Customer> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + CustomerTable.TABLE_NAME
                + " ORDER BY " + CustomerTable.ID + " DESC", null);
        if (cursor.moveToFirst()) {
            do {
                Customer customer = new Customer();
                customer.setId(cursor.getInt(cursor.getColumnIndexOrThrow(CustomerTable.ID)));
                customer.setCode(cursor.getString(cursor.getColumnIndexOrThrow(CustomerTable.CODE)));
                customer.setName(cursor.getString(cursor.getColumnIndexOrThrow(CustomerTable.NAME)));
                customer.setPhone(cursor.getString(cursor.getColumnIndexOrThrow(CustomerTable.PHONE)));
                list.add(customer);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return list;
    }

    public Customer getCustomerById(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(CustomerTable.TABLE_NAME, null, CustomerTable.ID + "=?",
                new String[]{String.valueOf(id)}, null, null, null);
        Customer customer = null;
        if (cursor.moveToFirst()) {
            customer = new Customer();
            customer.setId(cursor.getInt(cursor.getColumnIndexOrThrow(CustomerTable.ID)));
            customer.setCode(cursor.getString(cursor.getColumnIndexOrThrow(CustomerTable.CODE)));
            customer.setName(cursor.getString(cursor.getColumnIndexOrThrow(CustomerTable.NAME)));
            customer.setPhone(cursor.getString(cursor.getColumnIndexOrThrow(CustomerTable.PHONE)));
        }
        cursor.close();
        return customer;
    }

    // =========================
    // SERVICE CRUD
    // =========================

    public long insertService(PrintService service) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(ServiceTable.CODE, service.getCode());
        values.put(ServiceTable.PRINT_TYPE, service.getPrintType());
        values.put(ServiceTable.SIZE, service.getSize());
        values.put(ServiceTable.PRICE_PER_M2, service.getPricePerM2());
        return db.insert(ServiceTable.TABLE_NAME, null, values);
    }

    public int updateService(PrintService service) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(ServiceTable.CODE, service.getCode());
        values.put(ServiceTable.PRINT_TYPE, service.getPrintType());
        values.put(ServiceTable.SIZE, service.getSize());
        values.put(ServiceTable.PRICE_PER_M2, service.getPricePerM2());
        return db.update(ServiceTable.TABLE_NAME, values, ServiceTable.ID + "=?",
                new String[]{String.valueOf(service.getId())});
    }

    public int deleteService(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(ServiceTable.TABLE_NAME, ServiceTable.ID + "=?",
                new String[]{String.valueOf(id)});
    }

    public List<PrintService> getAllServices() {
        List<PrintService> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + ServiceTable.TABLE_NAME
                + " ORDER BY " + ServiceTable.ID + " DESC", null);
        if (cursor.moveToFirst()) {
            do {
                PrintService service = new PrintService();
                service.setId(cursor.getInt(cursor.getColumnIndexOrThrow(ServiceTable.ID)));
                service.setCode(cursor.getString(cursor.getColumnIndexOrThrow(ServiceTable.CODE)));
                service.setPrintType(cursor.getString(cursor.getColumnIndexOrThrow(ServiceTable.PRINT_TYPE)));
                service.setSize(cursor.getString(cursor.getColumnIndexOrThrow(ServiceTable.SIZE)));
                service.setPricePerM2(cursor.getDouble(cursor.getColumnIndexOrThrow(ServiceTable.PRICE_PER_M2)));
                list.add(service);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return list;
    }

    public PrintService getServiceById(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(ServiceTable.TABLE_NAME, null, ServiceTable.ID + "=?",
                new String[]{String.valueOf(id)}, null, null, null);
        PrintService service = null;
        if (cursor.moveToFirst()) {
            service = new PrintService();
            service.setId(cursor.getInt(cursor.getColumnIndexOrThrow(ServiceTable.ID)));
            service.setCode(cursor.getString(cursor.getColumnIndexOrThrow(ServiceTable.CODE)));
            service.setPrintType(cursor.getString(cursor.getColumnIndexOrThrow(ServiceTable.PRINT_TYPE)));
            service.setSize(cursor.getString(cursor.getColumnIndexOrThrow(ServiceTable.SIZE)));
            service.setPricePerM2(cursor.getDouble(cursor.getColumnIndexOrThrow(ServiceTable.PRICE_PER_M2)));
        }
        cursor.close();
        return service;
    }

    // =========================
    // ORDER CRUD
    // =========================

    public long insertOrder(PrintOrder order) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(OrderTable.CODE, order.getCode());
        values.put(OrderTable.CUSTOMER_ID, order.getCustomerId());
        values.put(OrderTable.SERVICE_ID, order.getServiceId());
        values.put(OrderTable.AREA, order.getArea());
        values.put(OrderTable.QUANTITY, order.getQuantity());
        values.put(OrderTable.TOTAL_PRICE, order.getTotalPrice());
        values.put(OrderTable.ORDER_DATE, order.getOrderDate());
        values.put(OrderTable.NOTE, order.getNote());
        return db.insert(OrderTable.TABLE_NAME, null, values);
    }

    public int updateOrder(PrintOrder order) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(OrderTable.CODE, order.getCode());
        values.put(OrderTable.CUSTOMER_ID, order.getCustomerId());
        values.put(OrderTable.SERVICE_ID, order.getServiceId());
        values.put(OrderTable.AREA, order.getArea());
        values.put(OrderTable.QUANTITY, order.getQuantity());
        values.put(OrderTable.TOTAL_PRICE, order.getTotalPrice());
        values.put(OrderTable.ORDER_DATE, order.getOrderDate());
        values.put(OrderTable.NOTE, order.getNote());
        return db.update(OrderTable.TABLE_NAME, values, OrderTable.ID + "=?",
                new String[]{String.valueOf(order.getId())});
    }

    public int deleteOrder(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(OrderTable.TABLE_NAME, OrderTable.ID + "=?",
                new String[]{String.valueOf(id)});
    }

    public List<PrintOrder> getAllOrders() {
        List<PrintOrder> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + OrderTable.TABLE_NAME
                + " ORDER BY " + OrderTable.ID + " DESC", null);
        if (cursor.moveToFirst()) {
            do {
                PrintOrder order = new PrintOrder();
                order.setId(cursor.getInt(cursor.getColumnIndexOrThrow(OrderTable.ID)));
                order.setCode(cursor.getString(cursor.getColumnIndexOrThrow(OrderTable.CODE)));
                order.setCustomerId(cursor.getInt(cursor.getColumnIndexOrThrow(OrderTable.CUSTOMER_ID)));
                order.setServiceId(cursor.getInt(cursor.getColumnIndexOrThrow(OrderTable.SERVICE_ID)));
                order.setArea(cursor.getDouble(cursor.getColumnIndexOrThrow(OrderTable.AREA)));
                order.setQuantity(cursor.getInt(cursor.getColumnIndexOrThrow(OrderTable.QUANTITY)));
                order.setTotalPrice(cursor.getDouble(cursor.getColumnIndexOrThrow(OrderTable.TOTAL_PRICE)));
                order.setOrderDate(cursor.getString(cursor.getColumnIndexOrThrow(OrderTable.ORDER_DATE)));
                order.setNote(cursor.getString(cursor.getColumnIndexOrThrow(OrderTable.NOTE)));
                list.add(order);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return list;
    }

    public PrintOrder getOrderById(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(OrderTable.TABLE_NAME, null, OrderTable.ID + "=?",
                new String[]{String.valueOf(id)}, null, null, null);
        PrintOrder order = null;
        if (cursor.moveToFirst()) {
            order = new PrintOrder();
            order.setId(cursor.getInt(cursor.getColumnIndexOrThrow(OrderTable.ID)));
            order.setCode(cursor.getString(cursor.getColumnIndexOrThrow(OrderTable.CODE)));
            order.setCustomerId(cursor.getInt(cursor.getColumnIndexOrThrow(OrderTable.CUSTOMER_ID)));
            order.setServiceId(cursor.getInt(cursor.getColumnIndexOrThrow(OrderTable.SERVICE_ID)));
            order.setArea(cursor.getDouble(cursor.getColumnIndexOrThrow(OrderTable.AREA)));
            order.setQuantity(cursor.getInt(cursor.getColumnIndexOrThrow(OrderTable.QUANTITY)));
            order.setTotalPrice(cursor.getDouble(cursor.getColumnIndexOrThrow(OrderTable.TOTAL_PRICE)));
            order.setOrderDate(cursor.getString(cursor.getColumnIndexOrThrow(OrderTable.ORDER_DATE)));
            order.setNote(cursor.getString(cursor.getColumnIndexOrThrow(OrderTable.NOTE)));
        }
        cursor.close();
        return order;
    }

    // =========================
    // QUERY JOIN
    // =========================

    public List<OrderDetail> getAllOrderDetails() {
        List<OrderDetail> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        String sql = "SELECT o.id, o.code, c.name AS customer_name, s.print_type, s.size, " +
                "o.area, o.quantity, o.total_price, o.order_date, o.note " +
                "FROM " + OrderTable.TABLE_NAME + " o " +
                "JOIN " + CustomerTable.TABLE_NAME + " c ON o.customer_id = c.id " +
                "JOIN " + ServiceTable.TABLE_NAME + " s ON o.service_id = s.id " +
                "ORDER BY o.id DESC";
        Cursor cursor = db.rawQuery(sql, null);
        if (cursor.moveToFirst()) {
            do {
                list.add(cursorToOrderDetail(cursor));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return list;
    }

    /**
     * Tìm đơn theo bộ lọc động.
     * @param customerId id khách hàng; &lt;= 0 = mọi khách hàng.
     * @param size       khổ giấy ("A3"/"A4"/"A5"); null hoặc rỗng = mọi khổ.
     * @param printType  loại in ("Mau"/"Den trang"); null hoặc rỗng = mọi loại.
     * @param minPrice   thành tiền tối thiểu (lấy các đơn có total_price > minPrice).
     *                   Truyền số âm để không giới hạn (lấy tất cả).
     */
    public List<OrderDetail> searchOrders(int customerId, String size, String printType, double minPrice) {
        List<OrderDetail> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        StringBuilder sql = new StringBuilder(
                "SELECT o.id, o.code, c.name AS customer_name, s.print_type, s.size, " +
                        "o.area, o.quantity, o.total_price, o.order_date, o.note " +
                        "FROM " + OrderTable.TABLE_NAME + " o " +
                        "JOIN " + CustomerTable.TABLE_NAME + " c ON o.customer_id = c.id " +
                        "JOIN " + ServiceTable.TABLE_NAME + " s ON o.service_id = s.id " +
                        "WHERE o." + OrderTable.TOTAL_PRICE + " > ? ");

        List<String> args = new ArrayList<>();
        args.add(String.valueOf(minPrice));

        if (customerId > 0) {
            sql.append("AND c.").append(CustomerTable.ID).append(" = ? ");
            args.add(String.valueOf(customerId));
        }
        if (size != null && !size.isEmpty()) {
            sql.append("AND s.").append(ServiceTable.SIZE).append(" = ? ");
            args.add(size);
        }
        if (printType != null && !printType.isEmpty()) {
            sql.append("AND s.").append(ServiceTable.PRINT_TYPE).append(" = ? ");
            args.add(printType);
        }
        sql.append("ORDER BY o." + OrderTable.ID + " DESC");

        Cursor cursor = db.rawQuery(sql.toString(), args.toArray(new String[0]));
        if (cursor.moveToFirst()) {
            do {
                list.add(cursorToOrderDetail(cursor));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return list;
    }

    private OrderDetail cursorToOrderDetail(Cursor cursor) {
        OrderDetail detail = new OrderDetail();
        detail.setId(cursor.getInt(cursor.getColumnIndexOrThrow("id")));
        detail.setOrderCode(cursor.getString(cursor.getColumnIndexOrThrow("code")));
        detail.setCustomerName(cursor.getString(cursor.getColumnIndexOrThrow("customer_name")));
        detail.setPrintType(cursor.getString(cursor.getColumnIndexOrThrow("print_type")));
        detail.setSize(cursor.getString(cursor.getColumnIndexOrThrow("size")));
        detail.setArea(cursor.getDouble(cursor.getColumnIndexOrThrow("area")));
        detail.setQuantity(cursor.getInt(cursor.getColumnIndexOrThrow("quantity")));
        detail.setTotalPrice(cursor.getDouble(cursor.getColumnIndexOrThrow("total_price")));
        detail.setOrderDate(cursor.getString(cursor.getColumnIndexOrThrow("order_date")));
        detail.setNote(cursor.getString(cursor.getColumnIndexOrThrow("note")));
        return detail;
    }
}
