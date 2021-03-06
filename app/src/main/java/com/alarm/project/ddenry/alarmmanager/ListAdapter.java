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
import java.util.Locale;

public class ListAdapter extends BaseAdapter {

    private Context context;
    private List<ResolveInfo> lists;

    public ListAdapter(Context context, List<ResolveInfo> resolveInfos) {
        this.context = context;
        this.lists = resolveInfos;
    }

    @Override
    public int getCount() {
        return lists.size() + Config.EXTRA_ITEM_COUNT;
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

        //列表第一条
        if (position <= Config.EXTRA_ITEM_COUNT - 1) {
            holder.normal.setVisibility(View.GONE);
            holder.signal.setVisibility(View.VISIBLE);
            holder.show.setText(String.format(Locale.CHINA, "%d %s", lists.size(), context.getResources().getString(R.string.installed_apps)));
        } else {
            position -= Config.EXTRA_ITEM_COUNT;
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
