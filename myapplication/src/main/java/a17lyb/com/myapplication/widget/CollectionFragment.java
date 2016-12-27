package a17lyb.com.myapplication.widget;

import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import a17lyb.com.myapplication.Base.StuffBaseAdapter;
import a17lyb.com.myapplication.Base.StuffBaseFragment;
import a17lyb.com.myapplication.Utils.CommonUtil;

/**
 * Created by 10400 on 2016/12/25.
 */

public class CollectionFragment extends StuffBaseFragment{
    private static final String TAG = "CollectionFragment";
    private static final String TYPE = "col_type";
    private String mType;

    public static CollectionFragment newInstance(String type) {
        Bundle args = new Bundle();
        args.putString(TYPE,type);
        CollectionFragment fragment = new CollectionFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected void initData() {
        super.initData();
        mType = getArguments().getString(TYPE);
    }

    @Override
    protected RecyclerView.Adapter initAdapter() {
        final CollectionAdaper adapter = new CollectionAdaper(getActivity(), mRealm, mType);
        adapter.setOnItemClickListerner(new StuffBaseAdapter.OnItemClickListener() {
            @Override
            public boolean onItemLongClick(View v, int position) {
                if (mIsLoadingMore || mIsRefreshing)
                    return true;
                getActivity().startActionMode(new StuffFragment.ShareListener(getActivity()
                ,adapter.getStuffAt(position),v));
                return true;
            }

            @Override
            public void onItemClick(View v, int position) {
                if (mIsLoadingMore || mIsRefreshing)
                    return;
                CommonUtil.openUrl(getActivity(), adapter.getStuffAt(position).getUrl());
            }
        });
        return adapter;
    }

    @Override
    protected void fetchLatest() {
        setRefreshLayout(false);
    }

    @Override
    protected void loadingMore() {
        return;
    }
}
