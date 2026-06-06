package com.example.photo_management.activities;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.photo_management.R;
import com.example.photo_management.adapters.OrderAdapter;
import com.example.photo_management.database.DatabaseHelper;
import com.example.photo_management.models.Customer;
import com.example.photo_management.models.OrderDetail;
import com.example.photo_management.models.PrintOrder;
import com.example.photo_management.models.PrintService;
import com.example.photo_management.utils.NavBar;
import com.example.photo_management.utils.ValidationUtils;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class OrderActivity extends AppCompatActivity {

    private RecyclerView rvOrders;
    private FloatingActionButton fabAddOrder;
    private View emptyState;

    private DatabaseHelper dbHelper;
    private List<OrderDetail> orderList;
    private OrderAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order);

        rvOrders = findViewById(R.id.rvOrders);
        fabAddOrder = findViewById(R.id.fabAddOrder);
        emptyState = findViewById(R.id.emptyState);

        dbHelper = new DatabaseHelper(this);
        orderList = new ArrayList<>();

        rvOrders.setLayoutManager(new LinearLayoutManager(this));

        adapter = new OrderAdapter(this, orderList, new OrderAdapter.OnOrderActionListener() {
            @Override
            public void onEdit(OrderDetail orderDetail) {
                PrintOrder order = dbHelper.getOrderById(orderDetail.getId());
                if (order != null) {
                    showOrderDialog(order);
                }
            }

            @Override
            public void onDelete(OrderDetail orderDetail) {
                int result = dbHelper.deleteOrder(orderDetail.getId());
                if (result > 0) {
                    Toast.makeText(OrderActivity.this, "Xóa thành công", Toast.LENGTH_SHORT).show();
                    loadOrders();
                } else {
                    Toast.makeText(OrderActivity.this, "Xóa thất bại", Toast.LENGTH_SHORT).show();
                }
            }
        }, true);

        rvOrders.setAdapter(adapter);

        fabAddOrder.setOnClickListener(v -> {
            if (dbHelper.getAllCustomers().isEmpty()) {
                Toast.makeText(this, "Hãy thêm khách hàng trước", Toast.LENGTH_SHORT).show();
                return;
            }
            if (dbHelper.getAllServices().isEmpty()) {
                Toast.makeText(this, "Hãy thêm dịch vụ in trước", Toast.LENGTH_SHORT).show();
                return;
            }
            showOrderDialog(null);
        });

        NavBar.setup(this, R.id.navOrder);

        loadOrders();
    }

    private void loadOrders() {
        orderList.clear();
        orderList.addAll(dbHelper.getAllOrderDetails());
        adapter.notifyDataSetChanged();

        boolean empty = orderList.isEmpty();
        emptyState.setVisibility(empty ? View.VISIBLE : View.GONE);
        rvOrders.setVisibility(empty ? View.GONE : View.VISIBLE);
    }

    private void showOrderDialog(PrintOrder order) {
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_order, null);

        EditText edtCode = view.findViewById(R.id.edtOrderCode);
        Spinner spCustomer = view.findViewById(R.id.spCustomer);
        Spinner spService = view.findViewById(R.id.spService);
        EditText edtArea = view.findViewById(R.id.edtArea);
        EditText edtQuantity = view.findViewById(R.id.edtQuantity);
        TextView tvTotalPrice = view.findViewById(R.id.tvTotalPrice);
        EditText edtDate = view.findViewById(R.id.edtOrderDate);
        EditText edtNote = view.findViewById(R.id.edtOrderNote);

        List<Customer> customers = dbHelper.getAllCustomers();
        List<PrintService> services = dbHelper.getAllServices();

        ArrayAdapter<Customer> customerAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, customers
        );
        customerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spCustomer.setAdapter(customerAdapter);

        ArrayAdapter<PrintService> serviceAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, services
        );
        serviceAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spService.setAdapter(serviceAdapter);

        boolean isEdit = order != null;

        if (!isEdit) {
            edtCode.setText(dbHelper.generateNextOrderCode());
        } else {
            edtCode.setText(order.getCode());
        }
        edtCode.setEnabled(false);

        if (isEdit) {
            edtArea.setText(ValidationUtils.formatPlain(order.getArea()));
            edtQuantity.setText(String.valueOf(order.getQuantity()));
            tvTotalPrice.setText(ValidationUtils.formatCurrency(order.getTotalPrice()));
            tvTotalPrice.setTag(order.getTotalPrice());
            edtDate.setText(order.getOrderDate());
            edtNote.setText(order.getNote());

            for (int i = 0; i < customers.size(); i++) {
                if (customers.get(i).getId() == order.getCustomerId()) {
                    spCustomer.setSelection(i);
                    break;
                }
            }

            for (int i = 0; i < services.size(); i++) {
                if (services.get(i).getId() == order.getServiceId()) {
                    spService.setSelection(i);
                    break;
                }
            }
        } else {
            String currentDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
            edtDate.setText(currentDate);
            tvTotalPrice.setText(ValidationUtils.formatCurrency(0));
            tvTotalPrice.setTag(0.0);
        }

        // Bấm vào ô ngày -> mở lịch chọn (không cho gõ tay)
        edtDate.setOnClickListener(v -> showDatePicker(edtDate));

        TextWatcher watcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                calculateTotal(spService, edtArea, edtQuantity, tvTotalPrice);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        };

        edtArea.addTextChangedListener(watcher);
        edtQuantity.addTextChangedListener(watcher);

        spService.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view1, int position, long id) {
                calculateTotal(spService, edtArea, edtQuantity, tvTotalPrice);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(isEdit ? "Sửa đơn in" : "Thêm đơn in")
                .setView(view)
                .setPositiveButton("Lưu", null)
                .setNegativeButton("Hủy", null)
                .create();

        dialog.show();

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String code = edtCode.getText().toString().trim();
            String areaStr = edtArea.getText().toString().trim();
            String quantityStr = edtQuantity.getText().toString().trim();
            String orderDate = edtDate.getText().toString().trim();
            String note = edtNote.getText().toString().trim();

            if (TextUtils.isEmpty(code)) {
                edtCode.setError("Nhập mã đơn");
                return;
            }

            if (!ValidationUtils.isPositiveDouble(areaStr)) {
                edtArea.setError("Diện tích phải > 0");
                return;
            }

            if (!ValidationUtils.isPositiveInt(quantityStr)) {
                edtQuantity.setError("Số lượng phải > 0");
                return;
            }

            Customer selectedCustomer = (Customer) spCustomer.getSelectedItem();
            PrintService selectedService = (PrintService) spService.getSelectedItem();

            double area = ValidationUtils.parseDouble(areaStr);
            int quantity = ValidationUtils.parseInt(quantityStr);
            Object tag = tvTotalPrice.getTag();
            double totalPrice = (tag instanceof Double) ? (Double) tag : 0.0;

            if (isEdit) {
                order.setCode(code);
                order.setCustomerId(selectedCustomer.getId());
                order.setServiceId(selectedService.getId());
                order.setArea(area);
                order.setQuantity(quantity);
                order.setTotalPrice(totalPrice);
                order.setOrderDate(orderDate);
                order.setNote(note);

                int result = dbHelper.updateOrder(order);
                if (result > 0) {
                    Toast.makeText(this, "Cập nhật thành công", Toast.LENGTH_SHORT).show();
                    loadOrders();
                    dialog.dismiss();
                } else {
                    Toast.makeText(this, "Cập nhật thất bại", Toast.LENGTH_SHORT).show();
                }
            } else {
                PrintOrder newOrder = new PrintOrder(
                        code,
                        selectedCustomer.getId(),
                        selectedService.getId(),
                        area,
                        quantity,
                        totalPrice,
                        orderDate,
                        note
                );

                long result = dbHelper.insertOrder(newOrder);
                if (result > 0) {
                    Toast.makeText(this, "Thêm thành công", Toast.LENGTH_SHORT).show();
                    loadOrders();
                    dialog.dismiss();
                } else {
                    Toast.makeText(this, "Thêm thất bại hoặc trùng mã", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void showDatePicker(EditText edtDate) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));

        long selection;
        try {
            Date parsed = sdf.parse(edtDate.getText().toString().trim());
            selection = parsed != null ? parsed.getTime() : MaterialDatePicker.todayInUtcMilliseconds();
        } catch (Exception e) {
            selection = MaterialDatePicker.todayInUtcMilliseconds();
        }

        MaterialDatePicker<Long> picker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Chọn ngày đặt")
                .setSelection(selection)
                .build();
        picker.addOnPositiveButtonClickListener(sel -> edtDate.setText(sdf.format(new Date(sel))));
        picker.show(getSupportFragmentManager(), "order_date_picker");
    }

    private void calculateTotal(Spinner spService, EditText edtArea, EditText edtQuantity, TextView tvTotalPrice) {
        PrintService selectedService = (PrintService) spService.getSelectedItem();
        if (selectedService == null) {
            tvTotalPrice.setText(ValidationUtils.formatCurrency(0));
            return;
        }

        double pricePerM2 = selectedService.getPricePerM2();
        double area = ValidationUtils.parseDouble(edtArea.getText().toString().trim());
        int quantity = ValidationUtils.parseInt(edtQuantity.getText().toString().trim());

        double total = pricePerM2 * area * quantity;
        tvTotalPrice.setText(ValidationUtils.formatCurrency(total));
        tvTotalPrice.setTag(total);
    }
}