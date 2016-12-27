package a17lyb.com.myapplication.Services;

import android.app.IntentService;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.List;

import a17lyb.com.myapplication.JavaBean.Stuff;
import a17lyb.com.myapplication.Net.GankAPI;
import a17lyb.com.myapplication.Net.GankAPIService;
import a17lyb.com.myapplication.Utils.Constants;
import a17lyb.com.myapplication.Utils.DateUtil;
import a17lyb.com.myapplication.widget.StuffFragment;
import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.exceptions.RealmPrimaryKeyConstraintException;


/**
 * Created by 10400 on 2016/12/24.
 */

public class StuffFetchService extends IntentService {
    private static final String TAG = "StuffFetchService";
    public static final String ACTION_UPDATE_RESULT = "update_result";
    public static final String EXTRA_FETCHED = "fetched";
    public static final String EXTRA_TRIGGER = "trigger";
    public static final String EXTRA_TYPE = "type";
    public static final String ACTION_FETCH_REFRESH = "fetch_refresh";
    public static final String ACTION_FETCH_MORE = "fetch_more";
    public static final String EXTRA_EXCEPTION_CODE = "exception_code";
    private String mType;
    private Constants.NETWORK_EXCEPTION mExceptionCode;
    private LocalBroadcastManager mLocalBroadcastManager;
    private Stuff mTest;

    public StuffFetchService() {
        super(TAG);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mExceptionCode = Constants.NETWORK_EXCEPTION.DEFAULT;
        mLocalBroadcastManager = LocalBroadcastManager.getInstance(this);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        //消息的类型
        mType = intent.getStringExtra(StuffFragment.SERVICE_TYPE);
        Log.d(TAG, "onHandleIntent: " + mType);
        Realm realm = Realm.getDefaultInstance();
        RealmResults<Stuff> latest = Stuff.all(realm, mType);
        for (Stuff stuff :
                latest) {
            Log.e(TAG, "latest"+stuff.getDesc());
        }
        int fetched = 0;

        try {
            if (latest.isEmpty()) {
                fetched=fetchLatest(realm);
                Log.e(TAG, "no latest, fresh fetch");
            }else if (ACTION_FETCH_REFRESH.equals(intent.getAction())) {
                Log.e(TAG, "latest fetch: " + latest.first().getPublishedAt());
                fetched = fetchRefresh(realm, latest.first().getPublishedAt());
            } else if (ACTION_FETCH_MORE.equals(intent.getAction())) {
                Log.e(TAG, "earliest fetch: " + latest.last().getPublishedAt());
                fetched = fetchMore(realm, latest.last().getPublishedAt());
            }
        }catch (SocketTimeoutException e) {
            mExceptionCode = Constants.NETWORK_EXCEPTION.TIMEOUT;
        } catch (UnknownHostException e) {
            mExceptionCode = Constants.NETWORK_EXCEPTION.UNKNOWN_HOST;
        } catch (IOException e) {
            mExceptionCode = Constants.NETWORK_EXCEPTION.IOEXCEPTION;
        } catch (Exception e) {
            e.printStackTrace();
        }

        sendResult(intent,realm,fetched);

    }

    private void sendResult(Intent intent, Realm realm, int fetched) {
        realm.close();
        Log.d(TAG, "finished fetching, actual fetched " + fetched);
        Intent broadcast = new Intent(ACTION_UPDATE_RESULT);
        broadcast.putExtra(EXTRA_FETCHED,fetched)
                .putExtra(EXTRA_TRIGGER,intent.getAction())
                .putExtra(EXTRA_EXCEPTION_CODE,mExceptionCode)
                .putExtra(EXTRA_TYPE,mType);
        mLocalBroadcastManager.sendBroadcast(broadcast);

    }

    private int fetchMore(Realm realm, Date publishedAt) throws IOException {
        String before = DateUtil.format(publishedAt);
        //向后计算日期，存到集合
        List<String> dates = DateUtil.generateSequenceDateBefore(publishedAt,10);
        return fetch(realm,before,dates);
    }

    private int fetchRefresh(Realm realm, Date publishedAt) throws IOException {
        String after = DateUtil.format(publishedAt);
        //距离今天的日期 还有多少
        //向当天计算日期，存到集合
        List<String> dates = DateUtil.generateSequenceDateTillToday(publishedAt);
        return fetch(realm,after,dates);
    }

    private int fetch(Realm realm, String after, List<String> dates) throws IOException {
        int fetched=0;
        //判断是哪一种类型的数据 ，是android ios app 还是其他
        if(mType.equals(Constants.TYPE.ANDROID.getApiName())){
            //取出每一个item的时间
            for(String date:dates){
                if(date.equals(after)){
                    continue;
                }
                //查出具体某一天的数据
                GankAPI.Result<GankAPI.Androids> stuffsResult = GankAPIService.getInstance().dayAndroid(date).execute().body();
                if(stuffsResult.error || stuffsResult.results==null ||
                        stuffsResult.results.stuffs==null){
                    continue;
                }
                for(Stuff stuff:stuffsResult.results.stuffs){
                    if(!saveToDb(realm,stuff)){
                        return fetched;
                    }
                    fetched++;
                }
            }
        }else if(mType.equals(Constants.TYPE.IOS.getApiName())){
            for (String date:dates){
                if(date.equals(after)){
                    continue;
                }
                //取出单条的数据
                GankAPI.Result<GankAPI.IOSs> stuffsResult = GankAPIService.getInstance().dayIOSs(date).execute().body();
                if(stuffsResult.error || null==stuffsResult.results || null==stuffsResult.results.stuffs){
                    continue;
                }
                for (Stuff stuff:stuffsResult.results.stuffs){
                    if(!saveToDb(realm,stuff)){
                        return fetched;
                    }
                    fetched++;
                }
            }
        } else if(mType.equals(Constants.TYPE.APP.getApiName())){
            for (String date:dates){
                if(date.equals(after)){
                    continue;
                }
                GankAPI.Result<GankAPI.Apps> stuffsResult = GankAPIService.getInstance().dayApps(date).execute().body();
                if(stuffsResult.error || null==stuffsResult.results || null==stuffsResult.results.stuffs){
                    continue;
                }
                for (Stuff stuff:stuffsResult.results.stuffs){
                    if(!saveToDb(realm,stuff)){
                        return fetched;
                    }
                    fetched++;
                }
            }
        }else if(mType.equals(Constants.TYPE.FUN.getApiName())){
            for (String date:dates){
                if(date.equals(after)){
                    continue;
                }
                GankAPI.Result<GankAPI.Funs> stuffsResult = GankAPIService.getInstance().dayFuns(date).execute().body();
                if(stuffsResult.error || null==stuffsResult.results || null==stuffsResult.results.stuffs){
                    continue;
                }
                for (Stuff stuff:stuffsResult.results.stuffs){
                    if(!saveToDb(realm,stuff)){
                        return fetched;
                    }
                    fetched++;
                }
            }
        }else if(mType.equals(Constants.TYPE.OTHERS.getApiName())){
            for (String date:dates){
                if(date.equals(after)){
                    continue;
                }
                GankAPI.Result<GankAPI.Others> stuffsResult = GankAPIService.getInstance().dayOthers(date).execute().body();
                if(stuffsResult.error || null==stuffsResult.results || null==stuffsResult.results.stuffs){
                    continue;
                }
                for (Stuff stuff:stuffsResult.results.stuffs){
                    if(!saveToDb(realm,stuff)){
                        return fetched;
                    }
                    fetched++;
                }
            }
        }else if(mType.equals(Constants.TYPE.WEB.getApiName())){
            for (String date:dates){
                if(date.equals(after)){
                    continue;
                }
                GankAPI.Result<GankAPI.Webs> stuffsResult = GankAPIService.getInstance().dayWebs(date).execute().body();
                if(stuffsResult.error || null==stuffsResult.results || null==stuffsResult.results.stuffs){
                    continue;
                }
                for (Stuff stuff:stuffsResult.results.stuffs){
                    if(!saveToDb(realm,stuff)){
                        return fetched;
                    }
                    fetched++;
                }
            }
        }
        return fetched;
    }

    private int fetchLatest(Realm realm) throws IOException {
        GankAPI.Result<List<Stuff>> result = GankAPIService.getInstance().latesStuff(mType, 20).execute().body();
        if (result.error) {
            return 0;
        }
        int stuffSize = result.results.size();
        for (int i = 0; i < stuffSize; i++) {
            if (!saveToDb(realm, result.results.get(i))) {
                return i;
            }
        }
        return stuffSize;
    }

    //保存数据 把Retrofit获取的数据 通过realm 用事物保存到数据库
    private boolean saveToDb(Realm realm, Stuff stuff) {
        realm.beginTransaction();
        try {
            mTest = realm.copyToRealm(stuff);
//            Log.e(TAG, "mTest"+mTest.getDesc());
        } catch (RealmPrimaryKeyConstraintException e) {
            realm.where(Stuff.class)
                    .equalTo("id", stuff.getId())
                    .findFirst()
                    .setDeleted(false);
            Log.e(TAG, "RealmPrimaryKeyConstraintException", e);
            //记得提交事物 ，不然不会保存
            realm.commitTransaction();
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Failed to save stuff", e);
            realm.cancelTransaction();
            return false;
        }
        realm.commitTransaction();
        return true;
    }
}
