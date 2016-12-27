package a17lyb.com.myapplication.widget;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;

import java.util.Date;

import a17lyb.com.myapplication.Base.StuffBaseAdapter;
import a17lyb.com.myapplication.JavaBean.Stuff;
import a17lyb.com.myapplication.R;
import io.realm.Realm;

/**
 * Created by 10400 on 2016/12/24.
 */

public class StuffAdapter extends StuffBaseAdapter {
    private static final String TAG = "StuffAdapter";

    public StuffAdapter(Context context, Realm realm, String type) {
        super(context, realm, type);
    }

    @Override
    protected void initStuffs(Realm realm, String type) {
        mStuffs = Stuff.all(realm, type);
    }

    @Override
    protected void bindColBtn(ImageView likeBtn, final int position) {
        likeBtn.setTag(position);
        likeBtn.setImageResource(mStuffs.get(position).isLiked()? R.drawable.like:R.drawable.unlike);
        likeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleLikeBtn((ImageView)v,position);
            }
        });
    }

    private void toggleLikeBtn(ImageView likeBtn, int position) {
        if(mStuffs.get(position).isLiked()){
            likeBtn.setImageResource(R.drawable.unlike);
            changeLiked(position,false);
        }else {
            likeBtn.setImageResource(R.drawable.like);
            changeLiked(position,true);
        }

    }

    public void updateInsertedData(int numImages, boolean isMore) {
        if (isMore)
            notifyItemRangeInserted(mLastStuffsNum, numImages);
        else
            notifyItemRangeInserted(0, numImages);
        mLastStuffsNum += numImages;
    }
    private void changeLiked(final int position, final boolean isLiked) {
        mRealm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                Stuff stuff = mStuffs.get(position);
                stuff.setLiked(isLiked);
                stuff.setLastChanged(new Date());
            }
        });
        notifyItemChanged(position);
    }

}
