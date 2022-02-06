package com.example.damagewear;

import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class CashPaymentActivity extends AppCompatActivity {
    final String TAG = CashPaymentActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cash_payment);
        Intent intent = getIntent();
        String message = intent.getStringExtra(PaymentMethodActivity.EXTRA_MESSAGE);
        TextView textView = findViewById(R.id.amount);
        textView.setText(message);
        Button btn = (Button) findViewById(R.id.button);

        btn.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                Toast.makeText(getBaseContext(),"Payment Successful Completed!",Toast.LENGTH_SHORT).show();
                btn.setEnabled(false);
                Log.i(TAG, "Button disabled");
            }
        });
    }
}