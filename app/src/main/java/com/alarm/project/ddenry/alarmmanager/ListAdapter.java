package com.alarm.project.ddenry.alarmmanager;

import android.content.Context;
import android.content.pm.ResolveInfo;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
        return lists.size() + 1;
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
    public boolean isEnabled(int position) {
        if (position == 0) return false;
        else return true;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;

        //TODO:判断是否是Top或者Bottom显示的Item

        if (convertView == null || convertView.getTag() == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.list_item, null);
            holder = new ViewHolder();
            holder.normal = convertView.findViewById(R.id.linearLayout_normal);
            holder.signal = convertView.findViewById(R.id.linearLayout_signal);

            holder.icon = convertView.findViewById(R.id.imageView);

            holder.name = convertView.findViewById(R.id.textView_name);

            holder.show = convertView.findViewById(R.id.textView_show);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        //最后一条
        if (position == 0) {
            holder.normal.setVisibility(View.GONE);
            holder.signal.setVisibility(View.VISIBLE);
            holder.show.setText("All " + lists.size() + " installed apps found");
        } else {

            position -= 1;

            holder.normal.setVisibility(View.VISIBLE);
            holder.signal.setVisibility(View.GONE);

            holder.icon.setImageDrawable(lists.get(position).activityInfo.loadIcon(context.getPackageManager()));
            holder.name.setText(lists.get(position).loadLabel(context.getPackageManager()).toString());
        }

        return convertView;
    }

    class ViewHolder {
        ImageView icon;
        TextView name;
        LinearLayout normal;
        LinearLayout signal;
        TextView show;
    }
}
