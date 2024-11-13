package com.example.midterm;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class CertificateAdapter extends RecyclerView.Adapter<CertificateAdapter.ViewHolder>{
    private static List<Certificate> certificates;
    private static OnItemClickListener onItemClick;

    public CertificateAdapter(List<Certificate> certificates) {
        this.certificates = certificates;
    }

    public void setAdapter(List<Certificate> certificates) {
        this.certificates = certificates;
        notifyDataSetChanged();
    }

    public interface OnItemClickListener {
        void onItemClick(Certificate certificate);
    }

    public void setOnItemClickListener(OnItemClickListener onItemClick) {
        this.onItemClick = onItemClick;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        TextView txtCertificateTitle, txtCertificateDate, txtCertificateDetail;
        CheckBox chkCertificateOption;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            txtCertificateTitle = itemView.findViewById(R.id.txtCertificateTitle);
            txtCertificateDate = itemView.findViewById(R.id.txtCertificateDate);
            txtCertificateDetail = itemView.findViewById(R.id.txtCertificateDetail);
            chkCertificateOption = itemView.findViewById(R.id.chkCertificateOption);

            itemView.setOnClickListener(v -> {
                if (onItemClick != null) {
                    onItemClick.onItemClick(certificates.get(getAdapterPosition()));
                }
            });
        }


    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_certificate, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Certificate certificate = certificates.get(position);

        holder.txtCertificateTitle.setText(certificate.getTitle());
        holder.txtCertificateDate.setText(certificate.getDate());
        holder.txtCertificateDetail.setText(certificate.getDetail());

        holder.chkCertificateOption.setChecked(certificate.isOption());
        holder.chkCertificateOption.setOnCheckedChangeListener((buttonView, isChecked) -> {
            certificate.setOption(isChecked);
        });
    }

    @Override
    public int getItemCount() {
        return certificates.size();
    }

    public List<Certificate> getCheckedCertificates() {
        List<Certificate> checkedCertificates = new ArrayList<>();
        for (Certificate certificate : certificates) {
            if (certificate.isOption()) {
                checkedCertificates.add(certificate);
            }
        }
        return checkedCertificates;
    }
}
