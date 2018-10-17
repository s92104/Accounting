package com.example.user.accounting;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
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
public class YearFragment extends Fragment implements  ViewPager.OnPageChangeListener{
    Activity activity;
    String path;
    int cost;
    int yearQuery;
    boolean[] isExpand;
    Intent intent;

    TextView text_cost;
    CollectionReference collectionReference;
    ExpandableListView expandableListView;
    ViewPager viewPager;
    Toolbar toolbar_data;

    DataListAdapter dataListAdapter;
    List<Map<String,String>> groupList;
    List<List<Map<String,String>>> childList;
    List<Integer> monthCost;
    public YearFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_year, container, false);
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
        expandableListView=activity.findViewById(R.id.list_year);
        expandableListView.setOnChildClickListener(selectData);
        expandableListView.setOnGroupCollapseListener(collapseMonth);
        expandableListView.setOnGroupExpandListener(expandMonth);
        expandableListView.setGroupIndicator(null);
        text_cost=activity.findViewById(R.id.text_costYear);
        toolbar_data=activity.findViewById(R.id.toolbar_data);
        viewPager=activity.findViewById(R.id.viewPager);
        viewPager.addOnPageChangeListener(this);

        intent=activity.getIntent();
        path=intent.getStringExtra("Path");
        yearQuery=intent.getIntExtra("Year",0);
    }

    void getData()
    {
        groupList=new ArrayList<>();
        childList=new ArrayList<>();
        monthCost=new ArrayList<>();
        cost=0;

        FirebaseFirestore fs=FirebaseFirestore.getInstance();
        //取得集合Reference
        collectionReference = fs.document(path).collection("Data");
        //取得集合中本月文件
        collectionReference.whereEqualTo("Year", yearQuery).orderBy("Month").orderBy("Day").orderBy("Hour").orderBy("Minute").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                //避免延遲
                int documentSize=task.getResult().size();
                if(documentSize > 0)
                {
                    setData(task.getResult().getDocuments());
                    expandableListView.setEnabled(true);
                }
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
                    monthCost.add(0);
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

        cost=0;
        int monthRecord=0;
        int dayRecord=0;
        for(int i=0;i<documentSnapshots.size();i++)
        {
            DocumentSnapshot documentSnapshot=documentSnapshots.get(i);
            //Month
            int tmp=Integer.parseInt(documentSnapshot.get("Month").toString());
            if(tmp!=monthRecord)
            {
                //Month
                monthRecord=tmp;
                Map<String,String> map=new HashMap<>();
                map.put("Group",String.valueOf(tmp)+getString(R.string.month));
                groupList.add(map);
                //Day
                List<Map<String,String>> tmpList=new ArrayList<>();
                childList.add(tmpList);
                //Cost
                monthCost.add(0);
            }
            //Day
            tmp=Integer.parseInt(documentSnapshot.get("Day").toString());
            if(tmp!=dayRecord)
            {
                dayRecord=tmp;
                Map<String,String> map=new HashMap<>();
                String day=documentSnapshot.get("Day").toString();
                map.put("Child",day+getString(R.string.day));
                childList.get(childList.size()-1).add(map);
            }
            //金額
            monthCost.set(monthCost.size()-1,monthCost.get(monthCost.size()-1) + Integer.parseInt(documentSnapshot.getString("Cost")));
            cost+=Integer.parseInt(documentSnapshot.getString("Cost"));
        }
    }

    void showData()
    {
        dataListAdapter=new DataListAdapter(activity,groupList,childList);
        expandableListView.setAdapter(dataListAdapter);
        //總花費
        text_cost.setText(getString(R.string.yearCost) + String.valueOf(cost) + getString(R.string.dollar));
        //展開
        if(isExpand!=null)
            for(int i=0;i<isExpand.length;i++)
                if(isExpand[i])
                    expandableListView.expandGroup(i);
    }

    ExpandableListView.OnChildClickListener selectData=new ExpandableListView.OnChildClickListener() {
        @Override
        public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
            intent.putExtra("Year",yearQuery);
            String string=groupList.get(groupPosition).get("Group");
            intent.putExtra("Month",Integer.parseInt(string.substring(0,string.length()-1)));
            string=childList.get(groupPosition).get(childPosition).get("Child");
            intent.putExtra("Day",Integer.parseInt(string.substring(0,string.length()-1)));
            ViewPager viewPager=activity.findViewById(R.id.viewPager);
            viewPager.setCurrentItem(0);
            return false;
        }
    };
    void showMonthCost()
    {
        cost=0;
        boolean isAllCollapse=isAllCollapse();
        for(int i=0;i<groupList.size();i++)
            if(expandableListView.isGroupExpanded(i) || isAllCollapse)
                cost+=monthCost.get(i);
        text_cost.setText((isAllCollapse?getString(R.string.monthCost):getString(R.string.totalCost)) + String.valueOf(cost) + getString(R.string.dollar));
    }
    boolean isAllCollapse()
    {
        for(int i=0;i<groupList.size();i++)
            if(expandableListView.isGroupExpanded(i))
                return false;
        return true;
    }
    //展開月份
    ExpandableListView.OnGroupExpandListener expandMonth=new ExpandableListView.OnGroupExpandListener() {
        @Override
        public void onGroupExpand(int groupPosition) {
            showMonthCost();
        }
    };
    //收合月份
    ExpandableListView.OnGroupCollapseListener  collapseMonth=new ExpandableListView.OnGroupCollapseListener() {
        @Override
        public void onGroupCollapse(int groupPosition) {
            showMonthCost();
        }
    };
    //OnPageChange
    @Override
    public void onPageSelected(int i) {
        if(i==2)
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
