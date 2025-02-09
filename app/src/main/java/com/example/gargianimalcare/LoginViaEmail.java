package com.example.gargianimalcare;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class LoginViaEmail extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;
    private TextView forgotlk;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_login_via_email);

        firebaseAuth = FirebaseAuth.getInstance();
        forgotlk=findViewById(R.id.forgotLink);

        MaterialToolbar toolbar = (MaterialToolbar) findViewById(R.id.loginEmailToolbar);
        setSupportActionBar(toolbar);


        //call registration page
        Button registerEmailBtn = (Button) findViewById(R.id.registerEmailBtn);
        registerEmailBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginViaEmail.this , com.example.gargianimalcare.RegistrationPage.class);
                startActivity(intent);
                finish();
            }
        });

        // Email login
        final EditText emailn = findViewById(R.id.email_login);
        final EditText passwordn = findViewById(R.id.password_login);
        Button proceedLogin = findViewById(R.id.proceedLogin);

        proceedLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String email = emailn.getText().toString().trim();
                String password = passwordn.getText().toString().trim();

                if(TextUtils.isEmpty(email))
                {
                    emailn.setError("Enter Email");
                    emailn.requestFocus();
                    return;
                }
                if(TextUtils.isEmpty(password))
                {
                    passwordn.setError("Enter Password");
                    passwordn.requestFocus();
                    return;
                }


                firebaseAuth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(LoginViaEmail.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    if(firebaseAuth.getCurrentUser().isEmailVerified()) {
                                        Intent intent = new Intent(LoginViaEmail.this, Home.class);
                                        startActivity(intent);
                                        finish();
                                    }
                                    else {

                                        Toast.makeText(LoginViaEmail.this, "Please Verify your email", Toast.LENGTH_SHORT).show();

                                        //send verification email
                                        firebaseAuth.getCurrentUser().sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                Toast.makeText(LoginViaEmail.this, "Verification email send", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                    }

                                } else {
                                    Toast.makeText(LoginViaEmail.this, task.getException().getMessage(), Toast.LENGTH_LONG).show();
                                }
                            }
                        });
            }
        });

        forgotlk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(LoginViaEmail.this,ForgotPasswordActivity.class));
            }
        });

    }
}
