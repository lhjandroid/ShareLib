package cn.missfresh.sharelib.manager;

import android.content.Context;
import android.graphics.Bitmap;

import com.tencent.mm.opensdk.modelmsg.SendMessageToWX;
import com.tencent.mm.opensdk.modelmsg.WXImageObject;
import com.tencent.mm.opensdk.modelmsg.WXMediaMessage;
import com.tencent.mm.opensdk.modelmsg.WXMiniProgramObject;
import com.tencent.mm.opensdk.modelmsg.WXWebpageObject;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;

import cn.missfresh.sharelib.ModuleConfigureConstant;
import cn.missfresh.sharelib.base.BaseShareManager;
import cn.missfresh.sharelib.bean.IMediaObject;
import cn.missfresh.sharelib.bean.ShareImage;
import cn.missfresh.sharelib.bean.ShareImageData;
import cn.missfresh.sharelib.bean.ShareMiniApp;
import cn.missfresh.sharelib.bean.ShareWeb;
import cn.missfresh.sharelib.download.IShareImageDownLoad;
import cn.missfresh.sharelib.event.RxBus;
import cn.missfresh.sharelib.event.ShareEventMessage;
import cn.missfresh.sharelib.event.ShareResultType;
import cn.missfresh.sharelib.util.MediaType;
import cn.missfresh.sharelib.util.PlamForm;
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

public class WxShareManager <T extends IMediaObject> extends BaseShareManager {

    private IWXAPI wxapi;

    private String miniRoutineId;
    private String miniRoutinePath;

    private Observable mExtralBitmapObservable; // 额外图片
    private BiFunction<Bitmap,Bitmap,Bitmap> mBitmapOpreatorFuncation; // 额外操作



    private IShareImageDownLoad mShareImageDownLoad;

    public WxShareManager(Context context) {
        super(context);
        wxapi = WXAPIFactory.createWXAPI(context, null);
        wxapi.registerApp(ModuleConfigureConstant.WECHAT_APPID);
    }

    /**
     * 额外的图片操作
     * @param extralBitmapObservable
     * @param bitmapOpreatorFuncation
     * @return
     */
    public WxShareManager withExtraOpration(Observable extralBitmapObservable, BiFunction<Bitmap,Bitmap,Bitmap> bitmapOpreatorFuncation) {
        mExtralBitmapObservable = extralBitmapObservable;
        mBitmapOpreatorFuncation = bitmapOpreatorFuncation;
        return this;
    }

    /**
     * 下载模式
     * @param shareImageDownLoad
     * @return
     */
    public WxShareManager withDownload(IShareImageDownLoad shareImageDownLoad) {
        mShareImageDownLoad = shareImageDownLoad;
        return this;
    }

    /**
     * 分享到微信
     * @param mediaObject
     */
    public void shareToWx(final T mediaObject) {
        if(StringUtil.isEmpt(ModuleConfigureConstant.WECHAT_APPID) || !wxapi.isWXAppInstalled()) { // 未安装微信直接退出分享
            RxBus.getIntanceBus().post(new ShareEventMessage(ShareResultType.SHARE_FAIL));
            return;
        }

        String imageUrl = ""; // 图片地址
        Bitmap originBitmap = null; // 主图
        boolean isPoster = false;

        if (mediaObject instanceof ShareWeb) {
            title = ((ShareWeb) mediaObject).getTitle();
            content = ((ShareWeb) mediaObject).getContent();
            imageUrl = ((ShareWeb) mediaObject).getImageUrl();
        } else if (mediaObject instanceof ShareImage) {
            imageUrl = ((ShareImage) mediaObject).getImageUrl();
            originBitmap = ((ShareImage) mediaObject).getOriginBitmap();
            isPoster = true;
        } else if (mediaObject instanceof ShareMiniApp) {
            title = ((ShareMiniApp) mediaObject).getTitle();
            content = ((ShareMiniApp) mediaObject).getContent();
            miniRoutineId = ((ShareMiniApp) mediaObject).getMiniRoutineId();
            miniRoutinePath = ((ShareMiniApp) mediaObject).getMiniRoutinePath();
            targetUrl = ((ShareMiniApp) mediaObject).getTargeUrl();
        } else {
            RxBus.getIntanceBus().post(new ShareEventMessage(ShareResultType.SHARE_FAIL));
            return;
        }
        ShareImageData shareImageData = ShareImageDataUtil.create(imageUrl,mediaObject.getPlamform(),mediaObject.getMediaType(),originBitmap);
        Observer<ShareImageData> result = new Observer<ShareImageData>() {
            @Override
            public void onSubscribe(Disposable d) {
                showProgressDialog(false);
            }

            @Override
            public void onNext(ShareImageData shareImageData) {
                int mediaType = mediaObject.getMediaType();
                WXMediaMessage msg = new WXMediaMessage();
                msg.title = title;
                msg.description = content;
                WXMediaMessage.IMediaObject iMediaObject = null;
                switch (mediaType) {
                    case MediaType.MEDIA_TYPE_WEB: {
                        msg.thumbData = shareImageData.getWebPageThumbData();
                        iMediaObject = new WXWebpageObject(((ShareWeb) mediaObject).getTargetUrl());
                        break;
                    }
                    case MediaType.MEDIA_TYPE_IMG: {
                        msg.thumbData = shareImageData.getPosterThumbData();
                        iMediaObject = new WXImageObject(shareImageData.getPosterImageData());
                        break;
                    }
                    case MediaType.MEDIA_TYPE_MINI_APP: {
                        WXMiniProgramObject miniProgramObject = new WXMiniProgramObject();
                        miniProgramObject.webpageUrl = targetUrl;//低版本微信将打开该URL
                        miniProgramObject.userName = miniRoutineId;//跳转小程序的原始ID
                        //小程序的path
                        miniProgramObject.path = miniRoutinePath;
                        miniProgramObject.withShareTicket = true;
                        iMediaObject = miniProgramObject;
                        break;
                    }
                }
                msg.mediaObject = iMediaObject;
                sendShareRequest(genWeiXinScene(mediaObject.getPlamform()),msg);
            }

            @Override
            public void onError(Throwable e) {
                e.printStackTrace();
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
     * 获取scene
     * @param shareType
     * @return
     */
    private int genWeiXinScene(int shareType) {
        int rs = 0;
        if (shareType == PlamForm.WX_CHAT) {
            rs = SendMessageToWX.Req.WXSceneSession;
        } else if (shareType == PlamForm.WX_MOUMENT) {
            rs = SendMessageToWX.Req.WXSceneTimeline;
        }
        return rs;
    }

    /**
     * 分享到微信
     * @param flag 朋友圈还是好友
     * @param msg
     */
    private void sendShareRequest(int flag, WXMediaMessage msg) {
        SendMessageToWX.Req req = new SendMessageToWX.Req();
        req.transaction = String.valueOf(System.currentTimeMillis());
        req.message = msg;
        req.scene = flag;
        wxapi.sendReq(req);
    }
}
