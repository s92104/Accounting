package com.example.user.accounting;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

public class MainActivity extends AppCompatActivity {
    Button btn_login,btn_sign;
    EditText input_username,input_password;
    CheckBox check_remember;
    Switch switch_autoLogin;
    FrameLayout frameLayout;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();
        setListener();
        //自動填入帳密
        if(isRemember() || isAutoLogin())
            getAccount();
        //自動登入
        if(isAutoLogin())
            autoLogin();
    }

    void initView()
    {
        btn_login=findViewById(R.id.btn_login);
        btn_sign=findViewById(R.id.btn_sign);
        input_username=findViewById(R.id.login_username);
        input_password=findViewById(R.id.login_password);
        //記住帳密
        check_remember=findViewById(R.id.check_remember);
        check_remember.setChecked(getSharedPreferences("Account",MODE_PRIVATE).getBoolean("Remember",false));
        //自動登入
        switch_autoLogin=findViewById(R.id.switch_autoLogin);
        switch_autoLogin.setChecked(getSharedPreferences("Account",MODE_PRIVATE).getBoolean("AutoLogin",false));
        //等待畫面
        frameLayout=findViewById(R.id.framelayout_login);
    }

    void setListener()
    {
        btn_login.setOnClickListener(login);
        btn_sign.setOnClickListener(sign);
    }

    boolean isRemember()
    {
        return getSharedPreferences("Account",MODE_PRIVATE).getBoolean("Remember",false);
    }

    void getAccount()
    {
        SharedPreferences sharedPreferences = getSharedPreferences("Account",MODE_PRIVATE);
        input_username.setText(sharedPreferences.getString("Username",""));
        input_password.setText(sharedPreferences.getString("Password",""));
    }

    boolean isAutoLogin()
    {
        return getSharedPreferences("Account",MODE_PRIVATE).getBoolean("AutoLogin",false);
    }

    void autoLogin()
    {
        btn_login.performClick();
    }
    //登入
    Button.OnClickListener login = new Button.OnClickListener()
    {
        @Override
        public void onClick(View v)
        {
            //顯示讀取條
            frameLayout.setVisibility(View.VISIBLE);
            //取得輸入
            final String username,password;
            username=input_username.getText().toString();
            password=input_password.getText().toString();
            //取得資料庫
            final FirebaseFirestore fs=FirebaseFirestore.getInstance();
            //檢查帳號
            Query query=fs.collection("User").whereEqualTo("Username",username);
            query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    QuerySnapshot qs = task.getResult();
                    //隱藏讀取條
                    frameLayout.setVisibility(View.INVISIBLE);
                    //無此帳號
                    if(qs.isEmpty())
                        Toast.makeText(MainActivity.this,R.string.userNotExist,Toast.LENGTH_SHORT).show();
                    //帳號存在
                    else
                    {
                        //檢查密碼
                        DocumentSnapshot ds = qs.getDocuments().get(0);
                        //正確
                        if(ds.getString("Password").equals(password))
                        {
                            //記住帳密
                            SharedPreferences sharedPreferences = getSharedPreferences("Account",MODE_PRIVATE);
                            if(check_remember.isChecked() || switch_autoLogin.isChecked())
                                sharedPreferences.edit().putString("Username",username).putString("Password",password).putBoolean("Remember",true).commit();
                            else
                                sharedPreferences.edit().putBoolean("Remember",false).commit();
                            //自動登入
                            if(switch_autoLogin.isChecked())
                                sharedPreferences.edit().putBoolean("AutoLogin",true).commit();
                            else
                                sharedPreferences.edit().putBoolean("AutoLogin",false).commit();
                            Toast.makeText(MainActivity.this,"登入成功",Toast.LENGTH_SHORT).show();
                            //將文件路徑丟到DataActivity
                            String path=ds.getReference().getPath();
                            startActivity(new Intent(MainActivity.this,DataActivity.class).putExtra("Path",path));
                        }
                        //錯誤
                        else
                            Toast.makeText(MainActivity.this,R.string.passwordError,Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    };
    //註冊
    Button.OnClickListener sign =new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            startActivity(new Intent(MainActivity.this,SignActivity.class));
        }
    };
}
