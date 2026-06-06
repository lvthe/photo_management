package com.example.photo_management.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.photo_management.R;
import com.example.photo_management.adapters.OrderAdapter;
import com.example.photo_management.database.DatabaseHelper;
import com.example.photo_management.models.Customer;
import com.example.photo_management.models.OrderDetail;
import com.example.photo_management.utils.NavBar;
import com.example.photo_management.utils.ValidationUtils;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class SearchOrderActivity extends AppCompatActivity {

    private RecyclerView rvSearchOrders;
    private View cardSummary;
    private View emptyState;
    private TextView tvSearchCount, tvSearchRevenue, tvSearchArea;
    private MaterialAutoCompleteTextView actvCustomer;
    private ChipGroup chipGroupSize, chipGroupType, chipGroupPrice;

    private DatabaseHelper dbHelper;
    private List<OrderDetail> orderList;
    private OrderAdapter adapter;
    private List<Customer> customerList;
    private int selectedCustomerId = -1; // -1 = tất cả khách hàng

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_order);

        rvSearchOrders = findViewById(R.id.rvSearchOrders);
        cardSummary = findViewById(R.id.cardSummary);
        emptyState = findViewById(R.id.emptyState);
        tvSearchCount = findViewById(R.id.tvSearchCount);
        tvSearchRevenue = findViewById(R.id.tvSearchRevenue);
        tvSearchArea = findViewById(R.id.tvSearchArea);
        actvCustomer = findViewById(R.id.actvCustomer);
        chipGroupSize = findViewById(R.id.chipGroupSize);
        chipGroupType = findViewById(R.id.chipGroupType);
        chipGroupPrice = findViewById(R.id.chipGroupPrice);

        dbHelper = new DatabaseHelper(this);
        orderList = new ArrayList<>();

        rvSearchOrders.setLayoutManager(new LinearLayoutManager(this));
        adapter = new OrderAdapter(this, orderList, null, false);
        rvSearchOrders.setAdapter(adapter);

        BottomNavigationView bottomNav = findViewById(R.id.bottomNav);
        NavBar.setup(this, bottomNav, R.id.nav_search);

        setupCustomerDropdown();

        // Đổi bất kỳ bộ lọc nào -> lọc lại ngay
        ChipGroup.OnCheckedStateChangeListener listener = (group, checkedIds) -> applyFilters();
        chipGroupSize.setOnCheckedStateChangeListener(listener);
        chipGroupType.setOnCheckedStateChangeListener(listener);
        chipGroupPrice.setOnCheckedStateChangeListener(listener);

        // Chạy lần đầu với bộ lọc mặc định (Tất cả)
        applyFilters();
    }

    private void setupCustomerDropdown() {
        customerList = dbHelper.getAllCustomers();

        // Phần tử đầu = "Tất cả khách hàng", sau đó tới từng khách.
        List<String> labels = new ArrayList<>();
        labels.add(getString(R.string.filter_all_customers));
        for (Customer c : customerList) {
            labels.add(c.getName());
        }

        ArrayAdapter<String> dropdownAdapter = new ArrayAdapter<>(
                this, R.layout.item_dropdown, labels);
        actvCustomer.setAdapter(dropdownAdapter);
        actvCustomer.setText(getString(R.string.filter_all_customers), false);

        actvCustomer.setOnItemClickListener((parent, view, position, id) -> {
            // position 0 = tất cả; còn lại trỏ vào customerList theo (position - 1)
            selectedCustomerId = (position == 0) ? -1 : customerList.get(position - 1).getId();
            applyFilters();
        });
    }

    private void applyFilters() {
        String size = selectedTag(chipGroupSize);
        String printType = selectedTag(chipGroupType);
        double minPrice = parseDoubleSafe(selectedTag(chipGroupPrice), -1);

        orderList.clear();
        orderList.addAll(dbHelper.searchOrders(selectedCustomerId, size, printType, minPrice));
        adapter.notifyDataSetChanged();

        double totalRevenue = 0;
        double totalArea = 0;
        for (OrderDetail o : orderList) {
            totalRevenue += o.getTotalPrice();
            totalArea += o.getArea();
        }

        tvSearchCount.setText(String.valueOf(orderList.size()));
        tvSearchRevenue.setText(ValidationUtils.formatCurrency(totalRevenue));
        tvSearchArea.setText(String.format(Locale.US, "%.1f m²", totalArea));
        cardSummary.setVisibility(View.VISIBLE);

        if (orderList.isEmpty()) {
            emptyState.setVisibility(View.VISIBLE);
            rvSearchOrders.setVisibility(View.GONE);
        } else {
            emptyState.setVisibility(View.GONE);
            rvSearchOrders.setVisibility(View.VISIBLE);
        }
    }

    /** Lấy android:tag của chip đang chọn trong nhóm (chuỗi rỗng nếu không có). */
    private String selectedTag(ChipGroup group) {
        int checkedId = group.getCheckedChipId();
        if (checkedId == View.NO_ID) {
            return "";
        }
        Chip chip = findViewById(checkedId);
        Object tag = chip.getTag();
        return tag != null ? tag.toString() : "";
    }

    private double parseDoubleSafe(String value, double fallback) {
        try {
            return Double.parseDouble(value);
        } catch (Exception e) {
            return fallback;
        }
    }
}
