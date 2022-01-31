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

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment_method);
        // Get the Intent that started this activity and extract the string
        Intent intent = getIntent();
        String message = intent.getStringExtra(MapsActivity.EXTRA_MESSAGE);
        // Capture the layout's TextView and set the string as its text
        TextView textView = findViewById(R.id.textView3);
        textView.setText(message);
        RelativeLayout relativeLayout4 = findViewById(R.id.relativeLayout4);
        RelativeLayout relativeLayout5 = findViewById(R.id.relativeLayout5);
        RelativeLayout relativeLayout6 = findViewById(R.id.relativeLayout6);

        final String bcolor = "#37939393";

        relativeLayout4.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction()==MotionEvent.ACTION_DOWN)
                {
                    layoutClicked = "layout4";
                    relativeLayout4.setBackgroundColor(getResources().getColor(R.color.myColor));
                    relativeLayout5.setBackgroundColor(getResources().getColor(R.color.relativeBackgound));
                    relativeLayout6.setBackgroundColor(getResources().getColor(R.color.relativeBackgound));
                }

                return true;
            }
        });

        relativeLayout5.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction()==MotionEvent.ACTION_DOWN)
                {
                    layoutClicked = "layout5";
                    relativeLayout5.setBackgroundColor(getResources().getColor(R.color.myColor));
                    relativeLayout4.setBackgroundColor(getResources().getColor(R.color.relativeBackgound));
                    relativeLayout6.setBackgroundColor(getResources().getColor(R.color.relativeBackgound));
                }

                return true;
            }
        });

        relativeLayout6.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction()==MotionEvent.ACTION_DOWN)
                {
                    layoutClicked = "layout6";
                    relativeLayout6.setBackgroundColor(getResources().getColor(R.color.myColor));
                    relativeLayout5.setBackgroundColor(getResources().getColor(R.color.relativeBackgound));
                    relativeLayout4.setBackgroundColor(getResources().getColor(R.color.relativeBackgound));
                }
                return true;
            }
        });

    }

    public void cashMethod(View view){
        Intent intent = new Intent(this, PaymentActivity.class);
        String message = layoutClicked;
        intent.putExtra(EXTRA_MESSAGE,message);
        startActivity(intent);
    }
}