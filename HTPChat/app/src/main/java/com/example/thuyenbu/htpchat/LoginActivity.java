package com.example.thuyenbu.htpchat;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity {

    private Toolbar mToolbar;

    private TextInputLayout txtEmail;
    private TextInputLayout txtPassword;
    private Button btnLogin;

    private ProgressDialog loginDialog;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();

        mToolbar = (Toolbar)findViewById(R.id.login_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Đăng nhập");

        loginDialog = new ProgressDialog(this);

        txtEmail = (TextInputLayout)findViewById(R.id.txtEmailLogin);
        txtPassword = (TextInputLayout)findViewById(R.id.txtPasswordLogin);
        btnLogin = (Button)findViewById(R.id.btnLogin);

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String email = txtEmail.getEditText().getText().toString();
                String password = txtPassword.getEditText().getText().toString();

                if(TextUtils.isEmpty(email))
                {
                    Toast.makeText(LoginActivity.this,
                            "Vui lòng nhập địa chỉ email!", Toast.LENGTH_LONG).show();
                    return;
                }
                if(TextUtils.isEmpty(password))
                {
                    Toast.makeText(LoginActivity.this,
                            "Vui lòng nhập mật khẩu!", Toast.LENGTH_LONG).show();
                    return;
                }

                if(!TextUtils.isEmpty(email) &&  !TextUtils.isEmpty(password))
                {
                    loginDialog.setTitle("Đang đăng nhập");
                    loginDialog.setMessage("Vui lòng chờ trong giây lát!");
                    loginDialog.setCanceledOnTouchOutside(false);
                    loginDialog.show();

                    fnLogin(email, password);
                }
            }
        });

    }

    private  void fnLogin(String email, String password)
    {
        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {

                if(task.isSuccessful())
                {
                    loginDialog.dismiss();

                    Intent mainIntent = new Intent(LoginActivity.this, MainActivity.class);
                    startActivity(mainIntent);
                    finish();
                }
                else
                {
                    loginDialog.hide();
                    Toast.makeText(LoginActivity.this,
                            "Email hoặc mật khẩu không hợp lệ. Vui lòng thử lại!", Toast.LENGTH_LONG).show();
                }
            }
        });
    }
}
