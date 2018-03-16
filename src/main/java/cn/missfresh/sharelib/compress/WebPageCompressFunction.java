package cn.missfresh.sharelib.compress;

import android.graphics.Bitmap;

import cn.missfresh.sharelib.bean.IShareImageBean;
import cn.missfresh.sharelib.util.BitmapUtil;
import io.reactivex.Observable;
import io.reactivex.functions.Function;

/**
 * File description.
 * 连接模式的图片压缩
 * @author lihongjun
 * @date 2018/1/12
 * @param <T> 继承至 IShareBean
 */

public class WebPageCompressFunction<T extends IShareImageBean> implements Function<Bitmap, Observable<T>> {

    /**
     * 分享数据
     */
    private T mShareBean;

    /**
     * 构造方法
     * @param shareBean 分享数据
     */
    public WebPageCompressFunction(T shareBean) {
        mShareBean = shareBean;
    }

    @Override
    public Observable<T> apply(Bitmap bitmap) throws Exception {
        BitmapUtil bitmapUtil = new BitmapUtil();
        if (bitmap != null) {
            byte[] result = bitmapUtil.getImageThumbnail(bitmap);
            mShareBean.setWebPageData(result);
        }
        return Observable.just(mShareBean);
    }
}
