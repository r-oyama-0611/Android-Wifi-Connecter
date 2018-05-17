package roc.com.android_wifi_connecter;

import android.content.Context;
import android.graphics.Color;
import android.net.wifi.ScanResult;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class WifiAdapter extends BaseAdapter {
    List<ScanResult> scanResults;
    LayoutInflater layoutInflater;

    public WifiAdapter(Context context) {
        this.layoutInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.scanResults = new ArrayList<ScanResult>();
    }

    @Override
    public int getCount() {
        return scanResults.size();
    }

    @Override
    public Object getItem(int position) {
        return scanResults.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View view, ViewGroup viewGroup) {
        if (view == null) {
            view = layoutInflater.inflate(R.layout.wifi_item, viewGroup, false);
        }
        if (null != scanResults.get(position).SSID && !scanResults.get(position).SSID.isEmpty()) {
            ((TextView)view.findViewById(R.id.SSID)).setText(scanResults.get(position).SSID);
        } else {
            ((TextView)view.findViewById(R.id.SSID)).setText(scanResults.get(position).BSSID);
        }

        ((TextView)view.findViewById(R.id.level)).setText(String.valueOf(scanResults.get(position).level));

        LinearLayout layout = view.findViewById(R.id.wifi_item_layout);

        if (position % 2 == 0) {
            layout.setBackgroundColor(Color.rgb(0xfa,0xfa,0xfa));
        } else {
            layout.setBackgroundColor(Color.rgb(0xf5,0xf5,0xf5));
        }

        return view;
    }
}
