package com.example.weatherforecastapp;

import android.content.Context;
import android.icu.number.CompactNotation;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class WeatherAdapter extends RecyclerView.Adapter<WeatherAdapter.ViewHolder> {

    private Context context;
    private ArrayList<WeatherModel>weatherModalArrayList;

    public WeatherAdapter(Context context, ArrayList<WeatherModel> weatherModalArrayList) {
        this.context = context;
        this.weatherModalArrayList = weatherModalArrayList;
    }

    @Override
    public WeatherAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.weather_item,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull WeatherAdapter.ViewHolder holder, int position) {
        WeatherModel model =weatherModalArrayList.get(position);
        holder.temperatureTv.setText(model.getTemperature()+"°C");
        Picasso.get().load("https:".concat(model.getIcon())).into(holder.condition);
        holder.windTV.setText(model.getWindSpeed()+"km/h");
        SimpleDateFormat input = new SimpleDateFormat("yyyy-MM-dd hh:mm");
        SimpleDateFormat output = new SimpleDateFormat("hh:mm aa");
        try{
            Date t = input.parse(model.getTime());
            holder.timeTv.setText(output.format(t));
        }catch (ParseException e){
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        return weatherModalArrayList.size();
    }
    public class ViewHolder extends RecyclerView.ViewHolder{
        private TextView windTV ,temperatureTv,timeTv;
        private ImageView condition;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            windTV = itemView.findViewById(R.id.idTVWindSpeed);
            temperatureTv = itemView.findViewById(R.id.idTVTemperature);
            timeTv = itemView.findViewById(R.id.idTVTime);
            condition = itemView.findViewById(R.id.idTVCondition );
        }
    }
}
