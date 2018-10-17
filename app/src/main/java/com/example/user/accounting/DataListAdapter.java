package com.example.user.accounting;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.List;
import java.util.Map;

public class DataListAdapter extends BaseExpandableListAdapter {
    Context context;
    List<Map<String,String>> groups;
    List<List<Map<String,String>>> childs;
    public DataListAdapter(Context context, List<Map<String,String>> groups,List<List<Map<String,String>>> childs)
    {
        this.context=context;
        this.groups=groups;
        this.childs=childs;
    }

    @Override
    public int getGroupCount() {
        return groups.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return childs.get(groupPosition).size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return groups.get(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return childs.get(groupPosition).get(childPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        //設定Layout
        LayoutInflater infalInflater = (LayoutInflater)this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        convertView = infalInflater.inflate(R.layout.expand_group,null);
        //設定文字
        TextView group_text=convertView.findViewById(R.id.group_text);
        group_text.setText(groups.get(groupPosition).get("Group"));
        return convertView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        //設定Layout
        LayoutInflater infalInflater = (LayoutInflater)this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        convertView = infalInflater.inflate(R.layout.expand_child,null);
        //設定文字
        TextView child_text=convertView.findViewById(R.id.child_text);
        child_text.setText(childs.get(groupPosition).get(childPosition).get("Child"));
        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }
}
