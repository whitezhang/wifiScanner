package com.example.wyatt.myapplication;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

/**
 * Created by wyatt on 1/10/16.
 */
public class ScanResultsAdapter extends BaseAdapter {
    private Context context;
    private List<ScanResult> results;

    public ScanResultsAdapter(Context context, List<ScanResult> results) {
        this.context = context;
        this.results = results;
    }

    public void setScanResult(List<ScanResult> results) {
        this.results = results;
    }

    public int getCount() {
        return results.size() > 10 ? 10 : results.size();
    }

    public Object getItem(int position) {
        return results.get(position);
    }

    public long getItemId(int position) {
        return position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        ScanResult result = results.get(position);

        if(convertView == null) {
            LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.network_list_row, null);
        }

        TextView txtSSID = (TextView)convertView.findViewById(R.id.txtSSID);
        TextView txtMSG = (TextView)convertView.findViewById(R.id.txtMsg);
        TextView txtLevel = (TextView)convertView.findViewById(R.id.txtLevel);

        txtSSID.setText("ssid:" + result.SSID);
        txtMSG.setText("Msg:" + result.BSSID);
        txtLevel.setText("sigLevel:" + Integer.toString(result.level));

        return convertView;
    }
}
