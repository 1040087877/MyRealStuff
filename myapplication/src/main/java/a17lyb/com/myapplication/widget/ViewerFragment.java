package a17lyb.com.myapplication.widget;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

import a17lyb.com.myapplication.R;
import a17lyb.com.myapplication.ViewerActivity;
import touch.TouchImageView;

/**
 * Created by 10400 on 2016/12/23.
 */

public class ViewerFragment extends Fragment implements RequestListener<String, GlideDrawable> {
    public static final String TAG = "ViewerFragment";
    public static final String URL = "url";
    public static final String INITIAL_SHOWN = "initial_shown";
    private String mUrl;
    private TouchImageView mTouchImageView;
    private boolean mInitialShown;

    public static Fragment newInstance(String url, boolean initialShow){
        Bundle args=new Bundle();
        args.putSerializable(URL,url);
        args.putBoolean(INITIAL_SHOWN,initialShow);

        ViewerFragment fragment = new ViewerFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mUrl = getArguments().getString(URL);
        mInitialShown = getArguments().getBoolean(INITIAL_SHOWN, false);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.viewer_pager_item,container,false);
        mTouchImageView = (TouchImageView) view.findViewById(R.id.picture);
        mTouchImageView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                ((ViewerActivity)getActivity()).showImgOptDialog(mUrl);
                return true;
            }
        });
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ViewCompat.setTransitionName(mTouchImageView, mUrl);
    }

    @Override
    public void onResume() {
        super.onResume();
        Glide.with(this).load(mUrl)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .crossFade(0)
                .listener(this)
                .into(Target.SIZE_ORIGINAL,Target.SIZE_ORIGINAL);
    }

    //加载图片异常信息处理 加载失败
    @Override
    public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
        Log.e(TAG, "onException: ", e);
        return true;
    }

    @Override
    //加载成功
    public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
        mTouchImageView.setImageDrawable(resource);
        maybeStartPostponedEnterTransition();
        return true;
    }

    private void maybeStartPostponedEnterTransition() {
        if(mInitialShown){
            getActivity().supportStartPostponedEnterTransition();
        }
    }

    public View getSharedElement(){
        return mTouchImageView;
    }
}
