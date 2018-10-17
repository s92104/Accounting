package com.example.user.accounting;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TimePicker;
import android.widget.Toast;
import android.widget.Toolbar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class EditDataActivity extends AppCompatActivity {
    Button btn_addConfirm,btn_date,btn_time;
    EditText text_itemName,text_cost,text_type,text_detail;
    Date pickDate;
    Spinner spn_type;
    android.support.v7.widget.Toolbar toolbar_editData;

    String path;

    ArrayAdapter<String> arrayAdapter;
    ArrayList typeString;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_date);

        initView();
        initToolBar();
        setListener();
        getType();
    }

    void initView()
    {
        //取出文件路徑
        path=getIntent().getStringExtra("Path");
        btn_addConfirm=findViewById(R.id.btn_addConfirm);
        btn_date=findViewById(R.id.btn_date);
        btn_time=findViewById(R.id.btn_time);
        text_itemName=findViewById(R.id.text_itemName);
        text_cost=findViewById(R.id.text_cost);
        text_type=findViewById(R.id.text_type);
        text_detail=findViewById(R.id.text_detail);
        spn_type=findViewById(R.id.spn_type);

        Intent intent=getIntent();
        //初始化時間
        int year=Integer.parseInt(intent.getStringExtra("Year"));
        int month=Integer.parseInt(intent.getStringExtra("Month"));
        int day=Integer.parseInt(intent.getStringExtra("Day"));
        int hour=Integer.parseInt(intent.getStringExtra("Hour"));
        int minute=Integer.parseInt(intent.getStringExtra("Minute"));
        pickDate=new Date(year-1900,month-1,day,hour,minute);
        btn_date.setText(String.format("%d/%02d/%02d",year,month,day));
        btn_time.setText(String.format("%02d:%02d",hour,minute));
        //初始化資料
        text_itemName.setText(intent.getStringExtra("Item"));
        text_cost.setText(intent.getStringExtra("Cost"));
        text_detail.setText(intent.getStringExtra("Detail"));
    }

    void setListener()
    {
        btn_addConfirm.setOnClickListener(editData);
        btn_date.setOnClickListener(setTime);
        btn_time.setOnClickListener(setTime);
        spn_type.setOnItemSelectedListener(typeSelect);
    }
    //取得資料類別
    void getType()
    {
        FirebaseFirestore fs=FirebaseFirestore.getInstance();
        fs.document(path).getParent().getParent().get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                typeString=(ArrayList) task.getResult().get("Type");
                //避免延遲
                if(typeString!=null)
                    setType();
            }
        });
    }
    //設定Adapter
    void setType()
    {
        arrayAdapter=new ArrayAdapter<String>(this,R.layout.support_simple_spinner_dropdown_item,typeString);
        spn_type.setAdapter(arrayAdapter);
        //設定原資料
        for(int i=0;i<typeString.size();i++)
            if(typeString.get(i).toString().equals(getIntent().getStringExtra("Type")))
                spn_type.setSelection(i);
    }
    //修改資料
    View.OnClickListener editData=new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            final String itemName=text_itemName.getText().toString();
            final String costString=text_cost.getText().toString().equals("")?"0":text_cost.getText().toString();
            final String type=text_type.getText().toString();
            final String detail=text_detail.getText().toString();

            FirebaseFirestore fs=FirebaseFirestore.getInstance();
            if(text_type.isEnabled())
            {
                typeString.add(text_type.getText().toString());
                typeString.set(typeString.size()-2,typeString.get(typeString.size()-1));
                typeString.set(typeString.size()-1,"+");
                Map<String,Object> map=new HashMap<>();
                map.put("Type",typeString);
                fs.document(path).getParent().getParent().update(map);
            }
            HashMap hashMap=new HashMap();
            hashMap.put("Date",pickDate);
            hashMap.put("Year",pickDate.getYear()+1900);
            hashMap.put("Month",pickDate.getMonth()+1);
            hashMap.put("Day",pickDate.getDate());
            hashMap.put("Hour",pickDate.getHours());
            hashMap.put("Minute",pickDate.getMinutes());
            hashMap.put("Item",itemName);
            hashMap.put("Cost",costString);
            hashMap.put("Type",type);
            hashMap.put("Detail",detail);
            fs.document(path).set(hashMap);
            Toast.makeText(EditDataActivity.this,R.string.editOK,Toast.LENGTH_SHORT).show();
            finish();
        }
    };
    //設定時間
    View.OnClickListener setTime=new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            int year=pickDate.getYear()+1900;
            int month=pickDate.getMonth()+1;
            int day=pickDate.getDate();
            final int hour=pickDate.getHours();
            int minute=pickDate.getMinutes();
            switch (v.getId())
            {
                case R.id.btn_date:
                    new DatePickerDialog(EditDataActivity.this, new DatePickerDialog.OnDateSetListener() {
                        @Override
                        public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                            pickDate.setYear(year-1900);
                            pickDate.setMonth(month);
                            pickDate.setDate(dayOfMonth);
                            btn_date.setText(String.format("%d/%02d/%02d",year,month+1,dayOfMonth));
                        }
                    },year,month-1,day).show();
                    break;
                case R.id.btn_time:
                    new TimePickerDialog(EditDataActivity.this, new TimePickerDialog.OnTimeSetListener() {
                        @Override
                        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                            pickDate.setHours(hourOfDay);
                            pickDate.setMinutes(minute);
                            btn_time.setText(String.format("%02d:%02d",hourOfDay,minute));
                        }
                    },hour,minute,true).show();
                    break;
            }
        }
    };
    //下拉選單
    AdapterView.OnItemSelectedListener typeSelect=new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            if(position!=parent.getCount()-1)
            {
                text_type.setText(parent.getSelectedItem().toString());
                text_type.setEnabled(false);
            }
            else
            {
                text_type.setText("");
                text_type.setEnabled(true);
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }
    };
    void initToolBar()
    {
        toolbar_editData=findViewById(R.id.toolbar_editData);
        toolbar_editData.inflateMenu(R.menu.menu_editdata);
        //新增資料
        toolbar_editData.setOnMenuItemClickListener(new android.support.v7.widget.Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                switch (menuItem.getItemId())
                {
                    case R.id.it_deleteData: new AlertDialog.Builder(EditDataActivity.this)
                            .setTitle(R.string.delete)
                            .setMessage(R.string.sureDelete)
                            .setPositiveButton(R.string.confirm,deleteDialog)
                            .setNegativeButton(R.string.cancel,deleteDialog)
                            .show();
                    break;
                }
                return false;
            }
        });
    }
    //確定刪除對話方塊
    DialogInterface.OnClickListener deleteDialog=new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            if(which==DialogInterface.BUTTON_POSITIVE)
            {
                FirebaseFirestore fs=FirebaseFirestore.getInstance();
                fs.document(path).delete();
                Toast.makeText(EditDataActivity.this,R.string.deleteOK,Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    };
}
