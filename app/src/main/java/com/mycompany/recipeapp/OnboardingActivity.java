package com.mycompany.recipeapp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ViewFlipper;

public class OnboardingActivity extends Activity {

    private ViewFlipper viewFlipper;
    private Button btnNext;
    private Button btnGetStarted;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboarding);

        viewFlipper = (ViewFlipper) findViewById(R.id.viewFlipper);
        btnNext = (Button) findViewById(R.id.btnNext);
        btnGetStarted = (Button) findViewById(R.id.btnGetStarted);

        btnNext.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                viewFlipper.showNext();
                if (viewFlipper.getDisplayedChild() == 2) {
                    btnNext.setVisibility(View.GONE);
                    btnGetStarted.setVisibility(View.VISIBLE);
                }
            }
        });

        btnGetStarted.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(OnboardingActivity.this, HomeActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }
}
