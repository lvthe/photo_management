package com.example.photo_management.adapters;
import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.photo_management.R;
import com.example.photo_management.models.Customer;

import java.util.List;

public class CustomerAdapter extends RecyclerView.Adapter<CustomerAdapter.CustomerViewHolder> {

    public interface OnCustomerActionListener {
        void onEdit(Customer customer);
        void onDelete(Customer customer);
    }

    private Context context;
    private List<Customer> customerList;
    private OnCustomerActionListener listener;

    public CustomerAdapter(Context context, List<Customer> customerList, OnCustomerActionListener listener) {
        this.context = context;
        this.customerList = customerList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public CustomerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_customer, parent, false);
        return new CustomerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CustomerViewHolder holder, int position) {
        Customer customer = customerList.get(position);

        holder.tvCode.setText("Mã: " + customer.getCode());
        holder.tvName.setText("Tên: " + customer.getName());
        holder.tvPhone.setText("SĐT: " + customer.getPhone());

        holder.btnEdit.setOnClickListener(v -> {
            if (listener != null) listener.onEdit(customer);
        });

        holder.btnDelete.setOnClickListener(v -> {
            new AlertDialog.Builder(context)
                    .setTitle("Xóa khách hàng")
                    .setMessage("Bạn có chắc muốn xóa khách hàng này không?")
                    .setPositiveButton("Xóa", (dialog, which) -> {
                        if (listener != null) listener.onDelete(customer);
                    })
                    .setNegativeButton("Hủy", null)
                    .show();
        });
    }

    @Override
    public int getItemCount() {
        return customerList.size();
    }

    public static class CustomerViewHolder extends RecyclerView.ViewHolder {
        TextView tvCode, tvName, tvPhone;
        Button btnEdit, btnDelete;

        public CustomerViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCode = itemView.findViewById(R.id.tvCustomerCode);
            tvName = itemView.findViewById(R.id.tvCustomerName);
            tvPhone = itemView.findViewById(R.id.tvCustomerPhone);
            btnEdit = itemView.findViewById(R.id.btnEditCustomer);
            btnDelete = itemView.findViewById(R.id.btnDeleteCustomer);
        }
    }
}