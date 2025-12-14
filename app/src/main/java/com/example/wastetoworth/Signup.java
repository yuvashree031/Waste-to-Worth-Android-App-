package com.example.wastetoworth;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.RadioGroup;
import android.widget.RadioButton;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;
public class Signup extends AppCompatActivity {
    public static final String TAG = "TAG";
    EditText mFullName,mEmail,mPassword,mPhone;
    Button mRegisterBtn;
    TextView mLoginBtn;
    RadioGroup userTypeRadioGroup;
    FirebaseAuth fAuth;
    FirebaseFirestore fStore;
    String userID;
    String userRole;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        
        // Get the role parameter from intent (if coming from role selection)
        userRole = getIntent().getStringExtra("USER_ROLE");
        if (userRole == null) {
            userRole = "user"; // Default role
        }
        mFullName = findViewById(R.id.name);
        mEmail = findViewById(R.id.email);
        mPassword = findViewById(R.id.password);
        mPhone = findViewById(R.id.phone);
        mRegisterBtn=findViewById(R.id.register);
        mLoginBtn = findViewById(R.id.login);
        userTypeRadioGroup = findViewById(R.id.userTypeRadioGroup);
        fAuth=FirebaseAuth.getInstance();
        fStore=FirebaseFirestore.getInstance();
        Log.d(TAG, "Firebase Auth initialized: " + (fAuth != null));
        Log.d(TAG, "Firebase Firestore initialized: " + (fStore != null));
        if(fAuth.getCurrentUser() !=null){
            //startActivity(new Intent(getApplicationContext(),MainActivity.class));
            //finish();
            //for closing the previous tasks and activities
            Intent intent = new Intent(Signup.this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
        mRegisterBtn.setOnClickListener(new View.OnClickListener ()
        {
            @Override
            public void onClick(View v)
            {
                Log.d(TAG, "Register button clicked!");
                String email = mEmail.getText().toString().trim();
                String password= mPassword.getText().toString().trim();
                String name= mFullName.getText().toString().trim();
                String phone= mPhone.getText().toString().trim();
                if(TextUtils.isEmpty(email))
                {
                    mEmail.setError("Email is Required.");
                    return;
                }
                if(TextUtils.isEmpty(password))
                {
                    mPassword.setError("Password is Required.");
                    return;
                }
                if(password.length() < 6)
                {
                    mPassword.setError("Password Must be >=6 Characters");
                    return;
                }
                Log.d(TAG, "Attempting to create user with email: " + email);
                fAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            Toast.makeText(Signup.this, "User Created.", Toast.LENGTH_SHORT) .show();
                            userID = fAuth.getCurrentUser().getUid();
                            DocumentReference documentReference = fStore.collection("users").document(userID);
                            Map<String,Object> user = new HashMap<>();
                            // Get selected user type or use role from intent
                            String userType = userRole; // Use role from intent (default "user" or "ngo")
                            
                            user.put("name",name);
                            user.put("email",email);
                            user.put("phone",phone);
                            user.put("userType",userType);
                            documentReference.set(user).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Log.d(TAG,"onSuccess: user Profile is created for "+ userID);
                                    Toast.makeText(Signup.this, "Registered Successfully.", Toast.LENGTH_SHORT) .show();
                                }
                            });
                            //startActivity(new Intent(getApplicationContext(),MainActivity.class));
                            Intent intent = new Intent(Signup.this, MainActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                        }
                        else{
                            Toast.makeText(Signup.this, "Error!" + task.getException().getMessage(),Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });
        mLoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), Logup.class);
                intent.putExtra("USER_ROLE", userRole); // Pass role back to login
                startActivity(intent);
            }
        });
    }
}