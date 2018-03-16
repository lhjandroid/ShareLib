package cn.missfresh.sharelib.download;

import android.content.Context;
import android.graphics.Bitmap;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;


/**
 * File description.
 * 默认的gelide图片下载方式
 * @author lihongjun
 * @date 2018/1/12
 */

public class GlideImageDownload implements IShareImageDownLoad {

    @Override
    public Bitmap dowwnload(Context context, String url) throws Exception {
        RequestOptions options = new RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL);
        return Glide.with(context)
                .asBitmap()
                .load(url)
                .apply(options)
                .submit()
                .get();
    }
}
