package cn.missfresh.sharelib.manager;

import android.content.Context;
import android.graphics.Bitmap;

import cn.missfresh.sharelib.bean.IShareImageBean;
import cn.missfresh.sharelib.compress.ImageCompressFunction;
import cn.missfresh.sharelib.download.GlideDownLoadObersevable;
import cn.missfresh.sharelib.download.IShareImageDownLoad;
import cn.missfresh.sharelib.util.RxSchedulersHelper;
import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.functions.BiFunction;

/**
 * Created by lihongjun on 2018/1/4.
 * 分享图片内容准备
 */

public class ContentManager<T extends IShareImageBean> {

    private final String TAG = "ContentManager";

    private Context mContext;

    public ContentManager(Context context) {
        mContext = context;
    }



    /**
     * 获取分享结构图
     * @param shareBean 需要加工的数据
     * @param result 结果通知
     * @param shareImageDownLoad 图片下载方式
     */
    public void getShareContent(T shareBean, Observer<T> result,IShareImageDownLoad shareImageDownLoad) {
        getShareContent(shareBean,result,shareImageDownLoad,null,null);
    }

    /**
     * 获取分享结构图
     * @param shareBean 需要加工的数据
     * @param result 结果通知
     * @param shareImageDownLoad 图片下载方式
     * @param extralBitmapObservable 额外的bitmap
     * @param bitmapOpreatorFuncation 两个bitmap的操作 如拼接 或者贴图
     */
    public void getShareContent(T shareBean, Observer<T> result, IShareImageDownLoad shareImageDownLoad, Observable extralBitmapObservable
            , BiFunction<Bitmap,Bitmap,Bitmap> bitmapOpreatorFuncation) {
        if (shareBean == null || result == null || shareImageDownLoad == null) {
            return;
        }
        if (extralBitmapObservable == null || bitmapOpreatorFuncation == null) {
            new GlideDownLoadObersevable(mContext,shareBean,shareImageDownLoad)
                    .flatMap(ImageCompressFunction.getCompressFunction(shareBean))
                    .compose(RxSchedulersHelper.io_main())
                    .subscribe(result);
        } else {
            Observable.zip(new GlideDownLoadObersevable(mContext,shareBean,shareImageDownLoad)
                    , extralBitmapObservable, bitmapOpreatorFuncation)
                    .flatMap(ImageCompressFunction.getCompressFunction(shareBean))
                    .compose(RxSchedulersHelper.io_main())
                    .subscribe(result);
        }
    }
}
