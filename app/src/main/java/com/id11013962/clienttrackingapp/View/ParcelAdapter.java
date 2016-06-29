package com.id11013962.clienttrackingapp.View;

import android.content.Context;
import android.os.Build;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.id11013962.clienttrackingapp.Model.DbParcelInfoDataModel;
import com.id11013962.clienttrackingapp.R;

import java.util.ArrayList;
import java.util.Objects;

/**
 * Adapter for the Recycler View. Each Parcel view binds
 */
public class ParcelAdapter extends RecyclerView.Adapter<ParcelAdapter.ViewHolder> {
    private Context mContext;
    private ArrayList<DbParcelInfoDataModel> mParcelList;

    /**
     * Gets the context and parcelList.
     */
    public ParcelAdapter (Context context, ArrayList parcelList){
        this.mContext = context;
        this.mParcelList = parcelList;
    }

    /**
     * inflate each adapter parcel item
     */
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(mContext)
                .inflate(R.layout.adapter_parcel_item, parent, false);
        return new ViewHolder(itemView);
    }

    /**
     * Binds each item to be displayed on the recycler view
     */
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        DbParcelInfoDataModel dbData = mParcelList.get(position);
        holder.mParcelNumber.setText(dbData.getParcelNumber());
        holder.mFullName.setText(String.valueOf(dbData.getFullName()));
        holder.mParcelStatus.setText(dbData.getStatus());
        String status = dbData.getStatus();

        // Check if status is delivered to set the background color.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            if (Objects.equals(status, "Delivered")){
                holder.mLinearLayoutStatus.setBackgroundColor(mContext.getResources().getColor(R.color.green));
            }else {
                holder.mLinearLayoutStatus.setBackgroundColor(mContext.getResources().getColor(R.color.colorGrey));
            }
        }
    }

    @Override
    public int getItemCount() {
        return mParcelList.size();
    }

    /**
     * Custom ViewHolder gets all the required attributes
     */
    public class ViewHolder extends RecyclerView.ViewHolder{
        public TextView mParcelNumber;
        public TextView mFullName;
        public TextView mParcelStatus;
        public LinearLayout mLinearLayoutStatus;

        public ViewHolder(View view) {
            super(view);
            mParcelNumber = (TextView) view.findViewById(R.id.adapter_parcel_number);
            mFullName = (TextView) view.findViewById(R.id.adapter_parcel_full_name);
            mParcelStatus = (TextView) view.findViewById(R.id.adapter_parcel_status);
            mLinearLayoutStatus = (LinearLayout) view.findViewById(R.id.adapter_linear_status_layout);
        }
    }
}
