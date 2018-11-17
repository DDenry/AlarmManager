package com.alarm.project.ddenry.alarmmanager;

import android.content.Context;
import android.content.pm.ResolveInfo;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

public class ListAdapter extends BaseAdapter {

    private Context context;
    private List<ResolveInfo> lists;

    public ListAdapter(Context context, List<ResolveInfo> resolveInfos) {
        this.context = context;
        this.lists = resolveInfos;
    }

    @Override
    public int getCount() {
        return lists.size();
    }

    @Override
    public Object getItem(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;

        if (convertView == null || convertView.getTag() == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.list_item, null);
            holder = new ViewHolder();
            holder.icon = convertView.findViewById(R.id.imageView);

            holder.name = convertView.findViewById(R.id.textView_name);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.icon.setImageDrawable(lists.get(position).activityInfo.loadIcon(context.getPackageManager()));
        holder.name.setText(lists.get(position).loadLabel(context.getPackageManager()).toString());

        return convertView;
    }

    class ViewHolder {
        ImageView icon;
        TextView name;
    }
}
