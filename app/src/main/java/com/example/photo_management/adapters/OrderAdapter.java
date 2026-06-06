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
import com.example.photo_management.models.OrderDetail;
import com.example.photo_management.utils.ValidationUtils;

import java.util.List;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.OrderViewHolder> {

    public interface OnOrderActionListener {
        void onEdit(OrderDetail orderDetail);
        void onDelete(OrderDetail orderDetail);
    }

    private final Context context;
    private final List<OrderDetail> orderList;
    private final OnOrderActionListener listener;
    private final boolean showActionButtons;

    public OrderAdapter(Context context, List<OrderDetail> orderList, OnOrderActionListener listener, boolean showActionButtons) {
        this.context = context;
        this.orderList = orderList;
        this.listener = listener;
        this.showActionButtons = showActionButtons;
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_order, parent, false);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        OrderDetail order = orderList.get(position);

        holder.tvOrderCode.setText("Mã đơn: " + order.getOrderCode());
        holder.tvCustomerName.setText("Khách hàng: " + order.getCustomerName());
        holder.tvPrintInfo.setText("Dịch vụ: " + order.getPrintType() + " - " + order.getSize());
        holder.tvAreaQty.setText("Diện tích: " + order.getArea() + " | Số lượng: " + order.getQuantity());
        holder.tvTotalPrice.setText("Thành tiền: " + ValidationUtils.formatCurrency(order.getTotalPrice()));
        holder.tvOrderDate.setText("Ngày: " + order.getOrderDate());

        String note = order.getNote();
        if (note == null || note.trim().isEmpty()) {
            holder.tvNote.setVisibility(View.GONE);
        } else {
            holder.tvNote.setVisibility(View.VISIBLE);
            holder.tvNote.setText("Ghi chú: " + note);
        }

        if (showActionButtons) {
            holder.btnEdit.setVisibility(View.VISIBLE);
            holder.btnDelete.setVisibility(View.VISIBLE);
        } else {
            holder.btnEdit.setVisibility(View.GONE);
            holder.btnDelete.setVisibility(View.GONE);
        }

        holder.btnEdit.setOnClickListener(v -> {
            if (listener != null) listener.onEdit(order);
        });

        holder.btnDelete.setOnClickListener(v -> {
            new AlertDialog.Builder(context)
                    .setTitle("Xóa đơn in")
                    .setMessage("Bạn có chắc muốn xóa đơn này không?")
                    .setPositiveButton("Xóa", (dialog, which) -> {
                        if (listener != null) listener.onDelete(order);
                    })
                    .setNegativeButton("Hủy", null)
                    .show();
        });
    }

    @Override
    public int getItemCount() {
        return orderList.size();
    }

    public static class OrderViewHolder extends RecyclerView.ViewHolder {
        TextView tvOrderCode, tvCustomerName, tvPrintInfo, tvAreaQty, tvTotalPrice, tvOrderDate, tvNote;
        Button btnEdit, btnDelete;

        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            tvOrderCode = itemView.findViewById(R.id.tvOrderCode);
            tvCustomerName = itemView.findViewById(R.id.tvCustomerName);
            tvPrintInfo = itemView.findViewById(R.id.tvPrintInfo);
            tvAreaQty = itemView.findViewById(R.id.tvAreaQty);
            tvTotalPrice = itemView.findViewById(R.id.tvOrderTotalPrice);
            tvOrderDate = itemView.findViewById(R.id.tvOrderDate);
            tvNote = itemView.findViewById(R.id.tvOrderNote);
            btnEdit = itemView.findViewById(R.id.btnEditOrder);
            btnDelete = itemView.findViewById(R.id.btnDeleteOrder);
        }
    }
}