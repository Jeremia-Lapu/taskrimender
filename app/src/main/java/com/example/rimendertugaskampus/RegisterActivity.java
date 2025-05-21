package com.example.rimendertugaskampus;

import android.app.ProgressDialog;
import android.content.Intent;  
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    EditText nameInput, emailInput, passwordInput, confirmPasswordInput;
    Button registerButton;

    String URL_REGISTER = "http://10.0.2.2/android/register_user.php";

    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register); // Pastikan layout XML bernama benar

        // Binding komponen dari XML
        nameInput = findViewById(R.id.nameInput);
        emailInput = findViewById(R.id.emailInput);
        passwordInput = findViewById(R.id.passwordInput);
        confirmPasswordInput = findViewById(R.id.confirmPasswordInput);
        registerButton = findViewById(R.id.registerButton);

        // Cek jika ada komponen null
        if (nameInput == null || emailInput == null || passwordInput == null || confirmPasswordInput == null || registerButton == null) {
            Toast.makeText(this, "Salah referensi view dari XML. Cek kembali ID-nya!", Toast.LENGTH_LONG).show();
            return;
        }

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Registering...");

        registerButton.setOnClickListener(v -> {
            String name = nameInput.getText().toString().trim();
            String email = emailInput.getText().toString().trim();
            String password = passwordInput.getText().toString().trim();
            String confirmPassword = confirmPasswordInput.getText().toString().trim();

            Log.d("REGISTER_DEBUG", "Name: " + name);
            Log.d("REGISTER_DEBUG", "Email: " + email);
            Log.d("REGISTER_DEBUG", "Password: " + password);
            Log.d("REGISTER_DEBUG", "Confirm: " + confirmPassword);

            // Validasi input kosong
            if (TextUtils.isEmpty(name)) {
                nameInput.setError("Nama tidak boleh kosong");
                nameInput.requestFocus();
                return;
            }

            if (TextUtils.isEmpty(email)) {
                emailInput.setError("Email tidak boleh kosong");
                emailInput.requestFocus();
                return;
            }

            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                emailInput.setError("Format email tidak valid");
                emailInput.requestFocus();
                return;
            }

            if (TextUtils.isEmpty(password)) {
                passwordInput.setError("Password tidak boleh kosong");
                passwordInput.requestFocus();
                return;
            }

            if (password.length() < 6) {
                passwordInput.setError("Password minimal 6 karakter");
                passwordInput.requestFocus();
                return;
            }

            if (TextUtils.isEmpty(confirmPassword)) {
                confirmPasswordInput.setError("Konfirmasi password tidak boleh kosong");
                confirmPasswordInput.requestFocus();
                return;
            }

            if (!password.equals(confirmPassword)) {
                confirmPasswordInput.setError("Konfirmasi tidak cocok dengan password");
                confirmPasswordInput.requestFocus();
                return;
            }

            // Jika valid, kirim ke server
            registerUser(name, email, password);
        });
    }

    private void registerUser(String name, String email, String password) {
        progressDialog.show();

        JSONObject requestBody = new JSONObject();
        try {
            requestBody.put("nama", name);
            requestBody.put("email", email);
            requestBody.put("password", password);
            requestBody.put("password_confirm", password); // kirim sama karena sudah diverifikasi
        } catch (JSONException e) {
            progressDialog.dismiss();
            Toast.makeText(this, "Gagal membuat data JSON", Toast.LENGTH_SHORT).show();
            return;
        }

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.POST,
                URL_REGISTER,
                requestBody,
                response -> {
                    progressDialog.dismiss();
                    Log.d("REGISTER_RESPONSE", response.toString());

                    boolean success = response.optBoolean("success", false);
                    String message = response.optString("message", "Gagal registrasi");

                    if (success) {
                        Toast.makeText(this, "Registrasi berhasil", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(this, LoginActivity.class));
                        finish();
                    } else {
                        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    progressDialog.dismiss();
                    Log.e("REGISTER_ERROR", error.toString());
                    Toast.makeText(this, "Gagal: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                }
        ) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");
                return headers;
            }
        };

        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(jsonObjectRequest);
    }
}