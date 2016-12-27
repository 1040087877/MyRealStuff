package a17lyb.com.myapplication.Net;

import android.graphics.Point;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

/**
 * Created by 10400 on 2016/12/22.
 */
public interface ImageFetcher {
    void prefetchImage(String url, Point measured)throws IOException, InterruptedException, ExecutionException;
}
