package com.example.civilapp2;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class ReportActivity extends AppCompatActivity {

    private Uri imageUri;
    private ImageView selectedImageView;
    private FirebaseFirestore firestore;
    private FirebaseStorage storage;
    private StorageReference storageReference;

    // ActivityResultLauncher for Gallery
    private final ActivityResultLauncher<Intent> pickImageLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    imageUri = result.getData().getData();
                    try {
                        Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                        if (selectedImageView != null) {
                            selectedImageView.setImageBitmap(bitmap);
                            selectedImageView.setVisibility(View.VISIBLE);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });

    // ActivityResultLauncher for Camera
    private final ActivityResultLauncher<Intent> takePictureLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Bundle extras = result.getData().getExtras();
                    if (extras != null) {
                        Bitmap imageBitmap = (Bitmap) extras.get("data");
                        if (selectedImageView != null) {
                            selectedImageView.setImageBitmap(imageBitmap);
                            selectedImageView.setVisibility(View.VISIBLE);
                        }
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);

        firestore = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference("report_images");

        ImageButton selectImageButton = findViewById(R.id.selectImageButton);
        selectedImageView = findViewById(R.id.selectedImageView);
        EditText reportEditText = findViewById(R.id.reportEditText);
        Button submitReportButton = findViewById(R.id.submitReportButton);

        selectImageButton.setOnClickListener(v -> {
            Intent pickIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            pickImageLauncher.launch(pickIntent);
        });

        submitReportButton.setOnClickListener(v -> {
            String reportText = reportEditText.getText().toString();

            if (TextUtils.isEmpty(reportText) && (imageUri == null && selectedImageView.getDrawable() == null)) {
                Toast.makeText(ReportActivity.this, "Please add a report or an image", Toast.LENGTH_SHORT).show();
                return;
            }

            if (imageUri != null) {
                uploadImageToFirebase(imageUri, reportText);
            } else {
                saveReportToFirestore(reportText, null);
            }
        });
    }

    private void uploadImageToFirebase(Uri imageUri, String reportText) {
        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            byte[] data = baos.toByteArray();

            StorageReference fileReference = storageReference.child(System.currentTimeMillis() + ".jpg");

            fileReference.putBytes(data)
                    .addOnSuccessListener(taskSnapshot -> fileReference.getDownloadUrl()
                            .addOnSuccessListener(uri -> saveReportToFirestore(reportText, uri.toString())))
                    .addOnFailureListener(e -> Toast.makeText(ReportActivity.this, "Image upload failed", Toast.LENGTH_SHORT).show());

        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(ReportActivity.this, "Error uploading image", Toast.LENGTH_SHORT).show();
        }
    }

    private void saveReportToFirestore(String reportText, String imageUrl) {
        String userId = FirebaseAuth.getInstance().getCurrentUser() != null ?
                FirebaseAuth.getInstance().getCurrentUser().getUid() : "anonymous";

        long timestamp = System.currentTimeMillis();
        Report report = new Report(reportText, imageUrl, userId, timestamp);

        firestore.collection("reports")
                .add(report)
                .addOnSuccessListener(documentReference ->
                        Toast.makeText(ReportActivity.this, "Report Submitted", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e ->
                        Toast.makeText(ReportActivity.this, "Failed to submit report", Toast.LENGTH_SHORT).show());
    }

    public static class Report {
        public String reportText;
        public String imageUrl;
        public String userId;
        public long timestamp;

        public Report() {
            // Firestore requires empty constructor
        }

        public Report(String reportText, String imageUrl, String userId, long timestamp) {
            this.reportText = reportText;
            this.imageUrl = imageUrl;
            this.userId = userId;
            this.timestamp = timestamp;
        }
    }
}
