package com.john990.mvp.contract;

import com.john990.mvp.model.Wifi;

import java.util.List;

/**
 * Created by John on 16/4/8.
 */
public interface HomeContract {
    interface View extends BaseView<Presenter> {
        void refreshWifiList();

        void updateClock(int second);
    }

    interface Presenter extends BasePresenter {
        List<Wifi> getWifiList();

        void saveWifi(int position);
    }
}
