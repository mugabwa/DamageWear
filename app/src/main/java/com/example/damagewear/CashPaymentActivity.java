package com.example.damagewear;

import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class CashPaymentActivity extends AppCompatActivity {
    final String TAG = CashPaymentActivity.class.getSimpleName();
    String domain_name;
    String m_key;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cash_payment);
        domain_name = getString(R.string.web_domain_name);
        Intent intent = getIntent();
        String message = intent.getStringExtra(PaymentMethodActivity.EXTRA_MESSAGE);
        m_key = intent.getStringExtra("CURRENT_KEY");
        TextView textView = findViewById(R.id.amount);
        textView.setText(message);
        Button btn = (Button) findViewById(R.id.button);

        btn.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                Toast.makeText(getBaseContext(),"Payment Successful Completed!",Toast.LENGTH_SHORT).show();
                btn.setEnabled(false);
                Log.i(TAG, "Button disabled");
                StopData();
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