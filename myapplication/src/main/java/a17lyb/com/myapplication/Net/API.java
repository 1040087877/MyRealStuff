package a17lyb.com.myapplication.Net;

import a17lyb.com.myapplication.JavaBean.GirlRoot;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Path;

/**
 * Created by 10400 on 2016/12/21.
 */

public class API {
    static String BASE_URL="http://gank.io/api/";
    static String FULI_RUL=BASE_URL+"data/%E7%A6%8F%E5%88%A9/10/1";

    public interface GirlService{
        @GET("data/%E7%A6%8F%E5%88%A9/{count}/1")
        Call<GirlRoot> getGirlMsg(@Path("count")int count);
    }
    static Retrofit retrofit=new Retrofit.Builder().baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create()).build();
    public static GirlService grilService=retrofit.create(GirlService.class);
}
