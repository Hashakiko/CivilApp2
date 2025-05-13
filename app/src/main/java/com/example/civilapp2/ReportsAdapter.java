package com.example.civilapp2;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

public class ReportsAdapter extends RecyclerView.Adapter<ReportsAdapter.ReportViewHolder> {

    private Context context;
    private ArrayList<ReportActivity.Report> reports;

    public ReportsAdapter(Context context, ArrayList<ReportActivity.Report> reports) {
        this.context = context;
        this.reports = reports;
    }

    @NonNull
    @Override
    public ReportViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.report_item, parent, false);
        return new ReportViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReportViewHolder holder, int position) {
        ReportActivity.Report report = reports.get(position);
        holder.reportTextView.setText(report.reportText);

        if (report.imageUrl != null && !report.imageUrl.isEmpty()) {
            holder.reportImageView.setVisibility(View.VISIBLE);
            Glide.with(context).load(report.imageUrl).into(holder.reportImageView);
        } else {
            holder.reportImageView.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return reports.size();
    }

    public static class ReportViewHolder extends RecyclerView.ViewHolder {
        TextView reportTextView;
        ImageView reportImageView;

        public ReportViewHolder(View itemView) {
            super(itemView);
            reportTextView = itemView.findViewById(R.id.reportTextView);
            reportImageView = itemView.findViewById(R.id.reportImageView);
        }
    }
}
