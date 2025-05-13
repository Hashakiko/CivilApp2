package com.example.civilapp2;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class PoliceDashboard extends AppCompatActivity {

    Button logoutButton, viewReportsButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_police_dashboard);

        logoutButton = findViewById(R.id.logoutButton);
        viewReportsButton = findViewById(R.id.viewReportsButton);

        // Logout functionality
        logoutButton.setOnClickListener(v -> {
            // Sign out from Firebase
            FirebaseAuth.getInstance().signOut();

            // Redirect to login and clear the back stack
            Intent intent = new Intent(PoliceDashboard.this, Login.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // Clear the stack
            startActivity(intent);
            finish(); // Close the current activity (PoliceDashboard)
        });

        // Navigate to ViewReportsActivity
        viewReportsButton.setOnClickListener(v -> {
            startActivity(new Intent(PoliceDashboard.this, ViewReportsActivity.class));
        });
    }
}
