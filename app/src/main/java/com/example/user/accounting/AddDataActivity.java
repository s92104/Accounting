package com.example.user.accounting;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
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
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AddDataActivity extends AppCompatActivity {
    Button btn_addConfirm,btn_date,btn_time;
    EditText text_itemName,text_cost,text_type,text_detail;
    Date pickDate;
    Spinner spn_type;

    ArrayAdapter<String> arrayAdapter;
    ArrayList typeString;

    DocumentReference documentReference;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_data);

        initView();
        setListener();
        getType();
    }

    void initView()
    {
        btn_addConfirm=findViewById(R.id.btn_addConfirm);
        btn_date=findViewById(R.id.btn_date);
        btn_time=findViewById(R.id.btn_time);
        text_itemName=findViewById(R.id.text_itemName);
        text_cost=findViewById(R.id.text_cost);
        text_type=findViewById(R.id.text_type);
        spn_type=findViewById(R.id.spn_type);
        text_detail=findViewById(R.id.text_detail);

        Date date=new Date();
        pickDate=date;
        int year=date.getYear()+1900;
        int month=date.getMonth()+1;
        int day=date.getDate();
        int hour=date.getHours();
        int minute=date.getMinutes();
        //按鈕
        btn_date.setText(String.format("%d/%02d/%02d",year,month,day));
        btn_time.setText(String.format("%02d:%02d",hour,minute));
    }

    void setListener()
    {
        btn_addConfirm.setOnClickListener(addData);
        btn_date.setOnClickListener(setTime);
        btn_time.setOnClickListener(setTime);
        spn_type.setOnItemSelectedListener(typeSelect);
    }
    //取得資料類別
    void getType()
    {
        String path=getIntent().getStringExtra("Path");
        FirebaseFirestore fs=FirebaseFirestore.getInstance();
        documentReference=fs.document(path);
        documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
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
    }
    //增加資料
   View.OnClickListener addData=new View.OnClickListener() {
       @Override
       public void onClick(View v) {
            final String itemName=text_itemName.getText().toString();
            final String costString=text_cost.getText().toString().equals("")?"0":text_cost.getText().toString();
            final String type=text_type.getText().toString();
            final String detail=text_detail.getText().toString();
           //類別
           if(text_type.isEnabled())
           {
               typeString.add(text_type.getText().toString());
               typeString.set(typeString.size()-2,typeString.get(typeString.size()-1));
               typeString.set(typeString.size()-1,"+");
               Map<String,Object> map=new HashMap<>();
               map.put("Type",typeString);
               documentReference.update(map);
           }
           //資料
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
           documentReference.collection("Data").add(hashMap);
           Toast.makeText(AddDataActivity.this,R.string.addOK,Toast.LENGTH_SHORT).show();
           finish();
       }
   };
    //設定時間
    View.OnClickListener setTime=new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Date date=new Date();
            int year=date.getYear()+1900;
            int month=date.getMonth()+1;
            int day=date.getDate();
            final int hour=date.getHours();
            int minute=date.getMinutes();
            switch (v.getId())
            {
                case R.id.btn_date:
                    new DatePickerDialog(AddDataActivity.this, new DatePickerDialog.OnDateSetListener() {
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
                    new TimePickerDialog(AddDataActivity.this, new TimePickerDialog.OnTimeSetListener() {
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


}
