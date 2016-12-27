package a17lyb.com.myapplication.Net;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import a17lyb.com.myapplication.Utils.DateUtil;
import io.realm.RealmObject;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by 10400 on 2016/12/22.
 */

public class GankAPIService{
    private static volatile GankAPI sGankAPI;
    private static final Gson gson=new GsonBuilder()
            .setDateFormat(DateUtil.DATE_FORMAT_WHOLE)
            .setExclusionStrategies(new ExclusionStrategy() {
                @Override
                public boolean shouldSkipField(FieldAttributes f) {
                    return f.getDeclaringClass().equals(RealmObject.class);
                }

                @Override
                public boolean shouldSkipClass(Class<?> clazz) {
                    return false;
                }
            }).create();

    private static final Retrofit girlsRetrofit=new Retrofit.Builder()
            .baseUrl(GankAPI.BASE_URL)
            //addConverterFactory()该方法是设置解析器，即上面提到的GsonConverterFactory
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build();

    public GankAPIService() {
    }

    public static GankAPI getInstance(){
        if(sGankAPI==null){
            synchronized (GankAPIService.class){
                if(sGankAPI==null){
                    sGankAPI=girlsRetrofit.create(GankAPI.class);
                }
            }
        }
        return sGankAPI;
    }
}
