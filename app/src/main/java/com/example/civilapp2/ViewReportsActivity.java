package com.example.civilapp2;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class ViewReportsActivity extends AppCompatActivity {

    private RecyclerView reportsRecyclerView;
    private ReportsAdapter adapter;
    private ArrayList<ReportActivity.Report> reportList;
    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_reports);

        reportsRecyclerView = findViewById(R.id.reportsRecyclerView);
        reportsRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        reportList = new ArrayList<>();
        adapter = new ReportsAdapter(this, reportList);
        reportsRecyclerView.setAdapter(adapter);

        firestore = FirebaseFirestore.getInstance();

        // Fetch reports from Firestore
        firestore.collection("reports").get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    reportList.clear();
                    for (DocumentSnapshot snapshot : queryDocumentSnapshots.getDocuments()) {
                        ReportActivity.Report report = snapshot.toObject(ReportActivity.Report.class);
                        reportList.add(report);
                    }
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e ->
                        e.printStackTrace()
                );
    }
}
