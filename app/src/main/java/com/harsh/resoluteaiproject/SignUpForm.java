package com.harsh.resoluteaiproject;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
public class SignUpForm extends AppCompatActivity {
    private EditText editEmail, editPassword, editRePassword, editName;
    private Button btnRegister;
    private FirebaseAuth mAuth;
    String mailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.[a-z]+";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up_form);
        editName = findViewById(R.id.editName);
        editEmail = findViewById(R.id.editEmail);
        editPassword = findViewById(R.id.editPassword);
        editRePassword = findViewById(R.id.editRePassword);
        btnRegister = findViewById(R.id.btnRegister);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isInternetAvailable = NetworkCheck.isNetworkAvailable(getApplicationContext());
                if(isInternetAvailable){
                    String email, password;
                    email = editEmail.getText().toString();
                    password = editPassword.getText().toString();
                    if (!email.matches(mailPattern)) {
                        editEmail.setError("Enter Correct Email");
                        editEmail.requestFocus();
                    }
                    else if (password.isEmpty() || password.length() < 8) {
                        editPassword.setError("Enter Proper password");
                    }
                    if(
                            editPassword.getText().toString().equals(editRePassword.getText().toString())
                                    && email.matches(mailPattern) && !(password.isEmpty()) && !(password.length() < 8)
                    ){
                        mAuth.createUserWithEmailAndPassword(email, password)
                                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                                    @Override
                                    public void onSuccess(AuthResult authResult) {
//                                     Sign in success, update UI with the signed-in user's information
                                        Log.d(TAG, "createUserWithEmail:success");
                                        FirebaseUser user = mAuth.getCurrentUser();
                                        updateUI(user);
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
//                                     If sign in fails, display a message to the user.
                                        Log.w(TAG, "createUserWithEmail:failure", e.fillInStackTrace());
                                        updateUI(null);
                                    }
                                });
                    }
                    else{
                        editRePassword.setError("Password does not match");
                    }
                }
                else{
                    Toast.makeText(SignUpForm.this, "No Internet Connection", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    private void updateUI(FirebaseUser user) {
        if(user==null){
            Toast.makeText(this, "Authentication failed", Toast.LENGTH_SHORT).show();
        }else{
            Toast.makeText(this, "Registration Successful", Toast.LENGTH_SHORT).show();
            Intent i = new Intent(getApplicationContext(),WelcomePage.class);
            i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(i);
            finish();
        }
    }
}