package a17lyb.com.myapplication.Net;

import android.app.IntentService;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.Target;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;

import a17lyb.com.myapplication.JavaBean.Image;
import a17lyb.com.myapplication.Utils.Constants;
import a17lyb.com.myapplication.Utils.DateUtil;
import io.realm.Realm;
import io.realm.RealmResults;

/**
 * Created by 10400 on 2016/12/22.
 */
public class ImageFetchService extends IntentService implements ImageFetcher {

    private Constants.NETWORK_EXCEPTION mExceptionCode;
    private LocalBroadcastManager mLocalBroadcastManager;
    public static final String ACTION_UPDATE_RESULT = "girls_update_result";
    public static final String EXTRA_FETCHED = "girls_fetched";
    public static final String EXTRA_TRIGGER = "girls_trigger";
    public static final String EXTRA_EXCEPTION_CODE = "exception_code";
    private static final String TAG = "ImageFetchService";
    public static final String ACTION_FETCH_REFRESH = "girls_fetch_refresh";
    public static final String ACTION_FETCH_MORE = "girls_fetch_more";

    public ImageFetchService() {
        super(TAG);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mExceptionCode = Constants.NETWORK_EXCEPTION.DEFAULT;
        mLocalBroadcastManager = LocalBroadcastManager.getInstance(this);
    }

    @Override
    public void prefetchImage(String url, Point measured) throws IOException, InterruptedException, ExecutionException {
        //获取网络图片 长和宽
        Bitmap bitmap = Glide.with(this).load(url).asBitmap()
                .into(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL).get();
        measured.x = bitmap.getWidth();
        measured.y = bitmap.getHeight();
        Log.d("TAG", "pre-measured image: " + measured.x + " x " + measured.y + " " + url);

    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.e(TAG, "onHandleIntent: ");
        Realm realm = Realm.getDefaultInstance();
        //从数据库拿数据
        RealmResults<Image> latest = Image.all(realm);
        int fetched = 0;
        try {
            //没有数据 重新获取
            if (latest.isEmpty()) {
                fetched = fetchLatest(realm);
                Log.d(TAG, "no latest, fresh fetch");
                //找到数据
            } else if (ACTION_FETCH_REFRESH.equals(intent.getAction())) {
                Log.d(TAG, "latest fetch: " + latest.first().getPublishedAt());
                fetched = fetchRefresh(realm, latest.first().getPublishedAt());
            } else if (ACTION_FETCH_MORE.equals(intent.getAction())) {
                Log.d(TAG, "earliest fetch: " + latest.last().getPublishedAt());
                fetched = fetchMore(realm, latest.last().getPublishedAt());
            }
        } catch (SocketTimeoutException e) {
            mExceptionCode = Constants.NETWORK_EXCEPTION.TIMEOUT;
            Log.d(TAG, mExceptionCode+"");
        } catch (UnknownHostException e) {
            mExceptionCode = Constants.NETWORK_EXCEPTION.UNKNOWN_HOST;
            Log.d(TAG, mExceptionCode+"");
        } catch (IOException e) {
            mExceptionCode = Constants.NETWORK_EXCEPTION.IOEXCEPTION;
            Log.d(TAG, mExceptionCode+"");
        } catch (Exception e) {
            e.printStackTrace();
        }
        sendResult(intent, realm, fetched);
    }

    private void sendResult(Intent intent, Realm realm, int fetched) {
        realm.close();
        Log.d(TAG, "finished fetching, actual fetched " + fetched);
        Intent broadcast=new Intent(ACTION_UPDATE_RESULT)
        .putExtra(EXTRA_FETCHED,fetched)
        .putExtra(EXTRA_EXCEPTION_CODE,mExceptionCode)
        .putExtra(EXTRA_TRIGGER,intent.getAction());
        mLocalBroadcastManager.sendBroadcast(broadcast);
    }

    //刷新最新数据
    private int fetchLatest(Realm realm) throws IOException {
        //执行网络获取图片数据
        GankAPI.Result<List<Image>> result = GankAPIService.getInstance().latestGirls(10).execute().body();
        if (result.error) {
            return 0;
        }
        int resultSize = result.results.size();
        for (int i = 0; i < resultSize; i++) {
            if (!saveToDb(realm, result.results.get(i))) {
                return i;
            }
        }
        return resultSize;
    }

    //刷新数据
    private int fetchRefresh(Realm realm, Date publishedAt) throws IOException {
        String after = DateUtil.format(publishedAt);
        //计算时间
        List<String> dates = DateUtil.generateSequenceDateTillToday(publishedAt);
        return fetch(realm, after, dates);
    }
    //获取更多数据
    private int fetchMore(Realm realm, Date publishedAt) throws IOException {
        String before = DateUtil.format(publishedAt);
        List<String> dates = DateUtil.generateSequenceDateBefore(publishedAt, 20);
        return fetch(realm, before, dates);
    }

    //重新从网络加载数据，主要根据日期判断
    private int fetch(Realm realm, String baseline, List<String> dates) throws IOException {
        int fetched = 0;
        for (String date : dates) {
            if (date.equals(baseline)) {
                continue;
            }
            //获取第几号的数据
            GankAPI.Result<GankAPI.Grils> girlsResult = GankAPIService.getInstance().dayGirls(date).execute().body();
            if (girlsResult.error || null == girlsResult || null == girlsResult.results.images) {
                continue;
            }
            for (Image image : girlsResult.results.images) {
                if (!saveToDb(realm, image)) {
                    return fetched;
                }
                fetched++;
            }

        }
        return fetched;
    }

    private boolean saveToDb(Realm realm, Image image) {
        //保存图片信息到本地
        realm.beginTransaction();

        try {
            realm.copyToRealm(Image.persist(image, this));
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("TAG", "Failed to fetch image", e);
            //取消事务
            realm.cancelTransaction();
            return false;
        }
        realm.commitTransaction();
        return true;

    }
}
