package com.example.touhid.mychat;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class StartPageActivity extends AppCompatActivity {

    private Button create_account_button;
    private Button already_have_account_button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_page);

        create_account_button = (Button) findViewById(R.id.create_account_btn);
        already_have_account_button = (Button) findViewById(R.id.already_have_account_btn);

        create_account_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent registerIntent = new Intent(StartPageActivity.this, RegisterActivity.class);
                startActivity(registerIntent);
            }
        });

        already_have_account_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent loginIntent = new Intent(StartPageActivity.this, LoginActivity.class);
                startActivity(loginIntent);
            }
        });
    }
}
