package com.example.journalapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.Date;

import model.Journal;
import util.JournalApi;

public class PostJournalActivity extends AppCompatActivity implements View.OnClickListener {

    private static final int GALLERY_CODE = 1 ;
    private Button saveButton;
    private ProgressBar progressBar;
    private ImageView addPhotoImageViewButton;
    private EditText titleEditText;
    private EditText thoughtsEditText;
    private TextView currentUSerTextView;
    private ImageView imageView;

    private String currentUserId;
    private String currentUsername;

    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private FirebaseUser firebaseUser;

    //Connection to firestore
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    //reference to our store database
    private StorageReference storageReference;

    private CollectionReference collectionReference = db.collection("Journal");
    private Uri imageURI;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_journal);

        storageReference = FirebaseStorage.getInstance().getReference();

        firebaseAuth = FirebaseAuth.getInstance();
        progressBar = findViewById(R.id.id_PostprogressBar);
        titleEditText = findViewById(R.id.id_post_editText_Title);
        thoughtsEditText = findViewById(R.id.id_editText_postThoughts);
        currentUSerTextView = findViewById(R.id.id_postUsername_TextView);

        imageView = findViewById(R.id.id_postImageView);
        saveButton = findViewById(R.id.id_buttonPostSave);
        saveButton.setOnClickListener(this);
        addPhotoImageViewButton = findViewById(R.id.id_postCameraButton);
        addPhotoImageViewButton.setOnClickListener(this);

        progressBar.setVisibility(View.INVISIBLE);

        if(JournalApi.getInstance() != null){
            currentUserId = JournalApi.getInstance().getUserID();
            currentUsername = JournalApi.getInstance().getUsername();

            currentUSerTextView.setText(currentUsername);
        }

        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                firebaseUser = firebaseAuth.getCurrentUser();
                if(firebaseUser!=null){

                }
                else{

                }
            }
        };


//        //pentru a prelua datele din Intent
//        Bundle bundle = getIntent().getExtras();
//        if(bundle!= null){
//            String username = bundle.getString("username");
//            String userId = bundle.getString("ussrID");
//        }

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.id_buttonPostSave:
                //saveJournal
                saveJournal();
                break;
            case R.id.id_postCameraButton:
                //Implicit intent
                Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");// anything that is image in image folder
                startActivityForResult(galleryIntent, GALLERY_CODE);
                //get image form gallery or phone
                break;


        }
    }

    private void saveJournal() {
        String title = titleEditText.getText().toString().trim();
        String thoughts = thoughtsEditText.getText().toString().trim();

        progressBar.setVisibility(View.VISIBLE);

        if(!TextUtils.isEmpty(title) && !TextUtils.isEmpty(thoughts)
        && imageURI != null){

            //each image name is always unique
            StorageReference filepath = storageReference.child("journal_images")
                    .child("my_image" + Timestamp.now().getSeconds());

            filepath.putFile(imageURI)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            progressBar.setVisibility(View.INVISIBLE);

                            filepath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) { //uri the path to convert to URL
                                    //create a Journal Object - model
                                    String imageURL = uri.toString();

                                    Journal journal = new Journal();
                                    journal.setTitle(title);
                                    journal.setThought(thoughts);
                                    journal.setImageUrl(imageURL);

                                    journal.setTimeAdded(new Timestamp((new Date()))); //add current date
                                    journal.setUserName(currentUsername);
                                    journal.setUserId(currentUserId);

                                    //invoke our colectionReference
                                    collectionReference.add(journal).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                        @Override
                                        public void onSuccess(DocumentReference documentReference) {
                                            progressBar.setVisibility(View.INVISIBLE);
                                            startActivity(new Intent(new Intent(PostJournalActivity.this, JournalListAcitivity.class)));
                                            finish();//end post journal activity

                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Toast.makeText(getApplicationContext(),"Something went wrong",Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                    //and save a Journal instance

                                }
                            });



                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressBar.setVisibility(View.INVISIBLE);
                        }
                    });




        }
        else{

        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == GALLERY_CODE && resultCode == RESULT_OK){
            if(data != null){
                imageURI = data.getData();
                imageView.setImageURI(imageURI);

            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        firebaseUser = firebaseAuth.getCurrentUser();
        firebaseAuth.addAuthStateListener(authStateListener);

    }

    @Override
    protected void onStop() {
        super.onStop();
        if(firebaseAuth != null){
            firebaseAuth.removeAuthStateListener(authStateListener);
            //we don't want the listener to listen after we kill the acitvity
        }
    }
}