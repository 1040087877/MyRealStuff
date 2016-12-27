package a17lyb.com.myapplication.JavaBean;

import android.content.Context;
import android.graphics.Point;

import com.bumptech.glide.Glide;
import com.google.gson.annotations.SerializedName;

import java.io.IOException;
import java.util.Date;
import java.util.concurrent.ExecutionException;

import a17lyb.com.myapplication.Net.ImageFetcher;
import io.realm.Realm;
import io.realm.RealmObject;
import io.realm.RealmResults;
import io.realm.Sort;
import io.realm.annotations.PrimaryKey;

/**
 * Created by 10400 on 2016/12/22.
 */

public class Image extends RealmObject{
    @PrimaryKey
    @SerializedName("_id")
    private String id;
    private String url;
    private int width;
    private int height;
    private Date publishedAt;

    public Image() {
    }

    public Image(String id, String url, Date publishedAt) {
        this.id = id;
        this.url = url;
        this.publishedAt = publishedAt;
    }
    //读取所有数据
    public static RealmResults<Image> all(Realm realm){
        return realm.where(Image.class)
                .findAllSorted("publishedAt", Sort.DESCENDING);
    }

    //清除数据
    public static void clearImage(final Context context, Realm realm){
        new Thread(new Runnable() {
            @Override
            public void run() {
                //清除缓存
                Glide.get(context).clearDiskCache();
            }
        }).start();
        final RealmResults<Image> images = realm.where(Image.class).findAll();
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                images.deleteAllFromRealm();
            }
        });
    }

    public static Image persist(Image image,ImageFetcher imageFetcher)
            throws IOException, InterruptedException, ExecutionException {
        Point size=new Point();
        imageFetcher.prefetchImage(image.getUrl(),size);
        image.setWidth(size.x);
        image.setHeight(size.y);
        return image;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public Date getPublishedAt() {
        return publishedAt;
    }

    public void setPublishedAt(Date publishedAt) {
        this.publishedAt = publishedAt;
    }
}
