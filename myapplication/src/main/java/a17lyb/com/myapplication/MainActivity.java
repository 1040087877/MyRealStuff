package a17lyb.com.myapplication;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.SearchRecentSuggestions;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.util.TypedValue;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewAnimationUtils;

import java.util.ArrayList;

import a17lyb.com.myapplication.Base.BaseFragment;
import a17lyb.com.myapplication.JavaBean.GirlRoot;
import a17lyb.com.myapplication.JavaBean.Image;
import a17lyb.com.myapplication.JavaBean.Stuff;
import a17lyb.com.myapplication.Utils.CommonUtil;
import a17lyb.com.myapplication.Utils.Constants;
import a17lyb.com.myapplication.widget.CollectionFragment;
import a17lyb.com.myapplication.widget.GirlsFragment;
import a17lyb.com.myapplication.widget.SearchFragment;
import a17lyb.com.myapplication.widget.SearchSuggestionProvider;
import a17lyb.com.myapplication.widget.StuffFragment;
import io.realm.Realm;

import static a17lyb.com.myapplication.R.id.fab;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{

    private RecyclerView mRecyclerView;
    private LinearLayoutManager mLinearLayoutManager;
    public static ArrayList<String> data = new ArrayList<>();
    private GirlRoot mGirlRoot;
//    private MyAdapter mMyAdapter;
    private StaggeredGridLayoutManager mSLayout;
    private FloatingActionButton mFb;
    private SearchView mSearchView;
    private DrawerLayout mDrawer;
    private SwipeRefreshLayout mRefreshLayout;
    private boolean mIsRefreshing;
    private Fragment mCurrenFragment;
    private String mCurrenFragmentType;
    private Realm mRealm;
    private Bundle mReenterState;
    private CoordinatorLayout mCoordinatorLayout;
    private Toolbar mToolbar;
    private DrawerLayout mDrawerLayout;
    private boolean mIsSearching=false;
    private Handler mClearCacheHandler;
    private static final int CLEAR_DONE = 0x36;
    private static final int CLEAR_ALL = 0x33;
    private static final String CURR_TYPE = "curr_fragment_type";
    GestureDetector mGestureDetector = new GestureDetector(new GestureDetector.SimpleOnGestureListener() {
        @Override
        public boolean onDoubleTap(MotionEvent e) {
            CommonUtil.makeSnackBar(mCoordinatorLayout, getResources().getString(R.string.main_double_taps), Snackbar.LENGTH_LONG);
            return true;
        }
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        initData();

    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        if(Intent.ACTION_SEARCH.equals(intent.getAction())){
            if(((BaseFragment)mCurrenFragment).isFetching()){
                CommonUtil.makeSnackBar(mCoordinatorLayout,getString(R.string.frag_is_fetching),Snackbar.LENGTH_SHORT);
                return;
            }
            //获取搜索框里的值
            String query = intent.getStringExtra(SearchManager.QUERY);
            //去掉特殊符号
            String safeText = CommonUtil.stringFilterStrict(query);
            //有非法字符 不执行
            if(safeText==null || safeText.length()==0 || safeText.length()!=query.length()){
                CommonUtil.makeSnackBar(mCoordinatorLayout, getString(R.string.search_tips), Snackbar.LENGTH_LONG);
            }else {
                //保存搜素记录
                new SearchRecentSuggestions(this, SearchSuggestionProvider.AUTHORITY,SearchSuggestionProvider.MODE)
                        .saveRecentQuery(safeText,null);
                //获取类型
                Constants.TYPE type=getCurrSearchType();
                String searchCat;
                if(type==null){
                    searchCat=getString(R.string.api_all);
                }else {
                    searchCat=type.getApiName();
                }
                switchToSearchResult(safeText,searchCat,10);
            }

        }
    }

    private void switchToSearchResult(String safeText, String searchCat, int count) {
        FragmentManager manager = getSupportFragmentManager();
        String searchTag = Constants.TYPE.SEARCH_RESULTS.getId();
        Fragment searchFragment = manager.findFragmentByTag(searchTag);
        //没创建过
        if(searchFragment==null){
            hideAndAdd(manager, SearchFragment.newInstance(safeText,
                    searchCat,count),searchTag);
        }else {
            hideAndShow(manager,searchFragment,searchTag);
            ((SearchFragment)searchFragment).search(safeText,searchCat, count);
        }
    }

    private Constants.TYPE getCurrSearchType() {
        if (Constants.TYPE.GIRLS.getId().equals(mCurrenFragmentType)
                || Constants.TYPE.COLLECTIONS.getId().equals(mCurrenFragmentType) || Constants.TYPE.SEARCH_RESULTS.getId().equals(mCurrenFragmentType)) {
            return null;
        }else {
            return Constants.TYPE.valueOf(mCurrenFragmentType);
        }

    }

    private void initView() {
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mToolbar.setTitle("照片墙");
        mToolbar.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return mGestureDetector.onTouchEvent(event);
            }
        });
        setSupportActionBar(mToolbar);
//        mRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_layout);
//        mRecyclerView = (RecyclerView) findViewById(R.id.rcy);
        mFb = (FloatingActionButton) findViewById(fab);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mSearchView = (SearchView) findViewById(R.id.searchview);
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        mSearchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        mSearchView.setIconifiedByDefault(false);
        //设置抽屉和抽屉开关
        setDrawerOpen(mToolbar);
        mCoordinatorLayout = (CoordinatorLayout) findViewById(R.id.main_coor_layout);

        FragmentManager fm = getSupportFragmentManager();
        Fragment fragment = fm.findFragmentById(R.id.fragment_container);
        getGirlFragment(fm, fragment);
        //抽屉布局
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }

    private void setDrawerOpen(Toolbar toolbar) {
        //设置抽屉和抽屉开关
        mDrawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, mDrawer, toolbar, R.string.navigation_drawer_open,
                R.string.navigation_drawer_close);
        mDrawer.setDrawerListener(toggle);
        toggle.syncState();
    }

    private void getGirlFragment(FragmentManager fm, Fragment fragment) {
        if(fragment==null){
            fragment = GirlsFragment.newInstance(Constants.TYPE.GIRLS.getApiName());
            fm.beginTransaction()
                    .add(R.id.fragment_container,fragment, Constants.TYPE.GIRLS.getId())
                    .commit();
            mCurrenFragment = fragment;
            mCurrenFragmentType = Constants.TYPE.GIRLS.getId();
        }
    }

    @Override
    public void onActivityReenter(int resultCode, Intent data) {
        super.onActivityReenter(resultCode, data);
        if (Constants.TYPE.GIRLS.getId().equals(mCurrenFragmentType)) {
            mReenterState = new Bundle(data.getExtras());
            int index = mReenterState.getInt(ViewerActivity.INDEX, 0);
            supportPostponeEnterTransition();
//            ((GirlsFragment)mCurrenFragment).onActivityReenter(index);
        }
    }

    private void initData() {
//        mSLayout = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
//        mRecyclerView.setLayoutManager(mSLayout);
        mFb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((BaseFragment)mCurrenFragment).smoothScrollToTop();
            }
        });


        /*Call<GirlRoot> girlMsg = API.grilService.getGirlMsg(10);
        girlMsg.enqueue(new Callback<GirlRoot>() {
            @Override
            public void onResponse(Call<GirlRoot> call, Response<GirlRoot> response) {
                mGirlRoot = new GirlRoot();
                mGirlRoot = response.body();
//                Log.e("TAG", girlRoot.toString());
//                mMyAdapter = new MyAdapter(MainActivity.this, mGirlRoot);
//                mRecyclerView.setAdapter(mMyAdapter);
//
//                mMyAdapter.setOnItemClickListener(new MyAdapter.OnRecyclerViewItemClickListener() {
//                    public void onItemClick(View view, String data) {
////                Toast.makeText(MainActivity.this, data, Toast.LENGTH_SHORT).show();
//                        makeSnackBar(view, data, Snackbar.LENGTH_SHORT);
//                    }
//                });
            }

            @Override
            public void onFailure(Call<GirlRoot> call, Throwable t) {
                t.printStackTrace();
            }
        });*/

        mRealm = Realm.getDefaultInstance();
        mClearCacheHandler=new Handler(){
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch(msg.what){
                    case CLEAR_DONE:
                        ((BaseFragment)mCurrenFragment).updateData();
                    break;
                    default:

                    break;
                }
            }
        };
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main,menu);
        return true;
    }

    @Override
    public void onBackPressed() {
        if(mDrawerLayout.isDrawerOpen(GravityCompat.START)){
            mDrawerLayout.closeDrawer(GravityCompat.START);
        }else if(mIsSearching){
            mIsSearching=false;
            hideSearchView();
        }else {
            super.onBackPressed();
        }

    }

    private void hideSearchView() {
        if(mSearchView!=null){
            mSearchView.setVisibility(View.GONE);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if(id==R.id.action_about){
            startActivity(new Intent(this,AboutActivity.class));
            return true;
        }else if(id==R.id.action_clear_cache){
            if(((BaseFragment)mCurrenFragment).isFetching()){
                CommonUtil.makeSnackBar(mCoordinatorLayout, getString(R.string.frag_is_fetching), Snackbar.LENGTH_SHORT);
            }else {
                clearRealmType(mCurrenFragmentType);
            }
            return true;
        }else if(id==R.id.action_search){
            mIsSearching=true;
            showSearchView();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void clearRealmType(String currenFragmentType) {
        /**
         * 在收藏的界面点清除数据表示清除所有缓存
         */
        if(Constants.TYPE.COLLECTIONS.getId().equals(currenFragmentType)){
            //清除所有数据
            clearCacheSnackBar(R.string.clear_cache_all,
                    new View.OnClickListener(){
                        @Override
                        public void onClick(View v) {
                            Image.clearImage(MainActivity.this,mRealm);
                            Stuff.clearAll(mRealm);
                            mClearCacheHandler.sendEmptyMessage(CLEAR_ALL);
                        }
                    });
        }else if(Constants.TYPE.SEARCH_RESULTS.getId().equals(currenFragmentType)){
            CommonUtil.makeSnackBar(mCoordinatorLayout, getString(R.string.no_search_cache), Snackbar.LENGTH_SHORT);
        //清除特定类型的数据
        }else{
            //清除种类id
            int strId = Constants.TYPE.valueOf(currenFragmentType).getStrId();
            //清除种类名称
            final String apiName = Constants.TYPE.valueOf(currenFragmentType).getApiName();
            if(strId!=-1){
                clearCacheSnackBar(strId,new View.OnClickListener(){

                    @Override
                    public void onClick(View v) {
                        if(Constants.TYPE.GIRLS.getApiName().equals(apiName)){
                            //清除图片
                            Image.clearImage(MainActivity.this,mRealm);

                        }else {
                            Stuff.clearType(mRealm,apiName);
                        }
                        mClearCacheHandler.sendEmptyMessage(CLEAR_DONE);
                    }
                });
            }

        }
    }

    /**
     *  String.format 拼接字符串
     * @param clearTipStrId 清除类型
     * @param onClickListener 类型监听
     */
    private void clearCacheSnackBar(int clearTipStrId, View.OnClickListener onClickListener) {
        CommonUtil.makeSnackBarWithAction(mCoordinatorLayout,
                String.format(getString(R.string.clear_type),getString(clearTipStrId)),Snackbar.LENGTH_SHORT,onClickListener
        ,getString(R.string.confirm));
    }

    private void showSearchView() {
        if(mSearchView!=null){
            mSearchView.setVisibility(View.VISIBLE);
            /**
             * dx 转 dp
             * getResources().getDisplayMetrics() 获取屏幕像素
             * cx xy=动画开始的x坐标和y坐标
             *finalRadius 结束的半径。
             */
            //设置动画
            int cx=mSearchView.getWidth()- (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,24,mSearchView.getResources().getDisplayMetrics());
            int cy=mSearchView.getHeight()/2;

            int finalRadius = Math.max(mSearchView.getWidth(), mSearchView.getHeight());
            if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.LOLLIPOP){
                ViewAnimationUtils.createCircularReveal(mSearchView,cx,cy,0,finalRadius).start();
            }

        }
    }

    /*private static class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder> implements View.OnClickListener {

        private Context context;
        private List<GirlRoot.GirlBean> girlBeanList;

        public MyAdapter(MainActivity mainActivity, GirlRoot mGirlRoot) {
            this.context = mainActivity;
            girlBeanList = mGirlRoot.getgirlBeanList();
        }


        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.girl_item, null);
            ViewHolder vh = new ViewHolder(view);
            view.setOnClickListener(this);
            return vh;
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
//            holder.mImageView.setText(data.get(position));
//            Point size = new Point();
//            holder.mImageView.setOriginalSize(size.x,size.y);
            Glide.with(context).load(girlBeanList.get(position).getUrl())
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(holder.mImageView);
            holder.itemView.setTag(position+"");
        }

        @Override
        public int getItemCount() {
            return girlBeanList.size();
        }

        @Override
        public void onClick(View v) {
            if (mOnItemClickListener != null) {
                mOnItemClickListener.onItemClick(v, (String) v.getTag());
            }
        }

        public void updateRefreshed(int numImages){
            notifyItemRangeChanged(0,numImages);
            Log.d("TAG", "updateInsertedData: from 0 to " + numImages);
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            public RatioImageView mImageView;
            private CardView cardView;

            public ViewHolder(View itemView) {
                super(itemView);
                cardView = (CardView) itemView.findViewById(R.id.cardview);
                mImageView = (RatioImageView) itemView.findViewById(R.id.iv);
            }
        }

        public OnRecyclerViewItemClickListener mOnItemClickListener = null;

        public void setOnItemClickListener(OnRecyclerViewItemClickListener listener) {
            mOnItemClickListener = listener;
        }

        public static interface OnRecyclerViewItemClickListener {
            void onItemClick(View view, String data);
        }
    }*/

    public static void makeSnackBar(View parentView, String str, int length) {
        final Snackbar snackbar = Snackbar.make(parentView, str, length);
        snackbar.show();
    }


    private void setFetchingFlagsFalse() {
        if (mIsRefreshing)
            mIsRefreshing = false;
    }

    public void setRefreshLayout(final boolean state){
        if(mRefreshLayout==null){
            return;
        }
        mRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                mRefreshLayout.setRefreshing(state);
            }
        });
    }



    //跳到指定fragment
    private void swithTo(FragmentManager manager, String type, Fragment addFragment) {
        Fragment fragment = manager.findFragmentByTag(type);
        //已经显示过的就不要在添加了，直接显示出来
        if(fragment!=null){
            hideAndShow(manager,fragment,type);
            //没有显示过的要重新添加
        }else {
            hideAndAdd(manager,addFragment,type);
        }
    }

    private void hideAndAdd(FragmentManager manager, Fragment newFragment, String fragmentIdx) {
        manager.beginTransaction().hide(mCurrenFragment).add(R.id.fragment_container,newFragment,fragmentIdx).commit();
        mCurrenFragment =newFragment;
        mCurrenFragmentType=fragmentIdx;
        mToolbar.setTitle(Constants.TYPE.valueOf(fragmentIdx).getStrId());
    }

    private void hideAndShow(FragmentManager manager, Fragment newFragment, String fragmentIdx) {
        manager.beginTransaction().hide(mCurrenFragment).show(newFragment).commit();
//        updateLikedData(newFragment, fragmentIdx);
        mCurrenFragment =newFragment;
        mCurrenFragmentType=fragmentIdx;
        mToolbar.setTitle(Constants.TYPE.valueOf(fragmentIdx).getStrId());
    }

    private void closeDrawer() {
        mDrawerLayout.closeDrawer(GravityCompat.START);
    }

    //抽屉监听操作
    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
//        CommonUtil.makeSnackBar(mCoordinatorLayout,"156156156点击了", Snackbar.LENGTH_SHORT);
        int id = item.getItemId();
        FragmentManager manager = getSupportFragmentManager();
        if(((BaseFragment)mCurrenFragment).isFetching()){
            CommonUtil.makeSnackBar(mCoordinatorLayout, getString(R.string.frag_is_fetching), Snackbar.LENGTH_SHORT);
            closeDrawer();
            return false;
        }

        if(id== Constants.TYPE.GIRLS.getResId()){
            swithTo(manager, Constants.TYPE.GIRLS.getId(),GirlsFragment.newInstance(Constants.TYPE.GIRLS.getApiName()));
        }else if(id== Constants.TYPE.COLLECTIONS.getResId()){
            swithTo(manager, Constants.TYPE.COLLECTIONS.getId(), CollectionFragment.newInstance(Constants.TYPE.COLLECTIONS.getApiName()));
        }else {
            for (Constants.TYPE type: Constants.TYPE.values()){
                if(type.getResId()==id){
                    swithTo(manager,type.getId(), StuffFragment.newInstance(type.getApiName()));
                    break;
                }
            }
        }
        closeDrawer();
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mRealm.close();
        CommonUtil.clearCache(getApplicationContext());
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(CURR_TYPE,mCurrenFragmentType);
    }

    @Override
    //内存不够被销魂的处理
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mCurrenFragmentType =savedInstanceState.getString(CURR_TYPE);
        hideAllExcept(mCurrenFragmentType);
        mToolbar.setTitle(Constants.TYPE.valueOf(mCurrenFragmentType).getStrId());

    }

    private void hideAllExcept(String currenFragmentType) {
        FragmentManager manager = getSupportFragmentManager();
        for(Constants.TYPE type: Constants.TYPE.values()){
            Fragment fragment = manager.findFragmentByTag(type.getId());
            if(fragment==null){
                continue;
            }
            if(type.getId().equals(mCurrenFragmentType)){
                manager.beginTransaction().show(fragment).commit();
                mCurrenFragment=fragment;
            }else {
                manager.beginTransaction().hide(fragment).commit();
            }
        }
    }
}
