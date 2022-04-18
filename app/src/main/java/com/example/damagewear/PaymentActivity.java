package com.example.damagewear;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class PaymentActivity extends AppCompatActivity {
    final String TAG = PaymentActivity.class.getSimpleName();
    String domain_name;
    String m_key;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        domain_name = getString(R.string.web_domain_name);
        setContentView(R.layout.activity_payment);
        Intent intent = getIntent();
        String message = intent.getStringExtra(PaymentMethodActivity.EXTRA_MESSAGE);
        m_key = intent.getStringExtra("CURRENT_KEY");
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
                    StopData();
                }
            }
        });
    }
    // Signal end of data stream
    public void StopData(){
        String msg = "#TEST#"+m_key+"#";
        SmsManager smsManager = SmsManager.getDefault();
        smsManager.sendTextMessage(getString(R.string.phone_number),null,msg,null,null);

        Toast.makeText(getApplicationContext(), "Message Sent successfully!", Toast.LENGTH_LONG).show();
    }
}