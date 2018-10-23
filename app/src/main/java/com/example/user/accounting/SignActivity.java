package com.example.user.accounting;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class SignActivity extends AppCompatActivity {
    Button btn_signConfirm;
    EditText input_username,input_password;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign);

        initView();
        setListener();
    }

    Button.OnClickListener signup = new Button.OnClickListener()
    {
        @Override
        public void onClick(View v)
        {
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
                    //帳號可註冊
                    if(task.getResult().isEmpty())
                    {
                        Map<String, Object> user = new HashMap<>();
                        user.put("Username", username);
                        user.put("Password", password);
                        user.put("Type",Arrays.asList(getResources().getStringArray(R.array.dataType)));
                        fs.collection("User").add(user);
                        Toast.makeText(SignActivity.this,R.string.signSuccess,Toast.LENGTH_SHORT).show();
                        finish();
                    }
                    //帳號已存在
                    else
                        Toast.makeText(SignActivity.this,R.string.userExist,Toast.LENGTH_SHORT).show();
                }
            });
        }
    };

    void initView()
    {
        btn_signConfirm=findViewById(R.id.btn_signConfirm);
        input_username=findViewById(R.id.sign_username);
        input_password=findViewById(R.id.sign_password);
    }

    void setListener()
    {
        btn_signConfirm.setOnClickListener(signup);
        input_password.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if(actionId==EditorInfo.IME_ACTION_GO)
                {
                    btn_signConfirm.performClick();
                    return true;
                }
                return false;
            }
        });
    }
}