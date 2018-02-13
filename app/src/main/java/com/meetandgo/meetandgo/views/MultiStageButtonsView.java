package com.meetandgo.meetandgo.views;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.Button;
import android.widget.LinearLayout;

import com.meetandgo.meetandgo.R;

public class MultiStageButtonsView extends LinearLayout {

    private LinearLayout mLinearLayout;
    private Button mButton1;
    private Button mButton2;
    private Button mButton3;

    public MultiStageButtonsView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        setUpView();
    }

    private void setUpView() {
        inflate(getContext(), R.layout.gender_buttons, this);
        mLinearLayout = findViewById(R.id.linear_layout);
        mButton1 = findViewById(R.id.button1);
        mButton2 = findViewById(R.id.button2);
        mButton3 = findViewById(R.id.button3);
    }
}
