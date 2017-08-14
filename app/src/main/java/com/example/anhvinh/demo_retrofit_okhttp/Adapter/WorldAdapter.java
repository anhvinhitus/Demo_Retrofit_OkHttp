package com.example.anhvinh.demo_retrofit_okhttp.Adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.anhvinh.demo_retrofit_okhttp.Models.Entity.Worldpopulation;
import com.example.anhvinh.demo_retrofit_okhttp.R;
import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Created by AnhVinh on 09/08/2017.
 */

public class WorldAdapter extends RecyclerView.Adapter<WorldAdapter.WorldAdapterHolder> {

    private List<Worldpopulation> listCountry;
    Context context;

    public WorldAdapter(List<Worldpopulation> world) {
        this.listCountry = world;
    }

    public List<Worldpopulation> getListWorld() {
        return this.listCountry;
    }

    @Override
    public WorldAdapterHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View worldView = inflater.inflate(R.layout.item_country, parent, false);
        return new WorldAdapterHolder(worldView);
    }

    @Override
    public void onBindViewHolder(WorldAdapterHolder holder, final int position) {
        if (listCountry == null) return;
        holder.Display(listCountry.get(position));

        holder.setItemClickListener(new ItemClickListener() {
            @Override
            public void onClick(View v, int position, boolean longclick) {
                if (!longclick)
                    Toast.makeText(context, "Click: " + listCountry.get(position).getCountry(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        if (listCountry == null) return 0;
        else
            return listCountry.size();
    }

    public void bindData(List<Worldpopulation> listCountry) {
        this.listCountry = listCountry;
        notifyDataSetChanged();
    }

    // VIewHolder:
    public class WorldAdapterHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private ItemClickListener itemClickListener;
        // Declare Variable on one Item:
        private ImageView flag;
        private TextView rank;
        private TextView country;
        private TextView population;

        public void setItemClickListener(ItemClickListener itemClickListener) {
            this.itemClickListener = itemClickListener;
        }
        public WorldAdapterHolder(View itemView) {
            super(itemView);
            rank = (TextView) itemView.findViewById(R.id.tv_rank);
            country = (TextView) itemView.findViewById(R.id.tv_country);
            population = (TextView) itemView.findViewById(R.id.tv_population);
            flag = (ImageView) itemView.findViewById(R.id.img_flag);
            itemView.setOnClickListener(this);
        }

        public void Display(Worldpopulation worldpopulation) {
            if (worldpopulation != null) {
                rank.setText(String.valueOf(worldpopulation.getRank()));
                country.setText(worldpopulation.getCountry());
                population.setText(String.valueOf(worldpopulation.getPopulation()));
                Picasso.with(context)
                        .load(worldpopulation.getFlag())
                        .placeholder(R.mipmap.ic_launcher)
                        .into(flag);
            }
        }

        @Override
        public void onClick(View view) {
            itemClickListener.onClick(view, getPosition(), false);
        }
    }
}
