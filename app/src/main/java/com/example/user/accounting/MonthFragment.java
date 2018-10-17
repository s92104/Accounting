package com.example.user.accounting;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.annotation.NonNull;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * A simple {@link Fragment} subclass.
 */
public class MonthFragment extends Fragment implements ViewPager.OnPageChangeListener{
    Activity activity;
    String path;
    int position;
    int yearQuery;
    int monthQuery;
    int cost;
    boolean[] isExpand;
    List<Integer> dayCost;
    Intent intent;

    TextView text_cost;
    ExpandableListView expandableListView;
    CollectionReference collectionReference;
    DocumentReference[] documentReferences;
    ViewPager viewPager;
    Toolbar toolbar_data;

    DataListAdapter dataListAdapter;
    List<Map<String,String>> groupList;
    List<List<Map<String,String>>> childList;
    public MonthFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_month, container, false);
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

    @Override
    public void onPause() {
        super.onPause();

        //紀錄展開的Group
        isExpand=new boolean[groupList.size()];
        for(int i=0;i<groupList.size();i++)
            isExpand[i]=expandableListView.isGroupExpanded(i);
    }

    void initView()
    {
        activity=getActivity();
        expandableListView=activity.findViewById(R.id.list_month);
        expandableListView.setOnChildClickListener(selectData);
        expandableListView.setOnGroupExpandListener(expandDay);
        expandableListView.setOnGroupCollapseListener(collapseDay);
        expandableListView.setGroupIndicator(null);
        text_cost=activity.findViewById(R.id.text_costMonth);
        toolbar_data=activity.findViewById(R.id.toolbar_data);
        viewPager=activity.findViewById(R.id.viewPager);
        viewPager.addOnPageChangeListener(this);

        intent=activity.getIntent();
        path=intent.getStringExtra("Path");
        yearQuery=intent.getIntExtra("Year",0);
        monthQuery=intent.getIntExtra("Month",0);
    }

    void getData()
    {
        groupList=new ArrayList<>();
        childList=new ArrayList<>();
        dayCost=new ArrayList<>();
        cost=0;

        FirebaseFirestore fs=FirebaseFirestore.getInstance();
        collectionReference=fs.document(path).collection("Data");
        //取得集合中本月文件
        collectionReference.whereEqualTo("Year", yearQuery).whereEqualTo("Month",monthQuery).orderBy("Day").orderBy("Hour").orderBy("Minute").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                //避免延遲
                int documentSize=task.getResult().size();
                if(documentSize > 0)
                {
                    setData(task.getResult().getDocuments());
                    expandableListView.setEnabled(true);
                }
                //無資料
                else
                {
                    if(groupList.size()>0)
                        return;
                    //Day
                    Map<String,String> map=new HashMap<>();
                    map.put("Group",getString(R.string.noData));
                    map.put("Child",getString(R.string.noData));
                    groupList.add(map);
                    //Data
                    List<Map<String,String>> tmpList=new ArrayList<>();
                    tmpList.add(map);
                    childList.add(tmpList);
                    //Cost
                    dayCost.add(0);
                    expandableListView.setEnabled(false);
                }
                showData();
            }
        });
    }

    void setData(List<DocumentSnapshot> documentSnapshots)
    {
        //避免執行兩次
        if(groupList.size()>0)
            return;

        int dayRecord=0;
        documentReferences=new DocumentReference[documentSnapshots.size()];
        for(int i=0;i<documentSnapshots.size();i++)
        {
            DocumentSnapshot documentSnapshot=documentSnapshots.get(i);
            documentReferences[i]=documentSnapshot.getReference();
            int tmp=Integer.parseInt(documentSnapshot.get("Day").toString());
            if(tmp!=dayRecord)
            {
                //daySet
                dayRecord=tmp;
                Map<String,String> map=new HashMap<>();
                map.put("Group",String.valueOf(tmp)+getString(R.string.day));
                groupList.add(map);
                //資料
                List<Map<String,String>> tmpList=new ArrayList<>();
                childList.add(tmpList);
                //Cost
                dayCost.add(0);
            }
            //資料
            String year=documentSnapshot.get("Year").toString();
            String month=documentSnapshot.get("Month").toString();
            String day=documentSnapshot.get("Day").toString();
            String hour=documentSnapshot.get("Hour").toString();
            String minute=documentSnapshot.get("Minute").toString();
            String item=documentSnapshot.getString("Item");
            String costString=documentSnapshot.getString("Cost");
            //清單
            String data = String.format("%s/%02d/%02d  %02d:%02d  %s  %s%s",year,Integer.parseInt(month),Integer.parseInt(day),Integer.parseInt(hour),Integer.parseInt(minute),item,costString,getString(R.string.dollar));
            Map<String,String> map=new HashMap<>();
            map.put("Child",data);
            childList.get(childList.size()-1).add(map);
            //金額
            dayCost.set(dayCost.size()-1,dayCost.get(dayCost.size()-1) + Integer.parseInt(documentSnapshot.getString("Cost")));
            cost+=Integer.parseInt(documentSnapshot.getString("Cost"));
        }
    }

    void showData()
    {
        dataListAdapter=new DataListAdapter(activity,groupList,childList);
        expandableListView.setAdapter(dataListAdapter);
        //總花費
        text_cost.setText(getString(R.string.monthCost) + String.valueOf(cost) + getString(R.string.dollar));
        //展開
        if(isExpand!=null)
            for(int i=0;i<isExpand.length;i++)
                if(isExpand[i])
                    expandableListView.expandGroup(i);
    }
    //選擇資料
    ExpandableListView.OnChildClickListener selectData=new ExpandableListView.OnChildClickListener() {
        @Override
        public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
            //計算第幾個
            position=0;
            for(int i=0;i<groupPosition;i++)
                position+=childList.get(i).size();
            position+=childPosition;
            documentReferences[position].get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    startActivity(new Intent(activity,EditDataActivity.class)
                            .putExtra("Path",documentReferences[position].getPath())
                            .putExtra("Item",task.getResult().getString("Item"))
                            .putExtra("Type",task.getResult().getString("Type"))
                            .putExtra("Cost",task.getResult().getString("Cost"))
                            .putExtra("Year",task.getResult().get("Year").toString())
                            .putExtra("Month",task.getResult().get("Month").toString())
                            .putExtra("Day",task.getResult().get("Day").toString())
                            .putExtra("Hour",task.getResult().get("Hour").toString())
                            .putExtra("Minute",task.getResult().get("Minute").toString())
                            .putExtra("Detail",task.getResult().getString("Detail"))
                    );
                }
            });
            return false;
        }
    };
    void showDayCost()
    {
        cost=0;
        boolean isAllCollapse=isAllCollapse();
        for(int i=0;i<groupList.size();i++)
            if(expandableListView.isGroupExpanded(i) || isAllCollapse)
                cost+=dayCost.get(i);
        text_cost.setText((isAllCollapse?getString(R.string.monthCost):getString(R.string.totalCost)) + String.valueOf(cost) + getString(R.string.dollar));
    }
    boolean isAllCollapse()
    {
        for(int i=0;i<groupList.size();i++)
            if(expandableListView.isGroupExpanded(i))
                return false;
        return true;
    }
    //展開日期
    ExpandableListView.OnGroupExpandListener expandDay=new ExpandableListView.OnGroupExpandListener() {
        @Override
        public void onGroupExpand(int groupPosition) {
            showDayCost();
        }
    };
    //收合日期
    ExpandableListView.OnGroupCollapseListener  collapseDay=new ExpandableListView.OnGroupCollapseListener() {
        @Override
        public void onGroupCollapse(int groupPosition) {
            showDayCost();
        }
    };
    //OnPageChange
    @Override
    public void onPageSelected(int i) {
        if(i==1)
        {
            toolbar_data.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem menuItem) {
                    switch (menuItem.getItemId())
                    {
                        case R.id.it_addData:
                            startActivity(new Intent(activity,AddDataActivity.class).putExtra("Path",path));
                            break;
                        case R.id.it_expandAll:
                            for(int i=0;i<groupList.size();i++)
                                    expandableListView.expandGroup(i);
                            break;
                        case R.id.it_collapseAll:
                            for(int i=0;i<groupList.size();i++)
                                expandableListView.collapseGroup(i);
                            break;
                    }
                    return false;
                }
            });
        }
    }

    @Override
    public void onPageScrolled(int i, float v, int i1) {

    }

    @Override
    public void onPageScrollStateChanged(int i) {

    }
}
