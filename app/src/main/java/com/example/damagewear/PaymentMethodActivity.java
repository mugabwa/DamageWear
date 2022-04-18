package com.example.damagewear;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class PaymentMethodActivity extends AppCompatActivity {
    private String layoutClicked = "";
    public static final String EXTRA_MESSAGE = "com.example.damagewear.MESSAGE";
    private String amount;
    String m_key;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment_method);
        // Get the Intent that started this activity and extract the string
        Intent intent = getIntent();
        amount = intent.getStringExtra(MapsActivity.EXTRA_MESSAGE);
        m_key = intent.getStringExtra("CURRENT_KEY");
        // Capture the layout's TextView and set the string as its text
        RelativeLayout relativeLayout4 = findViewById(R.id.relativeLayout4);
        RelativeLayout relativeLayout5 = findViewById(R.id.relativeLayout5);

        final String bcolor = "#37939393";

        relativeLayout4.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction()==MotionEvent.ACTION_DOWN)
                {
                    layoutClicked = "Cash";
                    relativeLayout4.setBackgroundColor(getResources().getColor(R.color.myColor));
                    relativeLayout5.setBackgroundColor(getResources().getColor(R.color.relativeBackgound));
                }

                return true;
            }
        });

        relativeLayout5.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction()==MotionEvent.ACTION_DOWN)
                {
                    layoutClicked = "Mpesa";
                    relativeLayout5.setBackgroundColor(getResources().getColor(R.color.myColor));
                    relativeLayout4.setBackgroundColor(getResources().getColor(R.color.relativeBackgound));
                }

                return true;
            }
        });


    }

    public void cashMethod(View view){
        Intent intent;
        if(layoutClicked.equals("Mpesa")) {
            intent = new Intent(this, PaymentActivity.class);
        }
        else{
            intent = new Intent(this, CashPaymentActivity.class);
        }
        intent.putExtra(EXTRA_MESSAGE, amount);
        intent.putExtra("CURRENT_KEY",m_key);
        startActivity(intent);
    }
}