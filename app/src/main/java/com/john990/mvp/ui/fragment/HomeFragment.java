package com.john990.mvp.ui.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.john990.mvp.R;
import com.john990.mvp.contract.HomeContract;
import com.john990.mvp.presenter.HomePresenter;
import com.john990.mvp.ui.adapter.HomeAdapter;
import com.john990.mvp.widget.PinnedSectionListView;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by John on 16/4/11.
 */
public class HomeFragment extends BaseFragment<HomeContract.Presenter> implements HomeContract.View {

    @Bind(R.id.listView)
    PinnedSectionListView listView;
    @Bind(R.id.clock)
    TextView clock;

    private HomeAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.frag_home, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        adapter = new HomeAdapter(activity, presenter.getWifiList());
        listView.setAdapter(adapter);
    }

    @Override
    public HomePresenter createPresenter() {
        return new HomePresenter(activity, this);
    }

    @Override
    public String pageName() {
        return null;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }

    @Override
    public void refreshWifiList() {
        adapter.refresh(presenter.getWifiList());
        Toast.makeText(activity, "wifi list refreshed", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void updateClock(int second) {
        clock.setText(second + "");
    }
}
