package a17lyb.com.myapplication.Base;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import a17lyb.com.myapplication.JavaBean.Stuff;
import a17lyb.com.myapplication.R;
import a17lyb.com.myapplication.Utils.DateUtil;
import io.realm.Realm;
import io.realm.RealmResults;

/**
 * Created by 10400 on 2016/12/24.
 */

public abstract class StuffBaseAdapter extends RecyclerView.Adapter<StuffBaseAdapter.ViewHolder> {

    private Context mContext;
    protected Realm mRealm;
    protected String mType;
    protected RealmResults<Stuff> mStuffs;
    protected int mLastStuffsNum;

    public StuffBaseAdapter(Context context, Realm realm, String type) {
        mContext = context;
        mRealm = realm;
        mType = type;
        initStuffs(mRealm, mType);
        mLastStuffsNum = mStuffs.size();
        setHasStableIds(true);

    }

    protected abstract void initStuffs(Realm realm, String type);


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(mContext).inflate(R.layout.stuff_item, parent, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        Stuff stuff = mStuffs.get(position);
        holder.source.setText(stuff.getWho());
        holder.title.setText(stuff.getDesc());
        holder.date.setText(DateUtil.format(stuff.getPublishedAt()));
        holder.stuff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mOnItemClickListener != null) {
                    mOnItemClickListener.onItemClick(v, position);
                }
            }
        });
        holder.stuff.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (mOnItemClickListener != null) {
                    return mOnItemClickListener.onItemLongClick(v, position);
                }
                return false;
            }
        });
        bindColBtn(holder.likeBtn, position);
    }

    protected abstract void bindColBtn(ImageView likeBtn, int position);

    @Override
    public long getItemId(int position) {
        //返回同一个对象，否则会出错
        return mStuffs.get(position).getId().hashCode();
    }

    @Override
    public int getItemCount() {
        return mStuffs.size();
    }

    public <T extends View> T $(View view, int resId) {
        return (T) view.findViewById(resId);
    }

    public Stuff getStuffAt(int pos) {
        return mStuffs.get(pos);
    }

    //回调接口
    private OnItemClickListener mOnItemClickListener;

    public interface OnItemClickListener {
        boolean onItemLongClick(View v, int position);

        void onItemClick(View v, int position);
    }

    public void setOnItemClickListerner(OnItemClickListener onItemClickListener) {
        mOnItemClickListener = onItemClickListener;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView title, source, date;
        LinearLayout stuff;
        ImageView likeBtn;

        //因为这是基类，获取不到数据，必须用回调
        public ViewHolder(View itemView) {
            super(itemView);
            this.title = $(itemView, R.id.stuff_title);
            this.source = $(itemView, R.id.stuff_author);
            this.date = $(itemView, R.id.stuff_date);
            this.stuff = $(itemView, R.id.stuff);
            this.likeBtn = $(itemView, R.id.like_btn);
        }
    }
}
