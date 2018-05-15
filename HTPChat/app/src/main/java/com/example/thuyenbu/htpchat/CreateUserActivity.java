package com.example.thuyenbu.htpchat;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class CreateUserActivity extends AppCompatActivity {

    private TextInputLayout textDisplay;
    private TextInputLayout textEmail;
    private TextInputLayout textPassword;
    private Button btnCreate;
    private FirebaseAuth mAuth;

    private ProgressDialog regDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_user);

        mAuth = FirebaseAuth.getInstance();

        textDisplay = (TextInputLayout)findViewById(R.id.txtDisplayName);
        textEmail = (TextInputLayout)findViewById(R.id.txtEmail);
        textPassword = (TextInputLayout)findViewById(R.id.txtPassword);
        btnCreate = (Button)findViewById(R.id.btnResigter);

        regDialog = new ProgressDialog(this);

        btnCreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String display_name = textDisplay.getEditText().getText().toString();
                String email = textEmail.getEditText().getText().toString();
                String password = textPassword.getEditText().getText().toString();

                if(TextUtils.isEmpty(display_name))
                {
                    Toast.makeText(CreateUserActivity.this,
                            "Vui lòng nhập tên hiển thị!", Toast.LENGTH_LONG).show();
                    return;
                }
                if(TextUtils.isEmpty(email))
                {
                    Toast.makeText(CreateUserActivity.this,
                            "Vui lòng nhập địa chỉ email!", Toast.LENGTH_LONG).show();
                    return;
                }
                if(TextUtils.isEmpty(password))
                {
                    Toast.makeText(CreateUserActivity.this,
                            "Vui lòng nhập mật khẩu!", Toast.LENGTH_LONG).show();
                    return;
                }

                if(!TextUtils.isEmpty(display_name) && !TextUtils.isEmpty(email) &&  !TextUtils.isEmpty(password))
                {
                    regDialog.setTitle("Đang xử lý");
                    regDialog.setMessage("Vui lòng chờ trong giây lát!");
                    regDialog.setCanceledOnTouchOutside(false);
                    regDialog.show();

                    fnCreateUser(display_name, email, password);
                }
            }
        });
    }

    private void fnCreateUser(String display_name, String email, String password) {

       mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
           @Override
           public void onComplete(@NonNull Task<AuthResult> task) {

               if(task.isSuccessful())
               {
                   //Toast.makeText(CreateUserActivity.this,
                   //        "Đăng ký thành công!", Toast.LENGTH_LONG).show();
                   regDialog.dismiss();

                   Intent mainIntent = new Intent(CreateUserActivity.this, StartActivity.class);
                   startActivity(mainIntent);
                   finish();
               }
               else
               {
                   regDialog.hide();
                   Toast.makeText(CreateUserActivity.this,
                           "Không thể đăng ký. Vui lòng thử lại!", Toast.LENGTH_LONG).show();
               }
           }
       });
    }
}
