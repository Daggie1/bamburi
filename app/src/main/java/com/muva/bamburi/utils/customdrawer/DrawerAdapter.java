package com.muva.bamburi.utils.customdrawer;

import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.muva.bamburi.R;
import com.muva.bamburi.interfaces.NavDrawerItemSelectedListener;

import java.util.ArrayList;

/**
 * Created by Njoro on 4/25/18.
 */
public class DrawerAdapter extends RecyclerView.Adapter<DrawerAdapter.DrawerViewHolder> {
    private ArrayList<NavDrawerItem> drawerItems;
    private NavDrawerItemSelectedListener listener;

    public DrawerAdapter(ArrayList<NavDrawerItem> navDrawerItems) {
        this.drawerItems = navDrawerItems;
    }


    @Override
    public DrawerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.nav_menu_item, parent, false);
        return new DrawerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(DrawerViewHolder holder, int position) {

        NavDrawerItem currentNavDrawerItem = drawerItems.get(position);
        //set menu title
        if (position == 2) {
            holder.tag.setVisibility(View.VISIBLE);
            holder.tag.setText("Community Corner");
        }
        holder.title.setText(currentNavDrawerItem.getTitle());
        holder.icon.setImageResource(currentNavDrawerItem.getIcon());
    }

    @Override
    public int getItemCount() {
        return drawerItems.size();
    }

    public void setOnNavDrawerItemClickListener(NavDrawerItemSelectedListener listener) {
        this.listener = listener;
    }

    class DrawerViewHolder extends RecyclerView.ViewHolder {
        TextView tag;
        TextView title;
        ImageView icon;
        public DrawerViewHolder(View itemView) {
            super(itemView);

            title = itemView.findViewById(R.id.title);
            icon = itemView.findViewById(R.id.icon);
            tag = itemView.findViewById(R.id.tag);

            itemView.setOnClickListener(v -> listener.onNavDrawerItemSelected(v, getAdapterPosition()));
        }
    }
}
