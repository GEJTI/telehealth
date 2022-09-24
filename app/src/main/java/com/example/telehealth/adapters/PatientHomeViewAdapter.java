package com.example.telehealth.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.telehealth.DoctorListActivity;
import com.example.telehealth.R;
import com.example.telehealth.models.PatientHomeData;

import java.util.ArrayList;

public class PatientHomeViewAdapter extends RecyclerView.Adapter<PatientHomeViewAdapter.ViewHolder> {

    private ArrayList<PatientHomeData> list;
    private Context context;
    public String ctg = null;

    public PatientHomeViewAdapter(Context context,ArrayList<PatientHomeData> list){
        this.context = context;
        this.list = list;
    }

    @Override
    public PatientHomeViewAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.patient_home_single_view, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final PatientHomeViewAdapter.ViewHolder holder, int position) {
        holder.category.setText(list.get(position).getCategory());
        holder.ctgImage.setImageResource(list.get(position).getImageId());

        final String ctg = list.get(position).getCategory();

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(context, DoctorListActivity.class);
                i.putExtra("doctor_specialization",ctg);
                i.putExtra("fromActivity", "PatientHomeActivity");
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(i);
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        private TextView category;
        private ImageView ctgImage;
        public ViewHolder(View view) {
            super(view);

            category = (TextView)view.findViewById(R.id.patient_ctg_txt);
            ctgImage = (ImageView) view.findViewById(R.id.patient_ctg_img);
        }
    }
}
