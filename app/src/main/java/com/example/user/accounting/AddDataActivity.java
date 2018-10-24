package com.example.user.accounting;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

public class AddDataActivity extends AppCompatActivity {
    Button btn_addConfirm,btn_date,btn_time;
    EditText text_cost,text_type,text_detail;
    Date pickDate;
    Spinner spn_type;
    AutoCompleteTextView text_itemName;

    ArrayAdapter<String> arrayAdapter;
    ArrayList typeString;
    SharedPreferences sharedPreferences;
    Set<String> autoTextSet;

    DocumentReference documentReference;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_data);

        initView();
        setListener();
        getType();
        loadTemp();
    }

    void initView()
    {
        btn_addConfirm=findViewById(R.id.btn_addConfirm);
        btn_date=findViewById(R.id.btn_date);
        btn_time=findViewById(R.id.btn_time);
        text_cost=findViewById(R.id.text_cost);
        text_type=findViewById(R.id.text_type);
        spn_type=findViewById(R.id.spn_type);
        text_detail=findViewById(R.id.text_detail);
        //自動輸入
        text_itemName=findViewById(R.id.text_itemName);
        sharedPreferences=getSharedPreferences("AutoText",MODE_PRIVATE);
        autoTextSet=sharedPreferences.getStringSet("Array",new HashSet<String>());
        String[] autoTextArray=new String[autoTextSet.size()];
        autoTextArray=autoTextSet.toArray(autoTextArray);
        arrayAdapter=new ArrayAdapter<String>(this,R.layout.support_simple_spinner_dropdown_item,autoTextArray);
        text_itemName.setAdapter(arrayAdapter);

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
    //是否載入暫存
    void loadTemp()
    {
        documentReference.collection("Temp").document("Temp").get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                DocumentSnapshot documentSnapshot=task.getResult();
                if(documentSnapshot.exists())
                {
                    new AlertDialog.Builder(AddDataActivity.this)
                            .setTitle(R.string.temporary)
                            .setMessage(R.string.isLoadTemp)
                            .setPositiveButton(R.string.confirm,loadTempDialog)
                            .setNeutralButton(R.string.cancel,loadTempDialog)
                            .setNegativeButton(R.string.giveup,loadTempDialog)
                            .show();
                }
            }
        });
    }
    //載入暫存對話方塊
    DialogInterface.OnClickListener loadTempDialog=new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            switch (which)
            {
                case DialogInterface.BUTTON_POSITIVE:
                    documentReference.collection("Temp").document("Temp").get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            DocumentSnapshot documentSnapshot=task.getResult();
                            text_itemName.setText(documentSnapshot.getString("Item"));
                            //取得Type Index
                            for(int i=0;i<typeString.size();i++)
                                if(typeString.get(i).equals(documentSnapshot.getString("Type")))
                                    spn_type.setSelection(i);
                            text_cost.setText(documentSnapshot.getString("Cost"));
                            text_detail.setText(documentSnapshot.getString("Detail"));
                            //時間
                            Date tmpDate=documentSnapshot.getDate("Date");
                            btn_date.setText(String.format("%d/%02d/%02d",tmpDate.getYear()+1900,tmpDate.getMonth()+1,tmpDate.getDate()));
                            btn_time.setText(String.format("%02d:%02d",tmpDate.getHours(),tmpDate.getMinutes()));
                            pickDate=tmpDate;
                            //刪除暫存
                            documentReference.collection("Temp").document("Temp").delete();
                        }
                    });
                    Toast.makeText(AddDataActivity.this,R.string.loadTempOK,Toast.LENGTH_SHORT).show();
                    break;
                 case DialogInterface.BUTTON_NEGATIVE:
                     documentReference.collection("Temp").document("Temp").delete();
            }
        }
    };
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
           //自動完成
           sharedPreferences=getSharedPreferences("AutoText",MODE_PRIVATE);
           autoTextSet=sharedPreferences.getStringSet("Array",new HashSet<String>());
           autoTextSet.add(itemName);
           sharedPreferences.edit().putStringSet("Array",autoTextSet).commit();
           Toast.makeText(AddDataActivity.this,R.string.addOK,Toast.LENGTH_SHORT).show();
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
    //是否暫存
    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.temporary)
                .setMessage(R.string.isSaveTemp)
                .setPositiveButton(R.string.confirm,saveTempDialog)
                .setNeutralButton(R.string.cancel,saveTempDialog)
                .setNegativeButton(R.string.giveup,saveTempDialog)
                .show();
    }
    //暫存對話方塊
    DialogInterface.OnClickListener saveTempDialog=new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            switch (which)
            {
                case DialogInterface.BUTTON_POSITIVE:
                    String itemName=text_itemName.getText().toString();
                    String costString=text_cost.getText().toString().equals("")?"0":text_cost.getText().toString();
                    String type=text_type.getText().toString();
                    String detail=text_detail.getText().toString();
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
                    documentReference.collection("Temp").document("Temp").set(hashMap);
                    Toast.makeText(AddDataActivity.this,R.string.saveTempOK,Toast.LENGTH_SHORT).show();
                    finish();
                    break;
                case DialogInterface.BUTTON_NEGATIVE:
                    finish();
                    break;
            }
        }
    };
}
