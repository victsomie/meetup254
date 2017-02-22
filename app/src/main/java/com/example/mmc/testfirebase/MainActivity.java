package com.example.mmc.testfirebase;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    FirebaseUser user;

    // Write a message to the database
    FirebaseDatabase database;
    DatabaseReference myRef;


    //Reference to the elements
    protected EditText passwordEditText;
    protected EditText emailEditText;
    protected Button signUpButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Authenticating first
        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        myRef = database.getReference();

        // myRef = database.getReference();
        isUserAuthenticated(); // the method to tell state of auth

        // Setup the form
        passwordEditText = (EditText) findViewById(R.id.passwordField);
        emailEditText = (EditText) findViewById(R.id.emailField);
        signUpButton = (Button) findViewById(R.id.signupButton);
        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String password = passwordEditText.getText().toString();
                String email = emailEditText.getText().toString();

                password = password.trim();
                email = email.trim();

                //Check field are filled
                if (password.isEmpty() || email.isEmpty()) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                    builder.setMessage("All fields need be filled")
                            .setTitle("Error in form!")
                            .setPositiveButton(android.R.string.ok, null);
                    AlertDialog dialog = builder.create();
                    dialog.show();
                } else {
                    // If fields are filled try sending data now
                    createAndSignUserWithEmail(email, password);

                }
            }
        });




        // Create dummy user and sign the user
        // createAndSignUserWithEmail("jamesjames@yahoo.com", "12345678");

        // Test again the state of auth
        // isUserAuthenticatedSecondTime();



        Toast.makeText(this, myRef.toString(), Toast.LENGTH_SHORT).show();

        myRef.push().setValue("Hello, World! ======");
    }

    private void isUserAuthenticated() {
        // myRef.push().setValue("Inside listener now!!");

        // The listener
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());

                    Toast.makeText(MainActivity.this, "FIRST TIME: User is signed in!", Toast.LENGTH_SHORT).show();
                }
                if (user == null) {
                    // User is signed out
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                    myRef.push().setValue("NO USER at first check...");
                    Toast.makeText(MainActivity.this, "FIRST TIME: This user is not signed in", Toast.LENGTH_SHORT).show();

                }
                // ...
            }
        };
    }

    private void isUserAuthenticatedSecondTime() {
        myRef.push().setValue("SECND TIEM CHECK: Inside listener now!!");

        // The listener
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                    myRef.push().setValue("======SECOND TIME CHECK: GOT USER HERE ========");

                    Toast.makeText(MainActivity.this, "SECOND TIME: User is signed in!", Toast.LENGTH_SHORT).show();
                } else {
                    // User is signed out
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                    myRef.push().setValue("======SECOND TIME: No user Found!! ========");

                    Toast.makeText(MainActivity.this, "SECOND TIME: NOT SIGNED IN", Toast.LENGTH_SHORT).show();

                }
                // ...
            }
        };
    }

    private void signInLikeAnonymous() {
        mAuth.signInAnonymously()
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG, "signInAnonymously:onComplete:" + task.isSuccessful());

                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {
                            Log.w(TAG, "signInAnonymously", task.getException());
                            Toast.makeText(MainActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }

                        if (task.isSuccessful()) {

                            myRef.push().setValue("Hello, World!");
                            Toast.makeText(MainActivity.this, "USER CREATED!!!!", Toast.LENGTH_SHORT).show();
                        }

                        // ...
                    }
                }); // signInAnonymously
    }

    public void createAndSignUserWithEmail(final String theEmail, String thePassword) {
        mAuth.createUserWithEmailAndPassword(theEmail, thePassword)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG, "createUserWithEmail:onComplete:" + task.isSuccessful());

                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {
                            Toast.makeText(MainActivity.this, "CAN'T create USER" + theEmail,
                                    Toast.LENGTH_SHORT).show();
                        }
                        if (task.isSuccessful()) {
                            Toast.makeText(MainActivity.this, "USER: " + theEmail + "CREATED SUCCESSFULLY!",
                                    Toast.LENGTH_SHORT).show();
                        }

                        // ...
                    }
                }); // createUser

        mAuth.signInWithEmailAndPassword(theEmail, thePassword)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG, "signInWithEmail:onComplete:" + task.isSuccessful());

                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {
                            Log.w(TAG, "signInWithEmail", task.getException());
                            Toast.makeText(MainActivity.this, "Couldn't sign in!!...",
                                    Toast.LENGTH_SHORT).show();
                        }

                        if (task.isSuccessful()) {
                            Toast.makeText(MainActivity.this, "USER NOW SIGNED IN!!!!", Toast.LENGTH_SHORT).show();

                            Intent i = new Intent(getApplicationContext(), NextActivity.class);
                            startActivity(i);

                        }
                        // ...
                    }
                });
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
}
