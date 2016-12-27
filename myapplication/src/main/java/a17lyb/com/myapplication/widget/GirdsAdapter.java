package a17lyb.com.myapplication.widget;

import android.content.Context;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import a17lyb.com.myapplication.JavaBean.Image;
import a17lyb.com.myapplication.R;
import io.realm.Realm;
import io.realm.RealmResults;

/**
 * Created by 10400 on 2016/12/22.
 */

public class GirdsAdapter extends RecyclerView.Adapter<GirdsAdapter.MyViewHolder> {
    private static final String TAG = "GirlsAdapter";

    private final Context mContext;
    private final RealmResults<Image> mImages;
    private OnItemClickListener mOnItemClickListener;
    private int originalWidth;
    private int originalHeight;
    public GirdsAdapter(Context context, Realm realm) {
        mContext = context;
        mImages = Image.all(realm);
        //设置同一个序列返回一个view
        setHasStableIds(true);
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new MyViewHolder(LayoutInflater.from(mContext)
        .inflate(R.layout.girl_item,parent,false));
    }

    public void updateRefreshed(int numImages) {
        notifyItemRangeInserted(0, numImages);
        Log.d(TAG, "updateInsertedData: from 0 to " + numImages);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, int position) {
        Image image = mImages.get(position);
        holder.mImageView.setOriginalSize(image.getWidth(),image.getHeight());
        Glide.with(mContext)
                .load(image.getUrl())
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(holder.mImageView);
        ViewCompat.setTransitionName(holder.mImageView,image.getUrl());
        if(mOnItemClickListener!=null){
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mOnItemClickListener.onItemClick(v,holder.getLayoutPosition());
                }
            });
            holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    mOnItemClickListener.onItemLongClick(v, holder.getLayoutPosition());
                    return true;
                }
            });
        }
    }



    @Override
    public int getItemCount() {
        return mImages.size();
    }
    @Override
    public long getItemId(int position) {
        return mImages.get(position).getId().hashCode();
    }
    public String getUrlAt(int position){
        return mImages.get(position).getUrl();
    }
    public interface OnItemClickListener{
        void onItemClick(View view,int pos);
        void onItemLongClick(View view,int pos);
    }
    public void setOnItemClickListener(OnItemClickListener onItemClickListener)
    {
        mOnItemClickListener=onItemClickListener;
    }

    public class MyViewHolder extends RecyclerView.ViewHolder{

        CardView mCardView;
        RatioImageView mImageView;

        public MyViewHolder(View itemView) {
            super(itemView);
            mCardView = (CardView) itemView.findViewById(R.id.cardview);
            mImageView = (RatioImageView) itemView.findViewById(R.id.network_imageview);
        }
    }
}
