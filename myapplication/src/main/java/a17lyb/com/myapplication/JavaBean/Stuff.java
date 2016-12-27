package a17lyb.com.myapplication.JavaBean;

import com.google.gson.annotations.SerializedName;

import java.text.ParseException;
import java.util.Date;

import a17lyb.com.myapplication.Utils.DateUtil;
import a17lyb.com.myapplication.db.SearchBean;
import io.realm.Realm;
import io.realm.RealmObject;
import io.realm.RealmResults;
import io.realm.Sort;
import io.realm.annotations.PrimaryKey;

/**
 * Created by 10400 on 2016/12/24.
 */

public class Stuff extends RealmObject {
    private static final String TAG = "Stuff";
    @PrimaryKey
    @SerializedName("_id")
    private String id;
    private String desc;
    private Date publishedAt, lastChanged;
    private String type;
    private String url;
    //发布者
    private String who;
    private boolean isLiked, isDeleted;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public Date getPublishedAt() {
        return publishedAt;
    }

    public void setPublishedAt(Date publishedAt) {
        this.publishedAt = publishedAt;
    }

    public Date getLastChanged() {
        return lastChanged;
    }

    public void setLastChanged(Date lastChanged) {
        this.lastChanged = lastChanged;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getWho() {
        return who;
    }

    public void setWho(String who) {
        this.who = who;
    }

    public boolean isLiked() {
        return isLiked;
    }

    public void setLiked(boolean liked) {
        isLiked = liked;
    }

    public boolean isDeleted() {
        return isDeleted;
    }

    public static String getTAG() {
        return TAG;
    }

    public Stuff() {
    }

    public Stuff(String id, String desc, String type, String url, String who, Date publishedAt) {
        this.id = id;
        this.desc = desc;
        this.publishedAt = publishedAt;
        this.lastChanged = lastChanged;
        this.type = type;
        this.url = url;
        this.who = who;
        this.isLiked = false;
        this.isDeleted = false;
    }

    public static Stuff fromSearch(SearchBean bean) throws ParseException {
        return new Stuff(
                bean.getUrl(),
                bean.getDesc(),
                bean.getType(),
                bean.getUrl(),
                bean.getWho(),
                DateUtil.parse(bean.getPublishedAt())
        );
    }

    public static RealmResults<Stuff> all(Realm realm, String type) {
        return realm.where(Stuff.class)
                .equalTo("type", type)
                .equalTo("isDeleted", false)
                .findAllSorted("publishedAt", Sort.DESCENDING);
    }

    public static RealmResults<Stuff> collections(Realm realm) {
        return realm.where(Stuff.class)
                .equalTo("isLiked", true)
                .findAllSorted("publishedAt", Sort.DESCENDING);
    }

    public static void clearAll(Realm realm) {
        final RealmResults<Stuff> allStuff = realm.where(Stuff.class).findAll();
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                allStuff.deleteAllFromRealm();
            }
        });
    }

    public static void clearType(Realm realm, String type) {
        final RealmResults<Stuff> unCollected = realm.where(Stuff.class)
                .equalTo("type", type)
                .equalTo("isLiked", false)
                .findAll();
        final RealmResults<Stuff> collected = realm.where(Stuff.class)
                .equalTo("type", type)
                .equalTo("isLiked", true)
                .findAll();
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                unCollected.deleteAllFromRealm();
                for (Stuff stuff : collected) {
                    stuff.setDeleted(true);
                }
            }
        });
    }

    public static Stuff checkSearch(Realm realm, String url) {
        return realm.where(Stuff.class)
                .equalTo("id", url)
                .findFirst();
    }

    public void setDeleted(boolean deleted) {
        isDeleted = deleted;
    }

}
