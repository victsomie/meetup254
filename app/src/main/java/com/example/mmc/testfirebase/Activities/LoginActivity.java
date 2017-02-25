package com.example.mmc.testfirebase.Activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mmc.testfirebase.Constants;
import com.example.mmc.testfirebase.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class LoginActivity extends AppCompatActivity {



    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference myRef = database.getReference().child(Constants.firebase_reference);

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;



    // private AutoCompleteTextView mEmailView;
    private EditText mEmailView;
    private View mProgressView;
    private View mLoginFormView;

    private EditText mPasswordView, mUsernameView;


    SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAuth = FirebaseAuth.getInstance();
        authenticationSetup();

        setContentView(R.layout.activity_login);

        prefs = getApplication().getSharedPreferences(Constants.shared_preference, 0);
        // Set up the login form.
        mEmailView = (EditText) findViewById(R.id.email);
        mPasswordView = (EditText) findViewById(R.id.password);
        mUsernameView = (EditText) findViewById(R.id.username);

        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    String email = mEmailView.getText().toString();
                    String password = mPasswordView.getText().toString();
                    String username = mUsernameView.getText().toString();

                    email =email.trim();
                    password = password.trim();
                    username = username.trim();

                    attemptLogin(email, password, username);

                    //attemptLogin(mEmailView.getText().toString(), mPasswordView.getText().toString(), mUsernameView.getText().toString());
                    return true;
                }
                return false;
            }
        });

        Button mEmailSignInButton = (Button) findViewById(R.id.email_sign_in_button);
        mEmailSignInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = mEmailView.getText().toString();
                String password = mPasswordView.getText().toString();
                String username = mUsernameView.getText().toString();

                email =email.trim();
                password = password.trim();
                username = username.trim();
                // attemptLogin(mEmailView.getText().toString(), mPasswordView.getText().toString(), mUsernameView.getText().toString());
                attemptLogin(email, password, username);

                prefs.edit().putString(Constants.firebase_reference_user_username,
                        mUsernameView.getText().toString()).commit();
            }
        });

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);
    }

    public void attemptLogin(final String email, String password, final String username) {
        prefs.edit().putString(Constants.firebase_reference_user_username,
                username).commit();
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {


                        if (task.isSuccessful()) {
                            //Saving userdata to firebase
                            HashMap<String, Object> result = new HashMap<>();
                            result.put(Constants.firebase_reference_user_email, email);
                            result.put(Constants.firebase_reference_user_username, username);
                            myRef.push().setValue(result);
                            prefs.edit().putString(Constants.firebase_reference_user_username, username).commit();
                            Toast.makeText(LoginActivity.this, "Sucessfully Created user", Toast.LENGTH_SHORT).show();

                        }


                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {
                            Toast.makeText(LoginActivity.this, "Authentication failed: Couldnt create user", Toast.LENGTH_SHORT).show();


                        }

                        // ...
                    }
                });

        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {

                if (task.isSuccessful()) {
                    Toast.makeText(LoginActivity.this, "Log in Sucessful", Toast.LENGTH_SHORT).show();
                    prefs.edit().putString(Constants.firebase_reference_user_username,
                            username).commit();


                }


                // If sign in fails, display a message to the user. If sign in succeeds
                // the auth state listener will be notified and logic to handle the
                // signed in user can be handled in the listener.
                if (!task.isSuccessful()) {
                    Toast.makeText(getApplicationContext(), "Authentication for login failed", Toast.LENGTH_LONG).show();


                }

            }
        });

    }

    private void authenticationSetup() {

        //Check for authentication state
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    Intent i = new Intent(getApplicationContext(), ViewListVLogs.class);
                    startActivity(i);
                    finish();


                } else {
                    // User is signed out
                }
                // ...
            }
        };
        // ...
    }

    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        finish();
    }
}
