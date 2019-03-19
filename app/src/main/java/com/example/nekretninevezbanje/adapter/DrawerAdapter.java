package com.example.nekretninevezbanje.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.nekretninevezbanje.R;
import com.example.nekretninevezbanje.model.NavigationItem;

import java.util.ArrayList;

public class DrawerAdapter extends BaseAdapter {

    private Context context;
    private ArrayList<NavigationItem> navigationItems = new ArrayList<>();

    public DrawerAdapter(Context context, ArrayList<NavigationItem> navigationItems) {
        this.context = context;
        this.navigationItems = navigationItems;
    }

    @Override
    public int getCount() {
        return navigationItems.size();
    }

    @Override
    public Object getItem(int position) {
        return navigationItems;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        convertView = layoutInflater.inflate(R.layout.drawer_item, null);

        TextView title = convertView.findViewById(R.id.drawer_title);
        TextView subTitle = convertView.findViewById(R.id.drawer_subTitle);
        ImageView icon = convertView.findViewById(R.id.drawer_icon);

        title.setText(navigationItems.get(position).getTitle());
        subTitle.setText(navigationItems.get(position).getSubTitle());
        icon.setImageResource(navigationItems.get(position).getIcon());

        return convertView;
    }
}
