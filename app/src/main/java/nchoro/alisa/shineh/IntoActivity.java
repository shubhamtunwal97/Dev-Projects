package nchoro.alisa.shineh;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import nchoro.alisa.shineh.activity.MainActivity;

public class IntoActivity extends Activity {


    ProgressDialog ph;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FirebaseApp.initializeApp(getApplicationContext());

        setContentView(R.layout.activity_into);

        ph=new ProgressDialog(this);
        ph.setMessage("Loading");

        ph.show();


        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference mDatabaseRef = database.getReference("holdy");


        ValueEventListener valueEventListener=new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if(dataSnapshot.exists()){


                    ph.dismiss();



                    Log.e("fe", "EXIST ");

                    Intent i= new Intent(IntoActivity.this, MainActivity.class);
                    startActivity(i);

                }
                else if(!dataSnapshot.exists()){
                    ph.dismiss();

                    Toast.makeText(IntoActivity.this,"Unable to connect",Toast.LENGTH_SHORT).show();

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("FE", databaseError.getMessage()); //Don't ignore errors!

            }
        };

        mDatabaseRef.addListenerForSingleValueEvent(valueEventListener);
    }
}
