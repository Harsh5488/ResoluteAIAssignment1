package com.harsh.resoluteaiproject;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

import java.util.ArrayList;

public class WelcomePage extends AppCompatActivity {

    private TextView textLinkedin,textGithub;
    private Button btnQRScanner, btnSignOut;
    private DatabaseReference dbRef;
    private ListView listCodes;
    private ArrayList<String> arr;
    private FirebaseAuth mAuth;
    private FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome_page);

        textLinkedin = findViewById(R.id.textLinkedin);
        textGithub = findViewById(R.id.textGithub);
        btnQRScanner = findViewById(R.id.btnQRScanner);
        btnSignOut = findViewById(R.id.btnSignOut);
        listCodes = findViewById(R.id.listCodes);

        arr = new ArrayList<>();
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1,arr);
        listCodes.setAdapter(adapter);

        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();

        if(user!=null){
            dbRef = FirebaseDatabase.getInstance().getReference("qrData").child(user.getUid());
        }
        else{
            dbRef = FirebaseDatabase.getInstance().getReference("qrData");
        }

        dbRef.addValueEventListener(new ValueEventListener(){

            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                arr.clear();
                for(DataSnapshot dataSnapshot : snapshot.getChildren()){
                    String str = dataSnapshot.getValue(String.class);
                    if(str!=null) {
                        arr.add(str);
                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getApplicationContext(), "Failed to fetch data from the database", Toast.LENGTH_SHORT).show();
            }
        });

        textLinkedin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openWeb("https://www.linkedin.com/in/harshvardhan-gupta-5488h");
            }
        });

        textGithub.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openWeb("https://github.com/Harsh5488");
            }
        });

        ActivityResultLauncher<ScanOptions> barLauncher = registerForActivityResult(new ScanContract(), result -> {
            if(result.getContents() != null){
                AlertDialog.Builder builder = new AlertDialog.Builder(WelcomePage.this);
                builder.setTitle("Result");
                String res = result.getContents();
                builder.setMessage(res);

                //database storing
                dbRef.push().setValue(res)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                Toast.makeText(getApplicationContext(),"Data Inserted Successfully",Toast.LENGTH_SHORT).show();
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(getApplicationContext(),"Failed to Push",Toast.LENGTH_SHORT).show();
                            }
                        });

                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).show();
            }
        });

        btnQRScanner.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ScanOptions so = new ScanOptions();
                so.setPrompt("Volume up to flash on");
                so.setBeepEnabled(true);
                so.setOrientationLocked(true);
                so.setCaptureActivity(ScannerView.class);
                barLauncher.launch(so);
            }
        });

        btnSignOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAuth.signOut();
                user = mAuth.getCurrentUser();
                if(user==null){
                    Toast.makeText(WelcomePage.this, "Signed out successfully", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                    startActivity(intent);
                    finish();
                }
                else{
                    Toast.makeText(WelcomePage.this, "Sign out Failed", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Add a long-click listener to your ListView items to enable deletion
        listCodes.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                // Show a confirmation dialog for deletion
                AlertDialog.Builder builder = new AlertDialog.Builder(WelcomePage.this);
                builder.setTitle("Confirm Deletion");
                builder.setMessage("Do you want to delete this item?");
                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String deletedItem = arr.get(position);

                        user = mAuth.getCurrentUser();
                        DatabaseReference userDbRef = FirebaseDatabase.getInstance().getReference("qrData").child(user.getUid());
                        userDbRef.orderByValue().equalTo(deletedItem).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                    snapshot.getRef().removeValue();
                                    arr.remove(position);
                                    adapter.notifyDataSetChanged();
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                // Handle any errors here
                                Toast.makeText(getApplicationContext(), "Failed to delete item from the database", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                });
                builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // User canceled the deletion, do nothing
                    }
                });
                builder.show();

                return true; // Return true to indicate that the long-click event is consumed
            }
        });

    }

    private void openWeb(String link) {
        Uri webpage = Uri.parse(link);
        Intent intent = new Intent(Intent.ACTION_VIEW, webpage);
        startActivity(intent);
    }
}