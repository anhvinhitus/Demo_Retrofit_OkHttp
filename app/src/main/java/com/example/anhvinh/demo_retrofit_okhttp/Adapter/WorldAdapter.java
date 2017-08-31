package com.example.anhvinh.demo_retrofit_okhttp.Adapter;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.anhvinh.demo_retrofit_okhttp.Models.Entity.Worldpopulation;
import com.example.anhvinh.demo_retrofit_okhttp.R;
import com.example.anhvinh.demo_retrofit_okhttp.View.DetailItem.DetaiItemActivity;
import com.squareup.picasso.Picasso;

import java.util.List;

import cn.pedant.SweetAlert.SweetAlertDialog;

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
    public void onBindViewHolder(final WorldAdapterHolder holder, final int position) {
        if (listCountry == null) return;
        holder.Display(listCountry.get(position));
        holder.setItemClickListener(new ItemClickListener() {
            @Override
            public void onClick(View v, int position, boolean longclick) {
                if (!longclick) {
                    Intent intent = new Intent(context, DetaiItemActivity.class);
                    intent.putExtra("NAME", listCountry.get(position).getCountry());
                    intent.putExtra("RANK", String.valueOf(listCountry.get(position).getRank()));
                    intent.putExtra("POPULATION", listCountry.get(position).getPopulation());
                    intent.putExtra("FLAG", listCountry.get(position).getFlag());
                    context.startActivity(intent);
                } else {
//                    Toast.makeText(context, "Click: " + listCountry.get(position).getCountry(), Toast.LENGTH_SHORT).show();
                    CharSequence items[] = {"Share Image", "Rename", "Delete"};
                    AlertDialog.Builder builder = new AlertDialog.Builder(context)
                            .setTitle(context.getString(R.string.app_name))
                            .setItems(items, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    switch (which) {
                                        case 0:
                                            // Chia sẽ tệp tin ghi âm qua ứng dụng khác
                                            shareCountry(holder.getPosition());
                                            break;
                                        case 1:
                                            // Sửa tên tệp tin ghi âm.
                                            changeNameCountry(holder.getPosition());
                                            break;
                                        case 2:
                                            // Xóa tệp tin ghi âm.
                                            deleteCountry(holder.getPosition());
                                            break;
                                    }
                                }
                            });
                    builder.setCancelable(true);
                    builder.setNegativeButton("Cancel",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.cancel();
                                }
                            });
                    AlertDialog dialog = builder.create();
                    dialog.show();
                }
            }
        });
    }

    private void deleteCountry(final int position) {
        SweetAlertDialog alertDialog = new SweetAlertDialog(context, SweetAlertDialog.WARNING_TYPE);
        alertDialog.setTitleText("Are you sure?")
                .setContentText("Won't be able to recover this item!")
                .setConfirmText("Yes, delete it!")
                .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sDialog) {
                        sDialog.dismissWithAnimation();
                        listCountry.remove(position);
                        notifyItemRemoved(position);
                        notifyDataSetChanged();
                    }
                })
                .show();
    }

    private void shareCountry(int position) {
        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.setType("text/html");
        shareIntent.putExtra(android.content.Intent.EXTRA_TEXT, Html.fromHtml("<p>This is the text that will be shared.</p>"));
        context.startActivity(Intent.createChooser(shareIntent, "Send to"));
    }

    private void changeNameCountry(int position) {

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
    public class WorldAdapterHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
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
            itemView.setOnLongClickListener(this);
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

        @Override
        public boolean onLongClick(View view) {
            itemClickListener.onClick(view, getPosition(), true);
            return true;
        }
    }
}
