package a17lyb.com.myapplication.Base;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import io.realm.Realm;

/**
 * Created by 10400 on 2016/12/21.
 */

public abstract class BaseFragment extends Fragment {

    protected SwipeRefreshLayout mRefreshLayout;
    protected RecyclerView mRecyclerView;
    protected RecyclerView.LayoutManager mMLayoutManager;
    protected RecyclerView.Adapter mAdapter;
    protected Realm mRealm;
    protected boolean mIsLoadingMore;
    protected boolean mIsNoMore;
    protected LocalBroadcastManager mLocalBroadcastManager;
    protected boolean mIsRefreshing;
    private String TAG="BaseFragment";
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initData();
    }

    protected void initData() {
        mRealm = Realm.getDefaultInstance();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(getLayoutResId(), container, false);
        mRefreshLayout = $(view, getRefreshLayoutId());
        mRecyclerView = $(view, getRecyclerViewId());
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mMLayoutManager = getLayoutManager();
        mRecyclerView.setLayoutManager(mMLayoutManager);
        mAdapter = initAdapter();
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
//                Log.e(TAG, "onScrolled: "+dx+"|||| dy"+dy);
                if(!mIsLoadingMore && dy>0){
                    int lastVisiblePos=getLastVisiblePos();
                    Log.e(TAG, "mIsNoMore"+mIsNoMore);
                    if(!mIsNoMore && lastVisiblePos+1== mAdapter.getItemCount()){
                        loadingMore();
                    }

                }
            }
        });
        mLocalBroadcastManager = LocalBroadcastManager.getInstance(getActivity());

        return view;
    }

    //滑动到底部
    protected abstract int getLastVisiblePos();

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mRefreshLayout.setColorSchemeResources(android.R.color.holo_blue_light, android.R.color.holo_red_light, android.R.color.holo_orange_light, android.R.color.holo_green_light);
        //首次进入进行刷新
        SwipeRefreshLayout.OnRefreshListener onRefreshListener = new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                fetchLatest();
            }
        };
        mRefreshLayout.setOnRefreshListener(onRefreshListener);
        //首次进入进行刷新
        if(savedInstanceState==null){
            onRefreshListener.onRefresh();
        }

    }
    public void setRefreshLayout(final boolean state){
        if(mRefreshLayout==null){
            return;
        }
        mRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                mRefreshLayout.setRefreshing(state);
            }
        });
    }
    public void setFetchingFlagsFalse() {
        if (mIsRefreshing)
            mIsRefreshing = false;
        if (mIsLoadingMore)
            mIsLoadingMore = false;
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        mRealm.removeAllChangeListeners();;
        mRealm.close();
    }
    public void smoothScrollToTop(){
        if(mMLayoutManager!=null){
            mMLayoutManager.smoothScrollToPosition(mRecyclerView,null,0);
        }
    }
    public boolean isFetching() {
        return mIsLoadingMore || mIsRefreshing;
    }
    protected abstract void fetchLatest();

    protected abstract RecyclerView.Adapter initAdapter();

    protected abstract RecyclerView.LayoutManager getLayoutManager();

    protected abstract int getRecyclerViewId();

    protected abstract int getRefreshLayoutId();
    protected abstract void loadingMore();


    protected <T extends View> T $(View v,int resId){
        return (T) v.findViewById(resId);
    }

    public abstract int getLayoutResId();

    public void updateData() {
        if(mAdapter==null){
            return;
        }
        mAdapter.notifyDataSetChanged();
    }
}
