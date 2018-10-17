package com.example.user.accounting;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class TodayFragment extends Fragment {
    Activity activity;
    String path;
    int yearQuery,monthQuery,dayQuery;
    int cost;

    TextView text_cost;
    ListView listView;
    String[] dataString;
    DocumentReference[] documentReferences;
    ArrayAdapter<String> arrayAdapter;
    CollectionReference collectionReference;

    public TodayFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_today, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        initView();
        getData();
    }

    @Override
    public void onResume() {
        super.onResume();

        getData();
    }

    void initView()
    {
        activity=getActivity();
        listView=activity.findViewById(R.id.list_today);
        text_cost=activity.findViewById(R.id.text_costToday);
        cost=0;
        path=activity.getIntent().getStringExtra("Path");
        listView.setOnItemClickListener(edit);
        Intent intent=activity.getIntent();
        yearQuery=intent.getIntExtra("Year",0);
        monthQuery=intent.getIntExtra("Month",0);
        dayQuery=intent.getIntExtra("Day",0);
    }

    void getData()
    {
        FirebaseFirestore fs=FirebaseFirestore.getInstance();
        //取得集合Reference
        collectionReference = fs.document(path).collection("Data");
        //取得集合中今天文件
        collectionReference.whereEqualTo("Year", yearQuery).whereEqualTo("Month",monthQuery).whereEqualTo("Day",dayQuery).orderBy("Hour").orderBy("Minute").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                //避免延遲
                int documentSize=task.getResult().size();
                if(documentSize > 0)
                {
                    setData(task.getResult().getDocuments());
                    listView.setEnabled(true);
                }
                else
                {
                    dataString=new String[1];
                    dataString[0]=getString(R.string.noData);
                    listView.setEnabled(false);
                }
                showData();
            }
        });

    }

    void setData(List<DocumentSnapshot> documentSnapshots)
    {
        dataString=new String[documentSnapshots.size()];
        documentReferences=new DocumentReference[documentSnapshots.size()];
        cost=0;
        for(int i=0;i<documentSnapshots.size();i++)
        {
            DocumentSnapshot documentSnapshot=documentSnapshots.get(i);

            //文件
            documentReferences[i]=documentSnapshot.getReference();
            //資料
            String year=documentSnapshot.get("Year").toString();
            String month=documentSnapshot.get("Month").toString();
            String day=documentSnapshot.get("Day").toString();
            String hour=documentSnapshot.get("Hour").toString();
            String minute=documentSnapshot.get("Minute").toString();
            String item=documentSnapshot.getString("Item");
            String costString=documentSnapshot.getString("Cost");
            //清單
            dataString[i] = String.format("%s/%02d/%02d  %02d:%02d  %s  %s%s",year,Integer.parseInt(month),Integer.parseInt(day),Integer.parseInt(hour),Integer.parseInt(minute),item,costString,getString(R.string.dollar));
            //金額
            cost+=Integer.parseInt(costString);
        }
    }

    void showData()
    {
        arrayAdapter =new ArrayAdapter<String>(activity,R.layout.support_simple_spinner_dropdown_item, dataString);
        listView.setAdapter(arrayAdapter);
        text_cost.setText(getString(R.string.todayCost) + String.valueOf(cost) + getString(R.string.dollar));
    }
    //修改資料
    AdapterView.OnItemClickListener edit=new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
            documentReferences[position].get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    DocumentSnapshot documentSnapshot=task.getResult();
                    startActivity(new Intent(activity,EditDataActivity.class)
                            .putExtra("Path",documentReferences[position].getPath())
                            .putExtra("Item",documentSnapshot.getString("Item"))
                            .putExtra("Type",documentSnapshot.getString("Type"))
                            .putExtra("Cost",documentSnapshot.getString("Cost"))
                            .putExtra("Year",documentSnapshot.get("Year").toString())
                            .putExtra("Month",documentSnapshot.get("Month").toString())
                            .putExtra("Day",documentSnapshot.get("Day").toString())
                            .putExtra("Hour",documentSnapshot.get("Hour").toString())
                            .putExtra("Minute",documentSnapshot.get("Minute").toString())
                            .putExtra("Detail",documentSnapshot.getString("Detail"))
                    );
                }
            });
        }
    };
}
