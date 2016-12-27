package a17lyb.com.myapplication.widget;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;

import a17lyb.com.myapplication.Base.StuffBaseAdapter;
import a17lyb.com.myapplication.JavaBean.Stuff;
import a17lyb.com.myapplication.R;
import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;

/**
 * Created by 10400 on 2016/12/25.
 */

public class CollectionAdaper extends StuffBaseAdapter{
    private static final String TAG = "CollectionAdapter";

    public CollectionAdaper(Context context, Realm realm, String type) {
        super(context, realm, type);
        //一有改变就更新
        mStuffs.addChangeListener(new RealmChangeListener<RealmResults<Stuff>>() {
            @Override
            public void onChange(RealmResults<Stuff> element) {
                notifyDataSetChanged();
            }
        });
    }



    @Override
    protected void initStuffs(Realm realm, String type) {
        //找到所有喜欢的数据
        mStuffs=Stuff.collections(realm);
    }

    @Override
    protected void bindColBtn(ImageView likeBtn, final int position) {
        likeBtn.setImageResource(R.drawable.like);
        likeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //不喜欢就删除掉
                deleteItem(position);
            }
        });
    }

    private void deleteItem(final int position) {
        mRealm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                Stuff stuff = mStuffs.get(position);
                if(stuff.isDeleted()){
                    stuff.deleteFromRealm();
                }else {
                    stuff.setLiked(false);
                }
            }
        });
    }
}
