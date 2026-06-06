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
import com.example.photo_management.models.PrintService;
import com.example.photo_management.utils.ValidationUtils;

import java.util.List;

public class ServiceAdapter extends RecyclerView.Adapter<ServiceAdapter.ServiceViewHolder> {

    public interface OnServiceActionListener {
        void onEdit(PrintService service);
        void onDelete(PrintService service);
    }

    private final Context context;
    private final List<PrintService> serviceList;
    private final OnServiceActionListener listener;

    public ServiceAdapter(Context context, List<PrintService> serviceList, OnServiceActionListener listener) {
        this.context = context;
        this.serviceList = serviceList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ServiceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_service, parent, false);
        return new ServiceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ServiceViewHolder holder, int position) {
        PrintService service = serviceList.get(position);

        holder.tvCode.setText("Mã " + service.getCode());
        holder.tvPrintType.setText("Loại in: " + service.getPrintType());
        holder.tvSize.setText("Kích thước: " + service.getSize());
        holder.tvPrice.setText("Giá/m²: " + ValidationUtils.formatCurrency(service.getPricePerM2()));

        holder.btnEdit.setOnClickListener(v -> {
            if (listener != null) listener.onEdit(service);
        });

        holder.btnDelete.setOnClickListener(v -> {
            new AlertDialog.Builder(context)
                    .setTitle("Xóa dịch vụ")
                    .setMessage("Bạn có chắc muốn xóa dịch vụ này không?")
                    .setPositiveButton("Xóa", (dialog, which) -> {
                        if (listener != null) listener.onDelete(service);
                    })
                    .setNegativeButton("Hủy", null)
                    .show();
        });
    }

    @Override
    public int getItemCount() {
        return serviceList.size();
    }

    public static class ServiceViewHolder extends RecyclerView.ViewHolder {
        TextView tvCode, tvPrintType, tvSize, tvPrice;
        Button btnEdit, btnDelete;

        public ServiceViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCode = itemView.findViewById(R.id.tvServiceCode);
            tvPrintType = itemView.findViewById(R.id.tvPrintType);
            tvSize = itemView.findViewById(R.id.tvSize);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            btnEdit = itemView.findViewById(R.id.btnEditService);
            btnDelete = itemView.findViewById(R.id.btnDeleteService);
        }
    }
}
