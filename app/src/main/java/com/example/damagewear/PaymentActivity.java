package com.example.damagewear;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class PaymentActivity extends AppCompatActivity {
    final String TAG = PaymentActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);
        Intent intent = getIntent();
        String message = intent.getStringExtra(PaymentMethodActivity.EXTRA_MESSAGE);
        TextView textView = findViewById(R.id.amount);
        textView.setText(message);

        EditText phone = (EditText) findViewById(R.id.phoneNumber);

        Button btn = (Button) findViewById(R.id.button);

        btn.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                if ((phone.length()>=0 && phone.length()<10) || phone.length()>10){
                    Toast.makeText(getBaseContext(), "Valid phone number require!!", Toast.LENGTH_SHORT).show();
                    phone.findFocus();
                } else {
                    Toast.makeText(getBaseContext(), "Payment to " + phone.getText().toString() + " being Processed!", Toast.LENGTH_LONG).show();
                    btn.setEnabled(false);
                    phone.setEnabled(false);
                    Log.i(TAG, "Button disabled");
                    Log.i(TAG, "Phone field disabled");
                }
            }
        });
    }
}