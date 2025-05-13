package com.example.civilapp2;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

public class AIChatActivity extends AppCompatActivity {

    EditText userInput;
    Button sendButton;
    TextView chatDisplay;

    // OpenRouter API
    private final String OPENROUTER_API_KEY = "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";
    private final String OPENROUTER_API_URL = "https://openrouter.ai/api/v1/chat/completions";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ai_chat);

        userInput = findViewById(R.id.userInput);
        sendButton = findViewById(R.id.sendButton);
        chatDisplay = findViewById(R.id.chatDisplay);

        sendButton.setOnClickListener(v -> {
            String question = userInput.getText().toString().trim();
            if (!question.isEmpty()) {
                chatDisplay.append("You: " + question + "\n");
                getOpenRouterResponse(question);
                userInput.setText("");
            }
        });
    }

    private void getOpenRouterResponse(String question) {
        new Thread(() -> {
            try {
                URL url = new URL(OPENROUTER_API_URL);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");

                connection.setRequestProperty("Authorization", "Bearer " + OPENROUTER_API_KEY);
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setDoOutput(true);

                // Create JSON body
                JSONObject jsonBody = new JSONObject();
                jsonBody.put("model", "deepseek/deepseek-r1:free");

                JSONArray messages = new JSONArray();
                JSONObject userMessage = new JSONObject();
                userMessage.put("role", "user");
                userMessage.put("content", question);
                messages.put(userMessage);
                jsonBody.put("messages", messages);

                // Send request
                OutputStream os = connection.getOutputStream();
                os.write(jsonBody.toString().getBytes("UTF-8"));
                os.flush();
                os.close();

                // Handle response
                InputStream is = connection.getResponseCode() < 300 ? connection.getInputStream() : connection.getErrorStream();
                Scanner scanner = new Scanner(is);
                StringBuilder response = new StringBuilder();
                while (scanner.hasNext()) {
                    response.append(scanner.nextLine());
                }
                scanner.close();

                JSONObject jsonResponse = new JSONObject(response.toString());

                if (jsonResponse.has("choices")) {
                    String reply = jsonResponse
                            .getJSONArray("choices")
                            .getJSONObject(0)
                            .getJSONObject("message")
                            .getString("content");

                    runOnUiThread(() -> chatDisplay.append("Bot: " + reply.trim() + "\n\n"));
                } else if (jsonResponse.has("error")) {
                    String errorMessage = jsonResponse.getJSONObject("error").getString("message");
                    runOnUiThread(() -> chatDisplay.append("Error: " + errorMessage + "\n"));
                }

            } catch (Exception e) {
                runOnUiThread(() -> {
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    chatDisplay.append("Error: " + e.getMessage() + "\n");
                });
                e.printStackTrace();
            }
        }).start();
    }
}
