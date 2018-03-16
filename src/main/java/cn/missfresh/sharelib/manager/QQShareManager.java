package cn.missfresh.sharelib.manager;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.widget.Toast;

import cn.missfresh.sharelib.ModuleConfigureConstant;
import cn.missfresh.sharelib.R;
import cn.missfresh.sharelib.activity.QqShareActivity;
import cn.missfresh.sharelib.base.BaseShareManager;
import cn.missfresh.sharelib.bean.IMediaObject;
import cn.missfresh.sharelib.bean.ShareImage;
import cn.missfresh.sharelib.bean.ShareImageData;
import cn.missfresh.sharelib.bean.ShareWeb;
import cn.missfresh.sharelib.download.IShareImageDownLoad;
import cn.missfresh.sharelib.event.RxBus;
import cn.missfresh.sharelib.event.ShareEventMessage;
import cn.missfresh.sharelib.event.ShareResultType;
import cn.missfresh.sharelib.util.BitmapUtil;
import cn.missfresh.sharelib.util.MediaType;
import cn.missfresh.sharelib.util.ShareImageDataUtil;
import cn.missfresh.sharelib.util.StringUtil;
import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.BiFunction;

/**
 * File description.
 *
 * @author lihongjun
 * @date 2018/3/7
 */

public class QQShareManager <T extends IMediaObject> extends BaseShareManager {

    private final String TAG = "QQShareManager";

    private String imageUrl; // 图片地址链接 可以是网络地址 也可以是本地地址
    private int mediaType; // 内容类型
    private Observable mExtralBitmapObservable; // 额外图片
    private BiFunction<Bitmap,Bitmap,Bitmap> mBitmapOpreatorFuncation; // 额外操作
    private IShareImageDownLoad mShareImageDownLoad; // 下载模式

    /**
     * 图片存放路径
     */
    private String picPath = "";

    public QQShareManager(Context context) {
        super(context);
    }

    /**
     * 额外操作
     * @param extralBitmapObservable
     * @param bitmapOpreatorFuncation
     * @return
     */
    public QQShareManager withExtraOpration(Observable extralBitmapObservable, BiFunction<Bitmap,Bitmap,Bitmap> bitmapOpreatorFuncation) {
        mExtralBitmapObservable = extralBitmapObservable;
        mBitmapOpreatorFuncation = bitmapOpreatorFuncation;
        return this;
    }

    /**
     * 自定义下载模式
     * @param shareImageDownLoad
     * @return
     */
    public QQShareManager withDownLoad(IShareImageDownLoad shareImageDownLoad) {
        mShareImageDownLoad = shareImageDownLoad;
        return this;
    }

    /**
     * 分享到QQ
     * @param shareData
     */
    public void share2Qq(T shareData) {
        // 如果没有读写权限则返回
        if (!havePremiss() || StringUtil.isEmpt(ModuleConfigureConstant.QQ_APPID)) {
            RxBus.getIntanceBus().post(new ShareEventMessage(ShareResultType.SHARE_FAIL));
            return;
        }
        boolean isPoster = false;
        mediaType = shareData.getMediaType();
        Bitmap bitmap = null;
        if (shareData instanceof ShareWeb) {
            title = ((ShareWeb) shareData).getTitle();
            content = ((ShareWeb) shareData).getContent();
            targetUrl = ((ShareWeb) shareData).getTargetUrl();
            imageUrl = ((ShareWeb) shareData).getImageUrl();
        } else if (shareData instanceof ShareImage) {
            imageUrl = ((ShareImage) shareData).getImageUrl();
            bitmap = ((ShareImage) shareData).getOriginBitmap();
            isPoster = true;
        } else {
            RxBus.getIntanceBus().post(new ShareEventMessage(ShareResultType.SHARE_FAIL));
            return;
        }
        // 如果传入的是网络地址 并且不需要额外操作 并且是webpage模式 直接传入网络图片地址给qq分享
        if (bitmap == null && mExtralBitmapObservable == null && mBitmapOpreatorFuncation == null && mediaType == MediaType.MEDIA_TYPE_WEB) {
            QqShareActivity.skipTo(mContext,title,content,targetUrl,imageUrl,mediaType);
            return;
        }

        ShareImageData shareImageData = ShareImageDataUtil.create(imageUrl,shareData.getPlamform(),shareData.getMediaType(),bitmap);
        Observer<ShareImageData> result = new Observer<ShareImageData>() {
            @Override
            public void onSubscribe(Disposable d) {
                showProgressDialog(false);
            }

            @Override
            public void onNext(ShareImageData shareImageData) {
                Bitmap bmp = BitmapFactory.decodeByteArray(shareImageData.getPosterImageData(), 0, shareImageData.getPosterImageData().length);
                saveBitmap(bmp);
            }

            @Override
            public void onError(Throwable e) {
                hideProgressDialog(false);
                RxBus.getIntanceBus().post(new ShareEventMessage(ShareResultType.SHARE_FAIL));
            }

            @Override
            public void onComplete() {
                hideProgressDialog(false);
            }
        };

        if (isPoster) { // 海报模式传入额外操作
            new ContentManager<ShareImageData>(mContext).getShareContent(shareImageData, result
                    ,mShareImageDownLoad,mExtralBitmapObservable,mBitmapOpreatorFuncation);
        } else {
            new ContentManager<ShareImageData>(mContext).getShareContent(shareImageData
                    , result,mShareImageDownLoad);
        }
    }

    /**
     * 权限检查
     *
     */
    private boolean havePremiss() {
        boolean hasePermission = false;
        //判断是否6.0以上的手机   不是就不用
        if (Build.VERSION.SDK_INT >= 23) {
            //判断是否有这个权限
            if (ContextCompat.checkSelfPermission(mContext, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                //2、申请权限: 参数二：权限的数组；参数三：请求码
                Toast.makeText(mContext, R.string.write_peromision_miss,Toast.LENGTH_LONG).show();
                hasePermission = false;
            } else {
                hasePermission = true;
            }
        } else {
            hasePermission = true;
        }
        return hasePermission;
    }


    /**
     * 存放Bitmap至本地文件
     *
     * @param bmp Bitmap
     */
    private void saveBitmap(Bitmap bmp) {
        Observer result = new Observer<String>() {
            @Override
            public void onSubscribe(Disposable d) {
                showProgressDialog(false);
            }

            @Override
            public void onNext(String picPath) {
                imageUrl = picPath;
            }

            @Override
            public void onError(Throwable e) {
                hideProgressDialog();
                RxBus.getIntanceBus().post(new ShareEventMessage(ShareResultType.SHARE_FAIL));
            }

            @Override
            public void onComplete() {
                hideProgressDialog(false);
                QqShareActivity.skipTo(mContext,title,content,targetUrl,imageUrl,mediaType);
            }
        };
        new BitmapUtil().saveBitmapAsFile(bmp,result);
    }

}
