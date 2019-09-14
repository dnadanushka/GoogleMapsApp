package com.example.mapsapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class SignUpActivity extends AppCompatActivity {

    private EditText nameTxt;
    private EditText emailTxt;
    private EditText passwordTxt;
    private EditText conPassText;
    FirebaseAuth mFirebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        mFirebaseAuth = FirebaseAuth.getInstance();
        nameTxt = findViewById(R.id.nameTxt);
        emailTxt = findViewById(R.id.emailText);
        passwordTxt = findViewById(R.id.passTxt);
        conPassText = findViewById(R.id.conPass);

        Button btnsign = (Button) findViewById(R.id.btnSign);
        btnsign.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String name = nameTxt.getText().toString();
                String email = emailTxt.getText().toString();
                String pass = passwordTxt.getText().toString();
                String conPass = conPassText.getText().toString();

                if(name.isEmpty() || email.isEmpty() || pass.isEmpty() || conPass.isEmpty()){
                    Toast.makeText(SignUpActivity.this,"Please fill the required fiedls",Toast.LENGTH_SHORT).show();
                }else if(!pass.equals(conPass)){
                    Toast.makeText(SignUpActivity.this,"Password mismatch",Toast.LENGTH_SHORT).show();
                }else{
                    mFirebaseAuth.createUserWithEmailAndPassword(email,pass).addOnCompleteListener(SignUpActivity.this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(!task.isSuccessful()){
                                Toast.makeText(SignUpActivity.this,"An error occured",Toast.LENGTH_SHORT).show();
                            }else{
                                Intent intent = new Intent(SignUpActivity.this, MainActivity.class);
                                startActivity(intent);
                            }
                        }
                    });
                }


            }
        });
    }
}
