package a17lyb.com.myapplication.widget;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;

import a17lyb.com.myapplication.Base.StuffBaseAdapter;
import a17lyb.com.myapplication.Base.StuffBaseFragment;
import a17lyb.com.myapplication.R;
import a17lyb.com.myapplication.Services.StuffFetchService;
import a17lyb.com.myapplication.Utils.CommonUtil;
import a17lyb.com.myapplication.Utils.Constants;

/**
 * Created by 10400 on 2016/12/24.
 */

public class StuffFragment extends StuffBaseFragment{
    private static final String TAG = "StuffFragment";
    private static final String TYPE = "type";
    public static final String SERVICE_TYPE = "service_type";

    private String mType;
    private UpdateResultReceiver mUpdateResultReceiver;

    public static StuffFragment newInstance(String type) {
        Bundle args = new Bundle();
        args.putString(TYPE,type);
        StuffFragment fragment = new StuffFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected void initData() {
        super.initData();
        mType = getArguments().getString(TYPE);
        mUpdateResultReceiver = new UpdateResultReceiver();
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume: " + mType);
        mLocalBroadcastManager.registerReceiver(mUpdateResultReceiver,
                new IntentFilter(StuffFetchService.ACTION_UPDATE_RESULT));
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG, "onPause: " + mType);
        mLocalBroadcastManager.unregisterReceiver(mUpdateResultReceiver);
    }

    @Override
    protected void fetchLatest() {
        if(mIsRefreshing){
            return;
        }
        Intent intent = new Intent(getActivity(), StuffFetchService.class);
        intent.setAction(StuffFetchService.ACTION_FETCH_REFRESH)
                .putExtra(SERVICE_TYPE,mType);
        getActivity().startService(intent);

        mIsRefreshing=true;
        setRefreshLayout(true);
    }

    @Override
    protected RecyclerView.Adapter initAdapter() {
        final StuffAdapter stuffAdapter = new StuffAdapter(getActivity(), mRealm, mType);
        stuffAdapter.setOnItemClickListerner(new StuffBaseAdapter.OnItemClickListener() {
            @Override
            public boolean onItemLongClick(View v, int position) {
                if(mIsLoadingMore || mIsRefreshing){
                    return true;
                }
                getActivity().startActionMode(new ShareListener(getActivity(),stuffAdapter.getStuffAt(position),v));
                return true;
            }

            @Override
            public void onItemClick(View v, int position) {
                if(mIsLoadingMore || mIsRefreshing){
                    return;
                }
                CommonUtil.openUrl(getActivity(),stuffAdapter.getStuffAt(position).getUrl());
            }
        });
        return stuffAdapter;
    }

    @Override
    //加载更多
    protected void loadingMore() {
        if(mIsLoadingMore){
            return;
        }
        Intent intent = new Intent(getActivity(), StuffFetchService.class);
        intent.setAction(StuffFetchService.ACTION_FETCH_MORE)
                .putExtra(SERVICE_TYPE,mType);
        getActivity().startService(intent);

        mIsLoadingMore=true;
        setRefreshLayout(true);
    }


    private class UpdateResultReceiver extends BroadcastReceiver{
        //拿到数据
        @Override
        public void onReceive(Context context, Intent intent) {
            CommonUtil.makeSnackBar(mRefreshLayout,"广播收到",Snackbar.LENGTH_SHORT);
            int fetched = intent.getIntExtra(StuffFetchService.EXTRA_FETCHED, 0);
            String trigger = intent.getStringExtra(StuffFetchService.EXTRA_TRIGGER);
            String type = intent.getStringExtra(StuffFetchService.EXTRA_TYPE);
            Constants.NETWORK_EXCEPTION networkException = (Constants.NETWORK_EXCEPTION) intent.getSerializableExtra(StuffFetchService.EXTRA_EXCEPTION_CODE);
            if(!type.equals(mType)){
                return;
            }
            Log.d(TAG, "fetched " + fetched + ", triggered by " + trigger);
            if(fetched==0 && trigger.equals(StuffFetchService.ACTION_FETCH_MORE)){
                CommonUtil.makeSnackBar(mRefreshLayout,"没有更多了", Snackbar.LENGTH_SHORT);
                mIsNoMore=true;
            }
            setRefreshLayout(false);

            if(networkException.getTipsResId()!=0){
                // 显示异常提示
                CommonUtil.makeSnackBar(mRefreshLayout, getString(networkException.getTipsResId()), Snackbar.LENGTH_SHORT);
                setFetchingFlagsFalse();
                return;
            }
            if(mIsRefreshing){
                CommonUtil.makeSnackBar(mRefreshLayout, getString(R.string.fragment_refreshed), Snackbar.LENGTH_SHORT);
                mRecyclerView.smoothScrollToPosition(0);
            }
            setFetchingFlagsFalse();

            if(null==mAdapter || fetched==0){
                return;
            }
            ((StuffAdapter)mAdapter).updateInsertedData(fetched,trigger.equals(StuffFetchService.ACTION_FETCH_MORE));
        }
    }
}
