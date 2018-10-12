package com.iitdh.sonusourav.instigo;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.iitdh.sonusourav.instigo.Utils.CommonFunctions;
import com.iitdh.sonusourav.instigo.Utils.PreferenceManager;

import java.util.Objects;

public class UpdatePassword extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener  {

    private EditText newPassword;
    private FirebaseUser firebaseUser;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference rootRef;
    private EditText oldPassword;
    private PreferenceManager updatePref;

    private ProgressBar updateProgressBar;
    private Button updateButton;
    DatabaseReference userRef;
    private String email;

    private ProgressDialog updatePassProgressDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        CommonFunctions.setUser(this);

        findViewById(R.id.include_update_pass).setVisibility(View.VISIBLE);


        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        android.support.v7.widget.Toolbar toolbar = findViewById(R.id.toolbar_main);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(false);
        getSupportActionBar().setHomeButtonEnabled(true);

        DrawerLayout drawerLayout = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        updatePassInit();

        updateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                updateProgressBar.setVisibility(View.VISIBLE);
                updateButton.setEnabled(false);
                String oldPass =oldPassword.getText().toString().trim();
                final String newPass =newPassword.getText().toString().trim();

                assert email != null;
                AuthCredential credential = EmailAuthProvider.getCredential(email,oldPass);


                firebaseUser.reauthenticate(credential)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    firebaseUser.updatePassword(newPass).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {

                                                updatePref.setLoginCredentials(firebaseUser.getEmail(),newPass);


                                                userRef=rootRef.child(encodeUserEmail(email)).getRef();
                                                userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                                        @Override
                                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                            if(dataSnapshot.exists()){
                                                                dataSnapshot.getRef().child("pass").setValue(newPass);
                                                            }
                                                        }

                                                        @Override
                                                        public void onCancelled(@NonNull DatabaseError databaseError) {

                                                            Toast.makeText(getApplicationContext(),"Database update failed",Toast.LENGTH_SHORT).show();
                                                            updateButton.setEnabled(true);
                                                            finish();
                                                        }
                                                    });


                                                Toast.makeText(getApplicationContext(),"Password successfully updated",Toast.LENGTH_SHORT).show();
                                                Log.d("Update Password", "Password updated");
                                                startActivity(new Intent(UpdatePassword.this,HomeActivity.class));
                                            } else {
                                                Toast.makeText(getApplicationContext(),"Password update failed. Try Again!",Toast.LENGTH_SHORT).show();
                                                updateButton.setEnabled(true);
                                                Log.d("Update Password", "Error password not updated");
                                            }
                                        }
                                    });
                                } else {
                                    Toast.makeText(getApplicationContext(),"Error : Wrong old password.",Toast.LENGTH_SHORT).show();
                                    updateButton.setEnabled(true);
                                    Log.d("Update Password", "Error auth failed");
                                }

                            updateProgressBar.setVisibility(View.GONE);}

                        });



            }
        });

    }



    static String encodeUserEmail(String userEmail) {
        return userEmail.replace(".", ",");
    }

    public void showProgressDialog() {

        if (updatePassProgressDialog == null) {
            updatePassProgressDialog = new ProgressDialog(this,R.style.MyAlertDialogStyle);
            updatePassProgressDialog.setMessage(" Connecting to server ....");
            updatePassProgressDialog.setIndeterminate(true);
        }

        updatePassProgressDialog.show();
    }

    public void hideProgressDialog() {
        if (updatePassProgressDialog != null && updatePassProgressDialog.isShowing()) {
            updatePassProgressDialog.dismiss();
        }
    }
    @Override
    protected void onResume(){
        super.onResume();
        updateButton.setEnabled(true);
    }

    private void updatePassInit(){
        updateButton = findViewById(R.id.btnUpdatePassword);
        newPassword = findViewById(R.id.etNewPassword);
        oldPassword = findViewById(R.id.etOldPassword);
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        firebaseDatabase= FirebaseDatabase.getInstance();
        rootRef =firebaseDatabase.getReference().child("Users");
        assert firebaseUser != null;
        email = firebaseUser.getEmail();
        updatePref = new PreferenceManager(this);
        updateProgressBar=findViewById(R.id.update_progress_bar);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        return CommonFunctions.navigationItemSelect(item, this);
    }
}
