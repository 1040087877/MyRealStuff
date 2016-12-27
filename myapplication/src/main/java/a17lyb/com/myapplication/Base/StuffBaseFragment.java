package a17lyb.com.myapplication.Base;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;

import a17lyb.com.myapplication.JavaBean.Stuff;
import a17lyb.com.myapplication.R;

/**
 * Created by 10400 on 2016/12/24.
 */

public abstract class StuffBaseFragment extends BaseFragment{
    @Override
    protected int getLastVisiblePos() {
        return ((LinearLayoutManager) mMLayoutManager).findLastVisibleItemPosition();
    }

    @Override
    protected RecyclerView.LayoutManager getLayoutManager() {
        return new LinearLayoutManager(getActivity(),LinearLayoutManager.VERTICAL,false);
    }

    @Override
    public int getLayoutResId() {
        return R.layout.stuff_fragment;
    }

    @Override
    protected int getRecyclerViewId() {
        return R.id.stuff_recyclerview;
    }

    @Override
    protected int getRefreshLayoutId() {
        return R.id.stuff_refresh_layout;
    }

    public class ShareListener implements AbsListView.MultiChoiceModeListener{
        private final Context context;
        private final Stuff mStuff;
        private final View view;

        public ShareListener(Context context, Stuff stuff, View view) {
            this.context = context;
            mStuff = stuff;
            this.view = view;
        }

        @Override
        public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {

        }

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            mode.getMenuInflater().inflate(R.menu.context_menu,menu);
            //开启活跃状态 主要跟背景颜色有关
            view.setActivated(true);
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            switch(item.getItemId()){
                case R.id.context_menu_share:
                    String textShared=mStuff.getDesc()+"   "+mStuff.getUrl()+" -- " + context.getString(R.string.share_msg);
                    Intent intent = new Intent(Intent.ACTION_SEND);
                    intent.setType("text/plain");
                    intent.putExtra(Intent.EXTRA_SUBJECT,context.getString(R.string.share_msg));
                    intent.putExtra(Intent.EXTRA_TEXT,textShared);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(intent);
                    mode.finish();
                    return true;
                default:

                break;
            }
            return false;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            //取消活跃状态
            view.setActivated(false);
        }
    }
}
