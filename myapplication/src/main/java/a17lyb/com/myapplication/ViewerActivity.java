package a17lyb.com.myapplication;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.SharedElementCallback;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import a17lyb.com.myapplication.JavaBean.Image;
import a17lyb.com.myapplication.Utils.CommonUtil;
import a17lyb.com.myapplication.Utils.PicUtil;
import a17lyb.com.myapplication.widget.GirlsFragment;
import a17lyb.com.myapplication.widget.ViewerFragment;
import io.realm.Realm;
import io.realm.RealmResults;

/**
 * Created by 10400 on 2016/12/23.
 */

public class ViewerActivity extends AppCompatActivity {
    private static final String MSG_URL = "msg_url";
    public static final String TAG = "ViewerActivity";
    private static final String SHARE_TITLE = "share_title";
    private static final String SHARE_TEXT = "share_text";
    private static final String SHARE_URL = "share_url";
    private FragmentStatePagerAdapter mAdapter;
    private ViewPager mViewPager;
    private int mPos;
    private Realm mRealm;
    private RealmResults<Image> mImage;
    public static final String INDEX = "index";
    private HandlerThread mThread;
    private final Handler mMsgHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.arg1) {
                case PicUtil.SAVE_DONE_TOAST:
                    String filepath = msg.getData().getString(PicUtil.FILEPATH);
                    CommonUtil.toast(ViewerActivity.this, "已保存至" + filepath, Toast.LENGTH_LONG);
                    break;
                default:

                    break;
            }
        }
    };
    private Handler mMSavePicHandler;
    private Handler mShareHandler;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportPostponeEnterTransition();
        setContentView(R.layout.viewer_pager_layout);
        initView();
        initData();

    }

    private void initView() {
        mViewPager = (ViewPager) findViewById(R.id.viewer_pager);
    }

    private void initData() {
        mPos = getIntent().getIntExtra(GirlsFragment.POSTION, 0);
        mRealm = Realm.getDefaultInstance();
        mImage = Image.all(mRealm);

        mAdapter = new FragmentStatePagerAdapter(getSupportFragmentManager()) {
            @Override
            public int getCount() {
                return mImage.size();
            }

            @Override
            public Fragment getItem(int position) {
                return ViewerFragment.newInstance(mImage.get(position).getUrl(),
                        position == mPos);
            }
        };
        mViewPager.setAdapter(mAdapter);
        mViewPager.setCurrentItem(mPos);
        if (Build.VERSION.SDK_INT >= 21) {
            Log.e("TAG", "Build.VERSION" + Build.VERSION.SDK_INT + "");
            getWindow().setSharedElementsUseOverlay(false);
        }

        //设置进入动画
        setEnterSharedElementCallback(new SharedElementCallback() {
            @Override
            public void onMapSharedElements(List<String> names, Map<String, View> sharedElements) {
                super.onMapSharedElements(names, sharedElements);
                Image image = mImage.get(mViewPager.getCurrentItem());
                sharedElements.clear();
                sharedElements.put(image.getUrl(), ((ViewerFragment) mAdapter
                        .instantiateItem(mViewPager, mViewPager.getCurrentItem())).getSharedElement());
            }
        });
        mThread = new HandlerThread("save-and-share");
        mThread.start();
        mMSavePicHandler = new Handler(mThread.getLooper()) {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                String url = msg.getData().getString(MSG_URL);
                try {
                    PicUtil.saveBitmapFromUrl(ViewerActivity.this, url, mMsgHandler);
                } catch (ExecutionException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };

        mShareHandler = new Handler(mThread.getLooper()) {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                String title = msg.getData().getString(SHARE_TITLE);
                String text = msg.getData().getString(SHARE_TEXT);
                String url = msg.getData().getString(SHARE_URL);
                shareImg(title,text,url);
            }
        };
    }

    public void showImgOptDialog(String url) {
        ImageOptionDialog.newInstance(url).show(getSupportFragmentManager(), TAG);
    }

    //对话框界面
    public static class ImageOptionDialog extends DialogFragment {
        private static final String OPT_URL = "option_url";
        private String mUrl;
        private TextView saveText;
        private TextView shareText;

        public static ImageOptionDialog newInstance(String url) {
            Bundle args = new Bundle();
            args.putString(OPT_URL, url);

            ImageOptionDialog fragment = new ImageOptionDialog();
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            mUrl = getArguments().getString(OPT_URL);
        }

        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
            View view = inflater.inflate(R.layout.dialog_image_option, container, false);
            shareText = (TextView) view.findViewById(R.id.share_img);
            saveText = (TextView) view.findViewById(R.id.save_img);
            saveText.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ((ViewerActivity) getActivity()).saveImg(mUrl);
                    dismiss();
                }
            });
            shareText.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ((ViewerActivity) getActivity()).shareImg(mUrl);
                    dismiss();
                }
            });

            return view;
        }
    }

    private void shareImg(String url) {
        if (url == null) {
            return;
        }
        Message message = Message.obtain();
        Bundle bundle = new Bundle();
        bundle.putString(SHARE_TITLE, "一条来自干货的消息");
        bundle.putString(SHARE_TEXT, null);
        bundle.putString(SHARE_URL, url);
        message.setData(bundle);
        mShareHandler.sendMessage(message);

    }
    //在线程中操作的
    private void shareImg(String title,String text,String url) {
        //取出存放路径
        String imgPath = PicUtil.getImgPathFromUrl(url);
        Intent intent = new Intent(Intent.ACTION_SEND);
        //路径没有 为空
        if(imgPath==null || imgPath.equals("")){
            intent.setType("text/plain");
        }else {
            File file = new File(imgPath);
            //没有文件
            if(!file.exists()){
                try {
                    PicUtil.saveBitmapFromUrl(ViewerActivity.this, url, mMsgHandler);
                } catch (ExecutionException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            if (file.exists() && file.isFile()){
                intent.setType("image/jpg");
                Uri uri = Uri.fromFile(file);
                intent.putExtra(Intent.EXTRA_STREAM,uri);
            }
        }
        intent.putExtra(Intent.EXTRA_SUBJECT,title);
        intent.putExtra(Intent.EXTRA_TEXT,text);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);


    }
    private void saveImg(String url) {
        if (url == null) {
            return;
        }
        Message message = Message.obtain();
        Bundle bundle = new Bundle();
        bundle.putString(MSG_URL, url);
        message.setData(bundle);
        mMSavePicHandler.sendMessage(message);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mRealm.removeAllChangeListeners();
        mRealm.close();
        mThread.quit();
    }
}
