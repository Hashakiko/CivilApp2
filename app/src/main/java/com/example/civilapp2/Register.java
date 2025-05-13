package com.example.civilapp2;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class Register extends AppCompatActivity {
    TextInputEditText editTextEmail, editTextPassword;
    Button buttonReg;
    FirebaseAuth mAuth;
    ProgressBar progressBar;
    TextView textView;
    RadioGroup radioGroup;
    private final Random random = new Random();

    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            startActivity(new Intent(getApplicationContext(), MainActivity.class));
            finish();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // --- your existing initialization ---
        mAuth = FirebaseAuth.getInstance();
        editTextEmail     = findViewById(R.id.email);
        editTextPassword  = findViewById(R.id.password);
        buttonReg         = findViewById(R.id.btn_register);
        progressBar       = findViewById(R.id.progressBar);
        textView          = findViewById(R.id.LoginNow);
        radioGroup        = findViewById(R.id.radioGroupRole);
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Login link stays the same
        textView.setOnClickListener(v -> {
            startActivity(new Intent(getApplicationContext(), Login.class));
            finish();
        });

        // 1) Touch listener for floating when invalid:
        buttonReg.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                // validate right here
                boolean invalid = false;
                String email = editTextEmail.getText().toString().trim();
                String pass  = editTextPassword.getText().toString().trim();
                int selectedId = radioGroup.getCheckedRadioButtonId();

                if (TextUtils.isEmpty(email))   { Toast.makeText(this, "Enter email",     Toast.LENGTH_SHORT).show(); invalid = true; }
                if (TextUtils.isEmpty(pass))    { Toast.makeText(this, "Enter password",  Toast.LENGTH_SHORT).show(); invalid = true; }
                if (selectedId == -1)           { Toast.makeText(this, "Select a role",  Toast.LENGTH_SHORT).show(); invalid = true; }

                if (invalid) {
                    moveButtonToRandomPosition();
                    return true;  // consume — don’t trigger click
                }
            }
            return false;  // allow click()
        });

        // 2) Click listener runs only when all valid (touch returned false):
        buttonReg.setOnClickListener(view -> {
            progressBar.setVisibility(View.VISIBLE);
            String email = editTextEmail.getText().toString().trim();
            String password = editTextPassword.getText().toString().trim();
            int selectedId = radioGroup.getCheckedRadioButtonId();

            // (Re-check just in case)
            if (TextUtils.isEmpty(email)) {
                Toast.makeText(this, "Enter email", Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.GONE);
                return;
            }
            if (TextUtils.isEmpty(password)) {
                Toast.makeText(this, "Enter password", Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.GONE);
                return;
            }
            if (selectedId == -1) {
                Toast.makeText(this, "Select a role", Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.GONE);
                return;
            }

            // Proceed with Firebase registration
            RadioButton selectedRadio = findViewById(selectedId);
            String role = selectedRadio.getText().toString();

            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        progressBar.setVisibility(View.GONE);
                        if (task.isSuccessful() && mAuth.getCurrentUser() != null) {
                            Map<String, Object> userData = new HashMap<>();
                            userData.put("email", email);
                            userData.put("role",  role);

                            db.collection("users")
                                    .document(mAuth.getCurrentUser().getUid())
                                    .set(userData)
                                    .addOnSuccessListener(aVoid -> {
                                        Toast.makeText(this, "Account created.", Toast.LENGTH_SHORT).show();
                                        startActivity(new Intent(getApplicationContext(), Login.class));
                                        finish();
                                    })
                                    .addOnFailureListener(e -> Toast.makeText(this, "Error saving data.", Toast.LENGTH_SHORT).show());
                        } else {
                            Toast.makeText(this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                        }
                    });
        });
    }

    private void moveButtonToRandomPosition() {
        buttonReg.post(() -> {
            ViewGroup parent = (ViewGroup) buttonReg.getParent();
            int maxX = parent.getWidth()  - buttonReg.getWidth();
            int maxY = parent.getHeight() - buttonReg.getHeight();
            if (maxX <= 0 || maxY <= 0) return;
            int x = random.nextInt(maxX);
            int y = random.nextInt(maxY);
            buttonReg.animate().x(x).y(y).setDuration(300).start();
        });
    }
}
