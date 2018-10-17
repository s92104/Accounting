package com.example.user.accounting;

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

import java.util.Date;

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
        //初始化Today
        date =new Date();
        intent=getIntent();
        intent.putExtra("Year",date.getYear()+1900);
        intent.putExtra("Month",date.getMonth()+1);
        intent.putExtra("Day",date.getDate());
        //ToolBar
        toolbar_data=findViewById(R.id.toolbar_data);
        toolbar_data.inflateMenu(R.menu.menu_data);
    }

    void setListener()
    {
        btn_today.setOnClickListener(changeFragment);
        btn_month.setOnClickListener(changeFragment);
        btn_year.setOnClickListener(changeFragment);
        viewPager.addOnPageChangeListener(this);
        setToolBarListener();
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
                    intent.putExtra("Year",date.getYear()+1900);
                    intent.putExtra("Month",date.getMonth()+1);
                    intent.putExtra("Day",date.getDate());
                    //重整
                    if(viewPager.getCurrentItem()==0)
                        viewPager.setAdapter(fragmentPagerAdapter);
                    viewPager.setCurrentItem(0);
                    break;
                case R.id.btn_month:
                    intent.putExtra("Year",date.getYear()+1900);
                    intent.putExtra("Month",date.getMonth()+1);
                    if(viewPager.getCurrentItem()==1)
                        viewPager.setAdapter(fragmentPagerAdapter);
                    viewPager.setCurrentItem(1);
                    break;
                case R.id.btn_year:
                    intent.putExtra("Year",date.getYear()+1900);
                    if(viewPager.getCurrentItem()==1)
                        viewPager.setAdapter(fragmentPagerAdapter);
                    viewPager.setCurrentItem(1);
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
            case 0: btn_today.setSelected(true);break;
            case 1: btn_month.setSelected(true);break;
            case 2: btn_year.setSelected(true);break;
        }
        if(i==0)
            setToolBarListener();
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
        if(viewPager.getCurrentItem()==0 && date.getDate()!=intent.getIntExtra("Day",0))
            viewPager.setCurrentItem(2);
        else
            super.onBackPressed();
    }
    //ActionBar
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater=getMenuInflater();
        menuInflater.inflate(R.menu.menu_data,menu);
        return super.onCreateOptionsMenu(menu);
    }
    //ToolBar
    void setToolBarListener()
    {
        //新增資料
        toolbar_data.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                switch (menuItem.getItemId())
                {
                    case R.id.it_addData:
                        startActivity(new Intent(DataActivity.this,AddDataActivity.class).putExtra("Path",path));
                        break;
                }
                return false;
            }
        });

    }
}
