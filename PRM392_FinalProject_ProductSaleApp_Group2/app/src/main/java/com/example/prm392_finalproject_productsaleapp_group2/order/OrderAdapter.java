package com.example.prm392_finalproject_productsaleapp_group2.order;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.prm392_finalproject_productsaleapp_group2.R;
import com.example.prm392_finalproject_productsaleapp_group2.models.Order;
import com.example.prm392_finalproject_productsaleapp_group2.models.Product;
import com.example.prm392_finalproject_productsaleapp_group2.services.ImageService;

import java.util.ArrayList;
import java.util.List;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.OrderViewHolder> {
    private List<Order> orders;
    private OnOrderClickListener onOrderClickListener;

    public interface OnOrderClickListener {
        void onOrderClick(Order order);
    }

    public OrderAdapter() {
        this.orders = new ArrayList<>();
    }

    public void setOrders(List<Order> orders) {
        this.orders = orders;
        notifyDataSetChanged();
    }

    public void setOnOrderClickListener(OnOrderClickListener listener) {
        this.onOrderClickListener = listener;
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_order, parent, false);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        Order order = orders.get(position);
        holder.bind(order);
    }

    @Override
    public int getItemCount() {
        return orders.size();
    }

    class OrderViewHolder extends RecyclerView.ViewHolder {
        private TextView tvOrderId;
        private TextView tvOrderDate;
        private TextView tvStatus;
        private TextView tvTotalAmount;
        private TextView tvPaymentMethod;
        private TextView tvBillingAddress;

        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            tvOrderId = itemView.findViewById(R.id.tv_order_id);
            tvOrderDate = itemView.findViewById(R.id.tv_order_date);
            tvStatus = itemView.findViewById(R.id.tv_status);
            tvTotalAmount = itemView.findViewById(R.id.tv_total_amount);
            tvPaymentMethod = itemView.findViewById(R.id.tv_payment_method);
            tvBillingAddress = itemView.findViewById(R.id.tv_billing_address);

            itemView.setOnClickListener(v -> {
                if (onOrderClickListener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        onOrderClickListener.onOrderClick(orders.get(position));
                    }
                }
            });
        }

        public void bind(Order order) {
            // Set order ID
            tvOrderId.setText("Order #" + order.getOrderId());
            
            // Set order date
            tvOrderDate.setText(order.getFormattedOrderDate());
            
            // Set total amount
            tvTotalAmount.setText(order.getFormattedTotalAmount());
            
            // Set payment method
            tvPaymentMethod.setText(order.getPaymentMethod() != null ? order.getPaymentMethod() : "N/A");
            
            // Set billing address
            tvBillingAddress.setText(order.getBillingAddress() != null ? order.getBillingAddress() : "N/A");
            
            // Set status
            tvStatus.setText(order.getStatusDisplayText());
            String resourceName = order.getStatusBackgroundResource();
            int resourceId = itemView.getContext().getResources().getIdentifier(resourceName, "drawable", itemView.getContext().getPackageName());
            tvStatus.setBackgroundResource(resourceId);
        }
    }
}
