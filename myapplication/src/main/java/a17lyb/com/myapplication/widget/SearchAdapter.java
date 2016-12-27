package a17lyb.com.myapplication.widget;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import a17lyb.com.myapplication.JavaBean.Stuff;
import a17lyb.com.myapplication.R;
import a17lyb.com.myapplication.Utils.CommonUtil;
import a17lyb.com.myapplication.Utils.DateUtil;
import a17lyb.com.myapplication.db.SearchBean;
import io.realm.Realm;

/**
 * Created by 10400 on 2016/12/26.
 */

public class SearchAdapter extends RecyclerView.Adapter<SearchAdapter.ViewHolder>{
    private static final String TAG = "SearchAdapter";
    private Context mContext;
    private Realm realm;
    private List<SearchBean> mSearchBeens;
    private OnItemClickListener mOnItemClickListener;

    public SearchAdapter(Context context, Realm realm) {
        mContext = context;
        this.realm = realm;
        mSearchBeens = new ArrayList<>();

    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(mContext)
        .inflate(R.layout.search_item,parent,false));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        final SearchBean searchBean = mSearchBeens.get(position);
        holder.author.setText(searchBean.getWho());
        holder.title.setText(searchBean.getDesc());
        try {
            //时间转化
            holder.date.setText(DateUtil.formatSearchDate(searchBean.getPublishedAt()));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        if(mOnItemClickListener!=null){
            holder.stuff.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mOnItemClickListener.onItemClick(v,position);
                }
            });
            holder.stuff.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    mOnItemClickListener.onItemLongClick(v,position);
                    return true;
                }
            });
        }

        //检测wifi网络是否连接
        if(CommonUtil.isWifiConnected(mContext) && !searchBean.getReadability().equals("")){
            holder.webView.setVisibility(View.VISIBLE);
            holder.webView.setTag(position);
            holder.webView.getSettings().setUseWideViewPort(true);
            holder.webView.getSettings().setLoadWithOverviewMode(true);
            holder.webView.getSettings().setDefaultFontSize(48);
            holder.webView.loadData(searchBean.getReadability(),
                    "text/html; charset=UTF-8","utf8");
            //因为是webview控件，要用触摸事件
            holder.webView.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    switch(event.getAction()){
                        case MotionEvent.ACTION_UP:
                            if(mOnItemClickListener!=null){
                                mOnItemClickListener.onItemClick(v,position);
                            }
                            return true;
                        default:

                        break;
                    }
                    return true;
                }
            });
        }else{
            holder.webView.setVisibility(View.GONE);
        }
        holder.likeBtn.setTag(position);
        holder.likeBtn.setImageResource(isLiked(searchBean)?R.drawable.like
        :R.drawable.unlike);
        holder.likeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleLikeBtn((ImageView)v,searchBean);
            }
        });
    }

    private void toggleLikeBtn(ImageView likeBtn, SearchBean searchBean) {
        if(searchBean.isLiked()){
            likeBtn.setImageResource(R.drawable.unlike);
            changeLike(searchBean,false);
        }else {
            likeBtn.setImageResource(R.drawable.like);
            changeLike(searchBean,true);

        }
    }
    public SearchBean getStuffAt(int pos) {
        return mSearchBeens.get(pos);
    }
    //更新数据库数据
    private void changeLike(SearchBean searchBean, boolean isLiked) {
        searchBean.setLiked(isLiked);
        Stuff stuff = Stuff.checkSearch(realm, searchBean.getUrl());
        realm.beginTransaction();
        try {
            if(stuff==null){
                //数据库找不到就创建
                stuff=Stuff.fromSearch(searchBean);
                stuff.setLastChanged(new Date());
                stuff.setLiked(isLiked);
                realm.copyToRealm(stuff);
            }else {
                stuff.setLiked(isLiked);
                stuff.setLastChanged(new Date());
            }
            realm.commitTransaction();
        } catch (ParseException e) {
            e.printStackTrace();
        }


    }

    private boolean isLiked(SearchBean searchBean) {
        //找到对应的数据
        Stuff stuff = Stuff.checkSearch(realm, searchBean.getUrl());
        if(stuff!=null){
            searchBean.setLiked(stuff.isLiked());
            return stuff.isLiked();
        }
        return false;
    }


    @Override
    public int getItemCount() {
        return mSearchBeens.size();
    }

    @Override
    public long getItemId(int position) {
        return mSearchBeens.get(position).getUrl().hashCode();
    }

    public void clearData() {
        mSearchBeens.clear();
        notifyDataSetChanged();
    }

    public void updateInsertedData(ArrayList<SearchBean> beans, boolean isMore) {
        if(isMore){
            int oldSize = mSearchBeens.size();
            mSearchBeens.addAll(beans);
            notifyItemRangeInserted(oldSize,beans.size());
        }else {
            mSearchBeens.clear();
            mSearchBeens.addAll(beans);
            notifyDataSetChanged();
        }
    }

    public interface OnItemClickListener{
        void onItemClick(View view,int pos);
        void onItemLongClick(View view,int pos);
    }
    public void setOnItemClickListener(OnItemClickListener onItemClickListener){
        mOnItemClickListener=onItemClickListener;
    }

    private <T extends View>T $(View view, int resId){
        return (T)view.findViewById(resId);
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        TextView title,author,date;
        LinearLayout stuff;
        ImageButton likeBtn;
        WebView webView;


        public ViewHolder(View itemView) {
            super(itemView);
            title=$(itemView, R.id.stuff_title);
            author=$(itemView, R.id.stuff_author);
            date=$(itemView, R.id.stuff_date);
            stuff=$(itemView, R.id.stuff);
            likeBtn=$(itemView, R.id.like_btn);
            webView=$(itemView, R.id.readability_wv);
        }
    }
}
