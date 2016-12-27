package a17lyb.com.myapplication.widget;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.view.View;

import a17lyb.com.myapplication.Base.BaseFragment;
import a17lyb.com.myapplication.Net.ImageFetchService;
import a17lyb.com.myapplication.R;
import a17lyb.com.myapplication.Utils.CommonUtil;
import a17lyb.com.myapplication.Utils.Constants;
import a17lyb.com.myapplication.ViewerActivity;

/**
 * Created by 10400 on 2016/12/21.
 */

public class GirlsFragment extends BaseFragment {
    public static final String TAG = "GirlsFragment";
    public static final String POSTION = "viewer_position";
    private static final String TYPE = "girls_type";
    private final UpdateResultReceiver updateResultReceiver = new UpdateResultReceiver();
    private String mType;
    private GirdsAdapter mGirdsAdapter;

    @Override
    protected int getLastVisiblePos() {
        StaggeredGridLayoutManager layoutManager = (StaggeredGridLayoutManager) mMLayoutManager;
        int[] lastVisibleItemPositions = layoutManager.findLastVisibleItemPositions(new int[layoutManager.getSpanCount()]);
        return getMaxPosition(lastVisibleItemPositions);
    }

    private int getMaxPosition(int[] lastVisibleItemPositions) {
        int maxPosition=0;
        int size=lastVisibleItemPositions.length;
        for (int i = 0; i < size; i++) {
            maxPosition = Math.max(maxPosition, lastVisibleItemPositions[i]);
        }
        return maxPosition;
    }

    public static GirlsFragment newInstance(String type) {
        Bundle bundle = new Bundle();
        bundle.putString(TYPE, type);

        GirlsFragment girlsFragment = new GirlsFragment();
        girlsFragment.setArguments(bundle);
        return girlsFragment;
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume: ");
        mLocalBroadcastManager.registerReceiver(updateResultReceiver,
                new IntentFilter(ImageFetchService.ACTION_UPDATE_RESULT));
    }


    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG, "onPause: ");
        mLocalBroadcastManager.unregisterReceiver(updateResultReceiver);
    }

    @Override
    protected void initData() {
        super.initData();
        mType = getArguments().getString(TYPE);
    }

    @Override
    protected RecyclerView.Adapter initAdapter() {
        mGirdsAdapter = new GirdsAdapter(getActivity(), mRealm);
        mGirdsAdapter.setOnItemClickListener(new GirdsAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int pos) {
                //正在刷新，请稍后
                if(mIsLoadingMore || mIsRefreshing){
                    CommonUtil.makeSnackBar(mRefreshLayout, getString(R.string.fetching_pic), Snackbar.LENGTH_LONG);
                    return;
                }
                //点开跳转到图片activity
                Intent intent = new Intent(getActivity(), ViewerActivity.class);
                intent.putExtra(POSTION,pos);
                Bundle bundle = ActivityOptionsCompat.makeSceneTransitionAnimation(getActivity(),
                        view.findViewById(R.id.network_imageview), mGirdsAdapter.getUrlAt(pos)).toBundle();
                getActivity().startActivity(intent,bundle);
            }

            @Override
            public void onItemLongClick(View view, int pos) {
                CommonUtil.makeSnackBar(mRefreshLayout, pos + getString(R.string.fragment_long_clicked), Snackbar.LENGTH_SHORT);

            }
        });
        return mGirdsAdapter;
    }

    @Override
    protected RecyclerView.LayoutManager getLayoutManager() {
        return new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
    }

    @Override
    protected int getRecyclerViewId() {
        return R.id.girls_recyclerview_id;
    }

    @Override
    protected int getRefreshLayoutId() {
        return R.id.swipe_refresh_layout;
    }

    //    }
//        });
//            }
//                return true;
//                getActivity().supportStartPostponedEnterTransition();
//                mRecyclerView.getViewTreeObserver().removeOnPreDrawListener(this);
//            public boolean onPreDraw() {
//            @Override
//        mRecyclerView.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
//    public void onActivityReenter(final int index) {

    @Override
    //刷新数据
    protected void fetchLatest() {
        if(mIsRefreshing){
            return;
        }
        Intent intent = new Intent(getActivity(), ImageFetchService.class);
        intent.setAction(ImageFetchService.ACTION_FETCH_REFRESH);
        getActivity().startService(intent);
        mIsRefreshing=true;
        setRefreshLayout(true);

    }

    @Override
    //加载更多数据
    protected void loadingMore() {
        //正在更新 不做处理
        if (mIsLoadingMore) {
            return;
        }
        Intent intent = new Intent(getActivity(), ImageFetchService.class);
        intent.setAction(ImageFetchService.ACTION_FETCH_MORE);
        getActivity().startService(intent);

        mIsLoadingMore = true;
        setRefreshLayout(true);
    }

    @Override
    public int getLayoutResId() {
        return R.layout.girls_fragment;
    }

    private class UpdateResultReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            CommonUtil.makeSnackBar(mRefreshLayout,"广播收到",Snackbar.LENGTH_SHORT);
            //返回多少个条目 个数
            final int fetched = intent.getIntExtra(ImageFetchService.EXTRA_FETCHED,0);
            String trigger = intent.getStringExtra(ImageFetchService.EXTRA_TRIGGER);
            final Constants.NETWORK_EXCEPTION networkException = (Constants.NETWORK_EXCEPTION) intent.getSerializableExtra(ImageFetchService.EXTRA_EXCEPTION_CODE);
            Log.d(TAG, "fetched " + fetched + ", triggered by " + trigger);
            setRefreshLayout(false);
            if(networkException.getTipsResId()!=0){
                CommonUtil.makeSnackBar(mRefreshLayout,getString(networkException.getTipsResId()),Snackbar.LENGTH_SHORT);
                setFetchingFlagsFalse();
                return;
            }
            if (mIsRefreshing) {
                CommonUtil.makeSnackBar(mRefreshLayout, "已更新", Snackbar.LENGTH_SHORT);
                if (fetched>0){
                    ((GirdsAdapter) mAdapter).updateRefreshed(fetched);
                    mRecyclerView.smoothScrollToPosition(0);
                }
            }
            setFetchingFlagsFalse();
        }
    }


}
