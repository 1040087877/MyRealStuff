package a17lyb.com.myapplication;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.NavUtils;
import android.support.v4.util.ArrayMap;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

import a17lyb.com.myapplication.Utils.CommonUtil;

/**
 * Created by 10400 on 2016/12/26.
 */

public class AboutActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private RecyclerView mRecyclerView;
    private ArrayMap<String, String> mLibsList;
    private ArrayList<String> mFeasList;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        mToolbar = $(R.id.about_toolbar);
        mRecyclerView = $(R.id.about_recyclerview);

        mToolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);
        setSupportActionBar(mToolbar);
        if (NavUtils.getParentActivityName(this) != null) {
            //让点击生效
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        TextView verTv = $(R.id.version_name);
        try {
            //格式化字符串（版本号）
            verTv.setText(String.format(getString(R.string.version_name), CommonUtil.getVersionName(this)));
        } catch (Exception e) {
            e.printStackTrace();
        }
        initData();
        AboutAdapter adapter = new AboutAdapter();
        mRecyclerView.setAdapter(adapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

    }

    private void initData() {
        mLibsList = new ArrayMap<>();
        mLibsList.put("bumptech / Glide", "https://github.com/bumptech/glide");
        mLibsList.put("Mike Ortiz / TouchImageView", "https://github.com/MikeOrtiz/TouchImageView");
        mLibsList.put("Realm", "https://realm.io");
        mLibsList.put("Square / Retrofit", "https://github.com/square/retrofit");

        mFeasList = new ArrayList<>();
        mFeasList.add("CardView");
        mFeasList.add("CollapsingToolbarLayout");
        mFeasList.add("DrawerLayout");
        mFeasList.add("RecyclerView");
        mFeasList.add("Shared Element Transition");
        mFeasList.add("SnackBar");
        mFeasList.add("TranslucentBar");

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.home:
                if (NavUtils.getParentActivityName(this) != null) {
                    NavUtils.navigateUpFromSameTask(this);
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private <T extends View> T $(int resId) {
        return (T) findViewById(resId);
    }

    private <T extends View> T $(View view, int resId) {
        return (T) view.findViewById(resId);
    }

    class AboutAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return viewType == 1 ? new ItemViewHolder(getLayoutInflater().inflate(R.layout.about_item, parent, false)) : new HeaderViewHolder(getLayoutInflater().inflate(R.layout.about_header, parent, false));
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            /**
             * mLibsList.keyAt 获取key值，比如：bumptech / Glide
             */
            //不是头条类型
            if (holder.getItemViewType() == 1) {
                if (position < mLibsList.size() + 1) {
                    ((ItemViewHolder) holder).mTextView.setText(mLibsList.keyAt(position - 1));
                } else {
                    ((ItemViewHolder) holder).mTextView.setText(mFeasList.get(position - 2 - mLibsList.size()));
                    //设置不能点击事件
                    ((ItemViewHolder) holder).mTextView.setClickable(false);
                }
                //是头条类型
            } else {
                ((HeaderViewHolder) holder).mTextView.setText(position == 0 ? R.string.about_libs_used : R.string.about_feas_used);
            }
        }

        @Override
        public int getItemCount() {
            return mLibsList.size() + mFeasList.size() + 2;
        }


        @Override
        public int getItemViewType(int position) {
            return (position == 0 || position == mLibsList.size() + 1) ? 0 : 1;
        }
    }

    private class ViewHolder extends RecyclerView.ViewHolder {

        public ViewHolder(View itemView) {
            super(itemView);
        }
    }

    private class HeaderViewHolder extends ViewHolder {
        TextView mTextView;

        public HeaderViewHolder(View itemView) {
            super(itemView);
            this.mTextView = (TextView) itemView;
        }
    }

    private class ItemViewHolder extends ViewHolder {
        TextView mTextView;

        public ItemViewHolder(View itemView) {
            super(itemView);
            this.mTextView = $(itemView, R.id.item_text);
            this.mTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int pos = getAdapterPosition();
                    //有头布局要+1
                    /**
                     * mLibsList.valueAt 获取value的值 是网页链接
                     */
                    if (pos < mLibsList.size() + 1 && pos != 0) {
                        CommonUtil.openUrl(AboutActivity.this, mLibsList.valueAt(pos - 1));
                    }
                }
            });
        }
    }

}
