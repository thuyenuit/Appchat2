package com.example.thuyenbu.htpchat;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class StartActivity extends AppCompatActivity {

    private  Button btnReadyAccount;
    private Button btnCreate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        btnCreate = (Button)findViewById(R.id.btnCreateUser);
        btnReadyAccount = (Button)findViewById(R.id.btnReadyAccount);

        btnCreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent createIntent = new Intent(StartActivity.this, CreateUserActivity.class);
                startActivity(createIntent);
                finish();
            }
        });

        btnReadyAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent loginIntent = new Intent(StartActivity.this, LoginActivity.class);
                startActivity(loginIntent);
                finish();
            }
        });

    }
}
