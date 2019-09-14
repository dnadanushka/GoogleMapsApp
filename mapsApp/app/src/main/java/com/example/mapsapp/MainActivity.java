package com.example.mapsapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.format.Time;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {


    private static final String TAG = "MainActivity";
    private static final int ERROR_DIALOG_REQUEST = 9001;

    private EditText emailTxt;
    private EditText passwordTxt;
    FirebaseAuth mFirebaseAuth;
    FirebaseAuth.AuthStateListener mAuthStateListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mFirebaseAuth = FirebaseAuth.getInstance();
        emailTxt = findViewById(R.id.emailText);
        passwordTxt = findViewById(R.id.passwordTxt);

        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            FirebaseUser mFirebaseUser = mFirebaseAuth.getCurrentUser();


            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if(mFirebaseUser != null){
                    Toast.makeText(MainActivity.this,"you are loged in",Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(MainActivity.this, TimeActivity.class);
                    startActivity(intent);
                }else{
                    Toast.makeText(MainActivity.this,"Please login",Toast.LENGTH_SHORT).show();
                }
            }
        };

        if(isServicesOK())
        {
            init();
        }
    }

    private void init(){
        Button btnMap = (Button) findViewById(R.id.btnLogin);
        btnMap.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: Danushka");

                String email = emailTxt.getText().toString();
                String password = passwordTxt.getText().toString();
                if(email.isEmpty()){
                    emailTxt.setError("Please enter email id");
                    emailTxt.requestFocus();
                }else if(password.isEmpty()){
                    passwordTxt.setError("Please enter the password");
                    passwordTxt.requestFocus();
                }else if(email.isEmpty() && password.isEmpty()){
                    Toast.makeText(MainActivity.this,"Feilds are empty",Toast.LENGTH_SHORT).show();

                }else{
                    mFirebaseAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener(MainActivity.this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(!task.isSuccessful()){
                                Toast.makeText(MainActivity.this,"Login error , please login again",Toast.LENGTH_SHORT).show();
                            }else{
                                Intent intent = new Intent(MainActivity.this, TimeActivity.class);
                                startActivity(intent);
                            }
                        }
                    });
                }



            }
        });

        Button btnSignUp = (Button) findViewById(R.id.btnSignUp);
        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: Danushka");
                Intent intent = new Intent(MainActivity.this, SignUpActivity.class);
                startActivity(intent);
            }
        });
    }

    public boolean isServicesOK(){
        Log.d(TAG, "isServicesOK: checking google services version");

        int avalable = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(MainActivity.this);

        if(avalable == ConnectionResult.SUCCESS){
            //Everything is fine
            Log.d(TAG, "isServicesOK: Gooogle play services working");
            return true;
        }else if(GoogleApiAvailability.getInstance().isUserResolvableError(avalable)){
            Log.d(TAG, "isServicesOK: an error occoured can fixed");
            Dialog dialog = GoogleApiAvailability.getInstance().getErrorDialog(MainActivity.this,avalable,ERROR_DIALOG_REQUEST);

        }else{
            Toast.makeText(this,"You can not make map requests",Toast.LENGTH_SHORT).show();

        }
        return false;
    }

    @Override
    protected void onStart() {
        super.onStart();
        mFirebaseAuth.addAuthStateListener(mAuthStateListener);
    }
}
