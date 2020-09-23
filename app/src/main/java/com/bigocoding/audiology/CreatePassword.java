package com.bigocoding.audiology;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class CreatePassword extends AppCompatActivity {
    private static final String userId = "usr_5abcb05344d141e398f1489b159d5ae5";
    DatabaseReference mDatabaseRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_password);
        mDatabaseRef = FirebaseDatabase.getInstance().getReference(userId + "/password");
    }
    public void passwordsignup_button(View view) {
        EditText password = (EditText) findViewById(R.id.signup_password);
        EditText confirm_password = (EditText) findViewById(R.id.signup_confirm_password) ;
        if (password.getText().toString().equals(confirm_password.getText().toString())){
            Toast.makeText(this, "Sucessful", Toast.LENGTH_SHORT).show() ;

            savePasswordToFirebase(password.getText().toString());
            Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        }
        else
            Toast.makeText(this, "Password and Confirm password are not the same", Toast.LENGTH_SHORT).show() ;

    }

    public void savePasswordToFirebase(String password) {
        mDatabaseRef.setValue(password);
    }
}