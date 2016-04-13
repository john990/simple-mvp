package com.john990.mvp.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.john990.mvp.R;
import com.john990.mvp.model.Wifi;
import com.john990.mvp.utils.WifiHelper;
import com.john990.mvp.widget.PinnedSectionListView;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by John on 16/4/12.
 */
public class HomeAdapter extends BaseAdapter implements PinnedSectionListView.PinnedSectionListAdapter {
    private List<Wifi> wifis;
    private Context context;

    public HomeAdapter(Context context, List<Wifi> wifis) {
        this.context = context;
        this.wifis = wifis;
    }

    @Override
    public int getCount() {
        return wifis.size();
    }

    @Override
    public Object getItem(int position) {
        return wifis.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_home_list, null);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        Wifi wifi = wifis.get(position);
        holder.ssid.setText(wifi.ssid + "  signal:" + WifiHelper.calculateSignalLevel(wifi.level, 100));
        return convertView;
    }

    @Override
    public boolean isItemViewTypePinned(int viewType) {
        return false;
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public int getItemViewType(int position) {
        return super.getItemViewType(position);
    }

    public void refresh(List<Wifi> wifis) {
        this.wifis = wifis;
        notifyDataSetChanged();
    }

    static class ViewHolder {
        @Bind(R.id.ssid)
        TextView ssid;

        ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }
}
