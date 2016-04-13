package com.john990.mvp.ui.fragment;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.View;

import com.john990.mvp.contract.BasePresenter;
import com.john990.mvp.contract.BaseView;
import com.john990.mvp.ui.activity.BaseActivity;
import com.john990.mvp.utils.CommonHandler;

/**
 * Created by John on 16/4/10.
 */
public abstract class BaseFragment<T extends BasePresenter> extends Fragment implements BaseView<T> {
    private static final String TAG = "CommonFragment";
    public BaseActivity activity;

    protected T presenter;

    private CommonHandler handler = new CommonHandler(new EmptyHandler());

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.activity = (BaseActivity) activity;
        setHasOptionsMenu(true);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        presenter = createPresenter();
    }

    public abstract String pageName();

    @Override
    public void onDetach() {
        super.onDetach();
    }

    public void finish() {
        activity.finish();
    }

    /**
     * true：截断activity操作
     *
     * @return
     */
    public boolean onBackPressed() {
        return false;
    }


    public void post(Runnable runnable) {
        handler.post(runnable);
    }

    public void postDelay(Runnable runnable, long time) {
        handler.postDelayed(runnable, time);
    }

    public void removeCallback(Runnable runnable) {
        if (runnable != null) {
            handler.removeCallbacks(runnable);
        }
    }

    @Override
    public void onDestroy() {
        handler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (presenter != null) {
            presenter.resume();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (presenter != null) {
            presenter.pause();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (presenter != null) {
            presenter.destroy();
        }
    }

    private class EmptyHandler implements CommonHandler.MessageHandler {

        @Override
        public void handleMessage(Message msg) {
        }
    }

    public void onNewIntent(Intent intent) {
    }
}
