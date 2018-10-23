package com.example.user.accounting;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.content.Intent;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ExpandableListView;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class DataActivity extends AppCompatActivity implements ViewPager.OnPageChangeListener {
    //User Path
    String path;

    //Global
    Date date;
    Intent intent;

    android.support.v7.widget.Toolbar toolbar_data;
    ViewPager viewPager;
    FragmentPagerAdapter fragmentPagerAdapter;
    FragmentManager fragmentManager;
    Button btn_today,btn_month,btn_year;
    ExpandableListView list_month,list_year;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data);

        initView();
        setListener();
    }

    void initView()
    {
        //取出資料
        path=getIntent().getStringExtra("Path");
        //初始化Button
        btn_today=findViewById(R.id.btn_today);
        btn_today.setSelected(true);
        btn_month=findViewById(R.id.btn_month);
        btn_year=findViewById(R.id.btn_year);
        //ViewPage
        viewPager=findViewById(R.id.viewPager);
        fragmentManager=getSupportFragmentManager();
        fragmentPagerAdapter=new DataPagerAdapter(fragmentManager);
        viewPager.setAdapter(fragmentPagerAdapter);
        viewPager.addOnPageChangeListener(this);
        //初始化Today
        date =new Date();
        intent=getIntent();
        intent.putExtra("Year",date.getYear()+1900);
        intent.putExtra("Month",date.getMonth()+1);
        intent.putExtra("Day",date.getDate());
        //ToolBar
        toolbar_data=findViewById(R.id.toolbar_data);
        toolbar_data.inflateMenu(R.menu.menu_data);
        toolbar_data.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                switch (menuItem.getItemId())
                {
                    case R.id.it_addData:
                        startActivity(new Intent(DataActivity.this,AddDataActivity.class).putExtra("Path",path));
                        break;
                    case R.id.it_expandAll:
                        switch (viewPager.getCurrentItem())
                        {
                            case 1:for(int i=0;i<list_month.getExpandableListAdapter().getGroupCount();i++) list_month.expandGroup(i);break;
                            case 2:for(int i=0;i<list_year.getExpandableListAdapter().getGroupCount();i++) list_year.expandGroup(i);break;
                        }
                        break;
                    case R.id.it_collapseAll:
                        switch (viewPager.getCurrentItem())
                        {
                            case 1:for(int i=0;i<list_month.getExpandableListAdapter().getGroupCount();i++) list_month.collapseGroup(i);break;
                            case 2:for(int i=0;i<list_year.getExpandableListAdapter().getGroupCount();i++) list_year.collapseGroup(i);break;
                        }
                        break;
                }
                return false;
            }
        });
    }

    void setListener()
    {
        btn_today.setOnClickListener(changeFragment);
        btn_month.setOnClickListener(changeFragment);
        btn_year.setOnClickListener(changeFragment);
    }
    //切換頁面
    View.OnClickListener changeFragment=new View.OnClickListener()
    {
        @Override
        public void onClick(View v)
        {
            switch (v.getId())
            {
                case R.id.btn_today:
                    boolean isToday=isToday();
                    intent.putExtra("Year",date.getYear()+1900);
                    intent.putExtra("Month",date.getMonth()+1);
                    intent.putExtra("Day",date.getDate());
                    //重整
                    if(viewPager.getCurrentItem()==0)
                        viewPager.setAdapter(fragmentPagerAdapter);
                    viewPager.setCurrentItem(0);
                    //不是今天，重整
                    if(!isToday)
                        viewPager.setAdapter(fragmentPagerAdapter);
                    break;
                case R.id.btn_month:
                    intent.putExtra("Year",date.getYear()+1900);
                    intent.putExtra("Month",date.getMonth()+1);
                    viewPager.setCurrentItem(1);
                    break;
                case R.id.btn_year:
                    intent.putExtra("Year",date.getYear()+1900);
                    viewPager.setCurrentItem(2);
                    break;
            }

        }
    };
    //OnPageChange
    @Override
    public void onPageSelected(int i) {
        //設定Button狀態
        btn_today.setSelected(false);
        btn_month.setSelected(false);
        btn_year.setSelected(false);
        switch (i)
        {
            case 0:
                btn_today.setSelected(true);
                break;
            case 1:
                btn_month.setSelected(true);
                //取得ExpandableList
                list_month=findViewById(R.id.list_month);
                break;
            case 2:
                btn_year.setSelected(true);
                list_year=findViewById(R.id.list_year);
                break;
        }
    }
    @Override
    public void onPageScrolled(int i, float v, int i1) {

    }
    @Override
    public void onPageScrollStateChanged(int i) {

    }
    //Year倒回
    @Override
    public void onBackPressed() {
        if(viewPager.getCurrentItem()==0 && !isToday())
            viewPager.setCurrentItem(2);
        else
            super.onBackPressed();
    }
    boolean isToday()
    {
        boolean isToday=date.getDate()==intent.getIntExtra("Day",0);
        isToday&=date.getMonth()+1 == intent.getIntExtra("Month",0);
        isToday&=date.getYear()+1900==intent.getIntExtra("Year",0);
        return isToday;
    }
}
