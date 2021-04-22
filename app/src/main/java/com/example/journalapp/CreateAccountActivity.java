package com.example.journalapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

import util.JournalApi;

public class CreateAccountActivity extends AppCompatActivity {
    private Button loginButtton;
    private Button createAccountButton;
    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private FirebaseUser currentUser;

    //Create Firestore conection
    //Firestore is a subsection of Firestore

    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    private CollectionReference collectionReference = db.collection("Users");

    private EditText emailEditText;
    private EditText passwordEditText;
    private ProgressBar progressBar;
    private EditText userNameEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_account);

        getSupportActionBar().setElevation(0);



        firebaseAuth = FirebaseAuth.getInstance();


        createAccountButton = findViewById(R.id.id_ACC_createAccountButton);
        progressBar = findViewById(R.id.id_ACC_Progress);
        emailEditText = findViewById(R.id.id_ACC_email);
        passwordEditText = findViewById(R.id.id_ACC_password);
        userNameEditText = findViewById(R.id.id_ACC_username);

        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                currentUser = firebaseAuth.getCurrentUser();
                if(currentUser != null){
                    // user is already logged in
                }
                else{
                    //no user logged in

                }
            }
        };


        createAccountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(!TextUtils.isEmpty(emailEditText.getText().toString())
                        && !TextUtils.isEmpty(passwordEditText.getText().toString())
                        &&!TextUtils.isEmpty(userNameEditText.getText().toString())){

                    String email = emailEditText.getText().toString().trim();
                    String password = passwordEditText.getText().toString().trim();
                    String username = userNameEditText.getText().toString().trim();

                    createUserEmailAccount(email, password, username);
                }

                else {
                    Toast.makeText(CreateAccountActivity.this, "Empty fields not allowed",Toast.LENGTH_SHORT).show();

                }


            }
        });


    }


    private void createUserEmailAccount(String email, String password, String username){
        if(!TextUtils.isEmpty(email) && !TextUtils.isEmpty(password) && !TextUtils.isEmpty(username)){

            progressBar.setVisibility(View.VISIBLE);

            firebaseAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    //task will have our user and some other metadata
                    if(task.isSuccessful()){
                        // we take user to AddJournalActivity
                        currentUser = firebaseAuth.getCurrentUser();

                        String currentUserId = currentUser.getUid();

                        //Create a user Map so we can create a user colection

                        Map<String,String> userObject = new HashMap<>();
                        userObject.put("userId",currentUserId);
                        userObject.put("username",username);

                        //save to our firestore database


                        collectionReference.add(userObject)
                                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                            @Override
                            public void onSuccess(DocumentReference documentReference) {
                                  //here we are gone have a new document
                                documentReference.get()
                                        .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                        //we have created our authenticate user
                                        //userObject to put on our Firebase database
                                        if(task.getResult().exists()){
                                            progressBar.setVisibility(View.INVISIBLE);
                                            String name = task.getResult().getString("username");

                                            JournalApi journalApi = JournalApi.getInstance(); //global api
                                            journalApi.setUserID(currentUserId);
                                            journalApi.setUsername(name);

                                            Intent intent = new Intent(CreateAccountActivity.this, PostJournalActivity.class);
                                            intent.putExtra("username",name);
                                            intent.putExtra("userId", currentUserId);
                                            startActivity(intent);
                                        }
                                        else{
                                            progressBar.setVisibility(View.INVISIBLE);
                                        }


                                    }
                                });


                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {

                            }
                        });


                    }
                    else {
                        //Someting went wrong
                    }

                }
            })
            .addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {

                }
            });


        }
        else{

        }

    }

    @Override
    protected void onStart() {
        super.onStart();

        currentUser = firebaseAuth.getCurrentUser();
        firebaseAuth.addAuthStateListener(authStateListener);

    }


}