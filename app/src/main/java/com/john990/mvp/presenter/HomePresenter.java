package com.john990.mvp.presenter;

import android.content.Context;
import android.os.Handler;
import android.widget.Toast;

import com.john990.mvp.contract.HomeContract;
import com.john990.mvp.model.Wifi;
import com.john990.mvp.utils.WifiHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by John on 16/4/9.
 */
public class HomePresenter implements HomeContract.Presenter {

    private List<Wifi> wifis;
    private Context context;
    private HomeContract.View homeView;
    private Handler handler = new Handler();
    private RefreshWifiTask refreshWifiTask = new RefreshWifiTask();
    private int index = 1;

    public HomePresenter(Context context, HomeContract.View homeView) {
        this.context = context;
        this.homeView = homeView;
    }

    @Override
    public void resume() {
        handler.postDelayed(refreshWifiTask, 1000);
    }

    @Override
    public void pause() {
        handler.removeCallbacksAndMessages(null);
    }

    @Override
    public void destroy() {
    }

    @Override
    public List<Wifi> getWifiList() {
        wifis = WifiHelper.getAllWifiListIncludeActive();
        if (wifis == null) {
            wifis = new ArrayList<>();
        }
        return wifis;
    }

    @Override
    public void saveWifi(int position) {
        wifis.get(position).save();
        Toast.makeText(context, "save success", Toast.LENGTH_SHORT).show();
    }

    private class RefreshWifiTask implements Runnable {

        @Override
        public void run() {
            if (index % 10 == 0) {
                homeView.refreshWifiList();
            }
            homeView.updateClock(10 - index % 10);
            index++;
            handler.postDelayed(this, 1000);
        }
    }
}
