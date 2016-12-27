package a17lyb.com.myapplication;

import android.app.Application;

import io.realm.Realm;
import io.realm.RealmConfiguration;

/**
 * Created by Ivor on 2016/2/12.
 */
public class APP extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Realm.setDefaultConfiguration(new RealmConfiguration.Builder(this)
                .schemaVersion(2)
                .deleteRealmIfMigrationNeeded()
                .build());
    }
}
