package com.example.prm392_finalproject_productsaleapp_group2.voucher;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.prm392_finalproject_productsaleapp_group2.R;
import com.example.prm392_finalproject_productsaleapp_group2.models.UserVoucher;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class VoucherAdapter extends RecyclerView.Adapter<VoucherAdapter.VoucherViewHolder> {

    private List<UserVoucher> vouchers;
    private OnVoucherClickListener listener;

    public interface OnVoucherClickListener {
        void onApplyClick(UserVoucher voucher);
    }

    public VoucherAdapter(List<UserVoucher> vouchers, OnVoucherClickListener listener) {
        this.vouchers = vouchers;
        this.listener = listener;
    }

    @NonNull
    @Override
    public VoucherViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_voucher, parent, false);
        return new VoucherViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VoucherViewHolder holder, int position) {
        UserVoucher userVoucher = vouchers.get(position);
        holder.bind(userVoucher, listener);
    }

    @Override
    public int getItemCount() {
        return vouchers.size();
    }

    public void updateData(List<UserVoucher> newVouchers) {
        this.vouchers = newVouchers;
        notifyDataSetChanged();
    }

    static class VoucherViewHolder extends RecyclerView.ViewHolder {
        ImageView imgVoucherIcon;
        TextView tvVoucherCode;
        TextView tvVoucherDescription;
        TextView tvExpiryDate;
        TextView tvCode;
        TextView btnApply;

        public VoucherViewHolder(@NonNull View itemView) {
            super(itemView);
            imgVoucherIcon = itemView.findViewById(R.id.img_voucher_icon);
            tvVoucherCode = itemView.findViewById(R.id.tv_voucher_code);
            tvVoucherDescription = itemView.findViewById(R.id.tv_voucher_description);
            tvExpiryDate = itemView.findViewById(R.id.tv_expiry_date);
            tvCode = itemView.findViewById(R.id.tv_code);
            btnApply = itemView.findViewById(R.id.btn_apply);
        }

        public void bind(UserVoucher userVoucher, OnVoucherClickListener listener) {
            if (userVoucher.getVoucher() != null) {
                // Set voucher code
                tvVoucherCode.setText(userVoucher.getVoucher().getCode());
                tvCode.setText(userVoucher.getVoucher().getCode());
                
                // Set description
                tvVoucherDescription.setText(userVoucher.getVoucher().getDescription());
                
                // Format and set expiry date
                String endDate = userVoucher.getVoucher().getEndDate();
                if (endDate != null && !endDate.isEmpty()) {
                    try {
                        SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
                        SimpleDateFormat outputFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                        Date date = inputFormat.parse(endDate);
                        if (date != null) {
                            tvExpiryDate.setText(outputFormat.format(date));
                        }
                    } catch (Exception e) {
                        tvExpiryDate.setText(endDate.substring(0, 10));
                    }
                }
                
                // Set voucher icon based on discount type
                if (userVoucher.getVoucher().getDiscountAmount() != null && userVoucher.getVoucher().getDiscountAmount() > 0) {
                    // Use voucher-vnd drawable (with hyphen, Android will convert to underscore)
                    imgVoucherIcon.setImageResource(itemView.getContext().getResources()
                            .getIdentifier("voucher_vnd", "drawable", itemView.getContext().getPackageName()));
                } else if (userVoucher.getVoucher().getDiscountPercent() != null && userVoucher.getVoucher().getDiscountPercent() > 0) {
                    imgVoucherIcon.setImageResource(R.drawable.voucher);
                }
                
                // Set click listener
                btnApply.setOnClickListener(v -> {
                    if (listener != null) {
                        listener.onApplyClick(userVoucher);
                    }
                });
            }
        }
    }
}

