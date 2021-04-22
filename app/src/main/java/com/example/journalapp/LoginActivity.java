package com.example.journalapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import util.JournalApi;


public class LoginActivity extends AppCompatActivity {

    private Button loginButtton;
    private Button createAccountButton;

    private AutoCompleteTextView emailAddress;
    private EditText password;

    private ProgressBar progressBar;

    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference collectionReference = db.collection("Users");





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        getSupportActionBar().setElevation(0);

        firebaseAuth = FirebaseAuth.getInstance();

        progressBar = findViewById(R.id.id_loginProgress);
        loginButtton = findViewById(R.id.id_emailSignInButton);
        createAccountButton = findViewById(R.id.id_createAccountButton);
        emailAddress = findViewById(R.id.id_email);
        password = findViewById(R.id.id_password);

        createAccountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, CreateAccountActivity.class));
            }
        });

        loginButtton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                loginEmailPasswordUser(emailAddress.getText().toString().trim(),
                        password.getText().toString().trim());

            }
        });
    }

    private void loginEmailPasswordUser(String email, String pass) {

        progressBar.setVisibility(View.VISIBLE);

        if(!TextUtils.isEmpty(email)&&!TextUtils.isEmpty(pass)){
            firebaseAuth.signInWithEmailAndPassword(email,pass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    FirebaseUser user = firebaseAuth.getCurrentUser();
                    assert user != null; //make sure user is not null
                    String currentUserId = user.getUid();

                    collectionReference
                            .whereEqualTo("userId",currentUserId)
                            .addSnapshotListener(new EventListener<QuerySnapshot>() {
                                @Override
                                public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException error) {
                                    //event listener to listen to whatever is coming
                                    //querySnapshot is the user coresponding to the current user id

                                    if(error!=null){
                                        return;
                                    }
                                    if(!queryDocumentSnapshots.isEmpty()){
                                        progressBar.setVisibility(View.VISIBLE);
                                        for(QueryDocumentSnapshot snapshot : queryDocumentSnapshots){
                                            JournalApi journalApi = JournalApi.getInstance();
                                            journalApi.setUsername(snapshot.getString("username"));
                                            journalApi.setUserID(snapshot.getString("userId"));

                                            //Go to ListAcctivity
                                            startActivity(new Intent(LoginActivity.this, PostJournalActivity.class));
                                            //finish();
                                        }


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
        else{
            Toast.makeText(LoginActivity.this,"Please anter user and also password",Toast.LENGTH_SHORT).show();
        }
    }
}