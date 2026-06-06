package com.example.photo_management.activities;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.photo_management.R;
import com.example.photo_management.adapters.ServiceAdapter;
import com.example.photo_management.database.DatabaseHelper;
import com.example.photo_management.models.PrintService;
import com.example.photo_management.utils.NavBar;
import com.example.photo_management.utils.ValidationUtils;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class ServiceActivity extends AppCompatActivity {

    private RecyclerView rvServices;
    private FloatingActionButton fabAddService;
    private View emptyState;

    private DatabaseHelper dbHelper;
    private List<PrintService> serviceList;
    private ServiceAdapter adapter;

    private final String[] printTypes = {"Mau", "Den trang"};
    private final String[] sizes = {"A3", "A4", "A5"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_service);

        rvServices = findViewById(R.id.rvServices);
        fabAddService = findViewById(R.id.fabAddService);
        emptyState = findViewById(R.id.emptyState);

        dbHelper = new DatabaseHelper(this);
        serviceList = new ArrayList<>();

        rvServices.setLayoutManager(new LinearLayoutManager(this));

        adapter = new ServiceAdapter(this, serviceList, new ServiceAdapter.OnServiceActionListener() {
            @Override
            public void onEdit(PrintService service) {
                showServiceDialog(service);
            }

            @Override
            public void onDelete(PrintService service) {
                if (dbHelper.serviceHasOrders(service.getId())) {
                    new android.app.AlertDialog.Builder(ServiceActivity.this)
                            .setTitle("Không thể xóa")
                            .setMessage("Dịch vụ này đang có đơn hàng. Vui lòng xóa đơn hàng trước.")
                            .setPositiveButton("OK", null)
                            .show();
                    return;
                }
                int result = dbHelper.deleteService(service.getId());
                if (result > 0) {
                    Toast.makeText(ServiceActivity.this, "Xóa thành công", Toast.LENGTH_SHORT).show();
                    loadServices();
                } else {
                    Toast.makeText(ServiceActivity.this, "Xóa thất bại", Toast.LENGTH_SHORT).show();
                }
            }
        });

        rvServices.setAdapter(adapter);

        fabAddService.setOnClickListener(v -> showServiceDialog(null));

        NavBar.setup(this, R.id.navService);

        loadServices();
    }

    private void loadServices() {
        serviceList.clear();
        serviceList.addAll(dbHelper.getAllServices());
        adapter.notifyDataSetChanged();

        boolean empty = serviceList.isEmpty();
        emptyState.setVisibility(empty ? View.VISIBLE : View.GONE);
        rvServices.setVisibility(empty ? View.GONE : View.VISIBLE);
    }

    private void showServiceDialog(PrintService service) {
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_service, null);

        EditText edtCode = view.findViewById(R.id.edtServiceCode);
        MaterialButtonToggleGroup togglePrintType = view.findViewById(R.id.togglePrintType);
        MaterialButtonToggleGroup toggleSize = view.findViewById(R.id.toggleSize);
        EditText edtPrice = view.findViewById(R.id.edtPrice);

        boolean isEdit = service != null;

        if (isEdit) {
            edtCode.setText(service.getCode());
            edtPrice.setText(ValidationUtils.formatPlain(service.getPricePerM2()));
            selectToggleByValue(togglePrintType, service.getPrintType());
            selectToggleByValue(toggleSize, service.getSize());
        } else {
            edtCode.setText(dbHelper.generateNextServiceCode());
            togglePrintType.check(R.id.btnTypeColor);
            toggleSize.check(R.id.btnSizeA3);
        }
        edtCode.setEnabled(false);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(isEdit ? "Sửa dịch vụ in" : "Thêm dịch vụ in")
                .setView(view)
                .setPositiveButton("Lưu", null)
                .setNegativeButton("Hủy", null)
                .create();

        dialog.show();

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String code = edtCode.getText().toString().trim();
            String printType = getToggleValue(togglePrintType);
            String size = getToggleValue(toggleSize);
            String priceStr = edtPrice.getText().toString().trim();

            if (TextUtils.isEmpty(code)) {
                edtCode.setError("Nhập mã dịch vụ");
                return;
            }

            if (!ValidationUtils.isPositiveDouble(priceStr)) {
                edtPrice.setError("Giá phải > 0");
                return;
            }

            int excludeId = isEdit ? service.getId() : -1;
            if (dbHelper.isServiceExists(printType, size, excludeId)) {
                Toast.makeText(ServiceActivity.this,
                        "Dịch vụ " + printType + " - " + size + " đã tồn tại",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            double price = ValidationUtils.parseDouble(priceStr);

            if (isEdit) {
                service.setCode(code);
                service.setPrintType(printType);
                service.setSize(size);
                service.setPricePerM2(price);

                int result = dbHelper.updateService(service);
                if (result > 0) {
                    Toast.makeText(this, "Cập nhật thành công", Toast.LENGTH_SHORT).show();
                    loadServices();
                    dialog.dismiss();
                } else {
                    Toast.makeText(this, "Cập nhật thất bại", Toast.LENGTH_SHORT).show();
                }
            } else {
                PrintService newService = new PrintService(code, printType, size, price);
                long result = dbHelper.insertService(newService);

                if (result > 0) {
                    Toast.makeText(this, "Thêm thành công", Toast.LENGTH_SHORT).show();
                    loadServices();
                    dialog.dismiss();
                } else {
                    Toast.makeText(this, "Thêm thất bại hoặc trùng mã", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    /** Lấy giá trị lưu DB của nhóm segmented: ưu tiên android:tag, không có thì lấy text. */
    private String getToggleValue(MaterialButtonToggleGroup group) {
        int checkedId = group.getCheckedButtonId();
        if (checkedId == View.NO_ID) {
            return ((MaterialButton) group.getChildAt(0)).getText().toString();
        }
        MaterialButton button = group.findViewById(checkedId);
        Object tag = button.getTag();
        return tag != null ? tag.toString() : button.getText().toString();
    }

    /** Chọn nút trong nhóm segmented khớp với giá trị (so theo tag rồi tới text). */
    private void selectToggleByValue(MaterialButtonToggleGroup group, String value) {
        for (int i = 0; i < group.getChildCount(); i++) {
            MaterialButton button = (MaterialButton) group.getChildAt(i);
            Object tag = button.getTag();
            String candidate = tag != null ? tag.toString() : button.getText().toString();
            if (candidate.equals(value)) {
                group.check(button.getId());
                return;
            }
        }
    }
}