package com.example.gargianimalcare;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskExecutors;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.concurrent.TimeUnit;

public class RegistrationOTPPage extends AppCompatActivity {

    String verificationId;
    FirebaseAuth mAuth;
    EditText registerPhoneOTPEditText;
    AuthCredential eCredential;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference reference;
    FirebaseUser user;
    Button resendOTP;
    private UserHelperClass helperClass;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration_otppage);

        mAuth = FirebaseAuth.getInstance();

        firebaseDatabase = FirebaseDatabase.getInstance("https://gargi-animal-care-default-rtdb.firebaseio.com/");
        reference = firebaseDatabase.getReference("users");

        registerPhoneOTPEditText = findViewById(R.id.registerPhoneOTPEditText);
        resendOTP=findViewById(R.id.generateNewOTPBtn);



        String username = getIntent().getStringExtra("username");
        String businessname  =getIntent().getStringExtra("businessname");
        String phoneNumber = getIntent().getStringExtra("phonenumber");
        String email = getIntent().getStringExtra("email");
        String password  = getIntent().getStringExtra("password");
        int flagCode  = getIntent().getIntExtra("flag",0);

        helperClass = new UserHelperClass(username,businessname,phoneNumber,email,password,flagCode);

        eCredential = EmailAuthProvider.getCredential(email,password);

        sendVerificationCode(phoneNumber);

        resendOTP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendVerificationCode(phoneNumber);
            }
        });

        findViewById(R.id.submitOTPBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String code = registerPhoneOTPEditText.getText().toString().trim();

                if(code.isEmpty() || code.length() < 6)
                {
                    registerPhoneOTPEditText.setError("Enter OTP Code...");
                    registerPhoneOTPEditText.requestFocus();
                    return;
                }

                verifyCode(code);
            }
        });
    }

    private void verifyCode(String code){
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId,code);
        signInWithCredentials(credential);
    }

    private void signInWithCredentials(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful())
                        {

                            mAuth.getCurrentUser().linkWithCredential(eCredential)
                                    .addOnCompleteListener(RegistrationOTPPage.this, new OnCompleteListener<AuthResult>() {
                                        @Override
                                        public void onComplete(@NonNull Task<AuthResult> task) {
                                            if (task.isSuccessful()) {

                                                Toast.makeText(RegistrationOTPPage.this, "Linking successful", Toast.LENGTH_LONG).show();
                                                user = FirebaseAuth.getInstance().getCurrentUser();
                                                reference.child(user.getUid()).setValue(helperClass);

                                                Intent intent = new Intent(RegistrationOTPPage.this,LoginViaEmail.class);
                                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

                                                startActivity(intent);
                                                finish();

                                            } else {
                                                Toast.makeText(RegistrationOTPPage.this, task.getException().getMessage(), Toast.LENGTH_LONG).show();
                                            }

                                        }
                                    });

                        }
                        else {
                            Toast.makeText(RegistrationOTPPage.this, task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                });


    }


    private void sendVerificationCode(String number)
    {
        PhoneAuthOptions options =
                PhoneAuthOptions.newBuilder(mAuth)
                        .setPhoneNumber(number)       // Phone number to verify
                        .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
                        .setActivity(this)                 // Activity (for callback binding)
                        .setCallbacks(mCallBack)          // OnVerificationStateChangedCallbacks
                        .build();
        PhoneAuthProvider.verifyPhoneNumber(options);

    }

    private PhoneAuthProvider.OnVerificationStateChangedCallbacks
            mCallBack = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

        @Override
        public void onCodeSent(String s, PhoneAuthProvider.ForceResendingToken forceResendingToken) {
            super.onCodeSent(s, forceResendingToken);

            verificationId = s;
        }

        @Override
        public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {
                String code = phoneAuthCredential.getSmsCode();

                if(code != null) {
                    registerPhoneOTPEditText.setText(code);
                    verifyCode(code);
                }
                if(code == null)
                {
                    signInWithCredentials(phoneAuthCredential);
                }
        }

        @Override
        public void onVerificationFailed(FirebaseException e) {
            Toast.makeText(RegistrationOTPPage.this, e.getMessage(), Toast.LENGTH_LONG).show();
        }
    };
}