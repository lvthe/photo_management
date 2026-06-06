package com.example.photo_management.activities;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.example.photo_management.utils.ValidationUtils;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.photo_management.R;
import com.example.photo_management.adapters.CustomerAdapter;
import com.example.photo_management.database.DatabaseHelper;
import com.example.photo_management.models.Customer;
import com.example.photo_management.utils.NavBar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class CustomerActivity extends AppCompatActivity {

    private RecyclerView rvCustomers;
    private FloatingActionButton fabAddCustomer;
    private View emptyState;

    private DatabaseHelper dbHelper;
    private List<Customer> customerList;
    private CustomerAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer);

        rvCustomers = findViewById(R.id.rvCustomers);
        fabAddCustomer = findViewById(R.id.fabAddCustomer);
        emptyState = findViewById(R.id.emptyState);

        dbHelper = new DatabaseHelper(this);
        customerList = new ArrayList<>();

        rvCustomers.setLayoutManager(new LinearLayoutManager(this));

        adapter = new CustomerAdapter(this, customerList, new CustomerAdapter.OnCustomerActionListener() {
            @Override
            public void onEdit(Customer customer) {
                showCustomerDialog(customer);
            }

            @Override
            public void onDelete(Customer customer) {
                if (dbHelper.customerHasOrders(customer.getId())) {
                    new android.app.AlertDialog.Builder(CustomerActivity.this)
                            .setTitle("Không thể xóa")
                            .setMessage("Khách hàng này đang có đơn hàng. Vui lòng xóa đơn hàng trước.")
                            .setPositiveButton("OK", null)
                            .show();
                    return;
                }
                int result = dbHelper.deleteCustomer(customer.getId());
                if (result > 0) {
                    Toast.makeText(CustomerActivity.this, "Xóa thành công", Toast.LENGTH_SHORT).show();
                    loadCustomers();
                } else {
                    Toast.makeText(CustomerActivity.this, "Xóa thất bại", Toast.LENGTH_SHORT).show();
                }
            }
        });

        rvCustomers.setAdapter(adapter);
        fabAddCustomer.setOnClickListener(v -> showCustomerDialog(null));

        BottomNavigationView bottomNav = findViewById(R.id.bottomNav);
        NavBar.setup(this, bottomNav, R.id.nav_customer);

        loadCustomers();
    }

    private void loadCustomers() {
        customerList.clear();
        customerList.addAll(dbHelper.getAllCustomers());
        adapter.notifyDataSetChanged();

        boolean empty = customerList.isEmpty();
        emptyState.setVisibility(empty ? View.VISIBLE : View.GONE);
        rvCustomers.setVisibility(empty ? View.GONE : View.VISIBLE);
    }

    private void showCustomerDialog(Customer customer) {
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_customer, null);

        EditText edtCode = view.findViewById(R.id.edtCustomerCode);
        EditText edtName = view.findViewById(R.id.edtCustomerName);
        EditText edtPhone = view.findViewById(R.id.edtCustomerPhone);

        boolean isEdit = customer != null;

        if (isEdit) {
            edtCode.setText(customer.getCode());
            edtName.setText(customer.getName());
            edtPhone.setText(customer.getPhone());
        } else {
            edtCode.setText(dbHelper.generateNextCustomerCode());
        }
        edtCode.setEnabled(false);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(isEdit ? "Sửa khách hàng" : "Thêm khách hàng")
                .setView(view)
                .setPositiveButton("Lưu", null)
                .setNegativeButton("Hủy", null)
                .create();

        dialog.show();

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String code = edtCode.getText().toString().trim();
            String name = edtName.getText().toString().trim();
            String phone = edtPhone.getText().toString().trim();

            if (TextUtils.isEmpty(code)) {
                edtCode.setError("Nhập mã khách hàng");
                return;
            }

            if (TextUtils.isEmpty(name)) {
                edtName.setError("Nhập tên khách hàng");
                return;
            }

            if (TextUtils.isEmpty(phone)) {
                edtPhone.setError("Nhập số điện thoại");
                return;
            }

            if (!ValidationUtils.isValidPhone(phone)) {
                edtPhone.setError("Số điện thoại không hợp lệ (9-11 chữ số)");
                return;
            }

            int excludeId = isEdit ? customer.getId() : -1;
            if (dbHelper.isPhoneExists(phone, excludeId)) {
                edtPhone.setError("Số điện thoại đã được sử dụng");
                return;
            }

            if (isEdit) {
                customer.setCode(code);
                customer.setName(name);
                customer.setPhone(phone);

                int result = dbHelper.updateCustomer(customer);
                if (result > 0) {
                    Toast.makeText(this, "Cập nhật thành công", Toast.LENGTH_SHORT).show();
                    loadCustomers();
                    dialog.dismiss();
                } else {
                    Toast.makeText(this, "Cập nhật thất bại", Toast.LENGTH_SHORT).show();
                }
            } else {
                Customer newCustomer = new Customer(code, name, phone);
                long result = dbHelper.insertCustomer(newCustomer);
                if (result > 0) {
                    Toast.makeText(this, "Thêm thành công", Toast.LENGTH_SHORT).show();
                    loadCustomers();
                    dialog.dismiss();
                } else {
                    Toast.makeText(this, "Thêm thất bại hoặc trùng mã", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}