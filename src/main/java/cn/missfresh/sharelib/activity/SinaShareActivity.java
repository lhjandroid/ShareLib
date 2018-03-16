package cn.missfresh.sharelib.activity;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.sina.weibo.sdk.WbSdk;
import com.sina.weibo.sdk.api.ImageObject;
import com.sina.weibo.sdk.api.MultiImageObject;
import com.sina.weibo.sdk.api.TextObject;
import com.sina.weibo.sdk.api.WeiboMultiMessage;
import com.sina.weibo.sdk.auth.AccessTokenKeeper;
import com.sina.weibo.sdk.auth.AuthInfo;
import com.sina.weibo.sdk.auth.Oauth2AccessToken;
import com.sina.weibo.sdk.auth.WbAuthListener;
import com.sina.weibo.sdk.auth.WbConnectErrorMessage;
import com.sina.weibo.sdk.auth.sso.SsoHandler;
import com.sina.weibo.sdk.share.WbShareCallback;
import com.sina.weibo.sdk.share.WbShareHandler;

import java.io.File;
import java.util.ArrayList;

import cn.missfresh.sharelib.ModuleConfigureConstant;
import cn.missfresh.sharelib.event.RxBus;
import cn.missfresh.sharelib.event.ShareEventMessage;
import cn.missfresh.sharelib.event.ShareResultType;
import cn.missfresh.sharelib.manager.SinaShareManager;
import cn.missfresh.sharelib.util.StringUtil;


/**
 * 微博分享对应的Activity
 */
public class SinaShareActivity  extends AppCompatActivity implements WbShareCallback {

    private static final String EXTRA_TITLE = "EXTRA_TITLE";
    private static final String EXTRA_CONTENT = "EXTRA_CONTENT";
    private static final String EXTRA_URL = "EXTRA_URL";
    private static final String EXTRA_THUMB_DATA = "EXTRA_THUMB_DATA";
    private static final String EXTRA_LOCAL_IMAGE = "EXTRA_LOCAL_IMAGE";

    private String title; // 标题
    private String content; // 内容
    private String url; // 连接地址
    private byte[] thumbData; // webpage模式时使用
    private String localImage; // 本地图片地址

    private Oauth2AccessToken mAccessToken; // token
    private SsoHandler mSsoHandler; // 授权

    /**
     * 分享回调
     */
    private WbShareHandler shareHandler;

    public static   void skipTo(Context context,String title, String content, String url,byte[] thumbData) {
        skipTo(context,title,content,url,thumbData,null);
    }

    /**
     * 跳转方法
     *
     * @param context 上下文
     * @param localPicPath 本地图片地址
     */
    public static   void skipTo(Context context,String title, String content, String url,byte[] thumbData,String localPicPath) {
        Intent intent = new Intent(context, SinaShareActivity.class);
        intent.putExtra(EXTRA_TITLE,title);
        intent.putExtra(EXTRA_CONTENT,content);
        intent.putExtra(EXTRA_URL,url);
        intent.putExtra(EXTRA_THUMB_DATA,thumbData);
        if (!StringUtil.isEmpt(localPicPath)) {
            intent.putExtra(EXTRA_LOCAL_IMAGE,localPicPath);
        }
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initData();
        registerToSina();
    }

    /**
     * 初始化数据
     */
    private void initData() {
        if (getIntent().hasExtra(EXTRA_TITLE)) {
            title = getIntent().getStringExtra(EXTRA_TITLE);
        }
        if (getIntent().hasExtra(EXTRA_CONTENT)) {
            content = getIntent().getStringExtra(EXTRA_CONTENT);
        }
        if (getIntent().hasExtra(EXTRA_URL)) {
            url = getIntent().getStringExtra(EXTRA_URL);
        }
        if (getIntent().hasExtra(EXTRA_THUMB_DATA)) {
            thumbData = getIntent().getByteArrayExtra(EXTRA_THUMB_DATA);
        }
        if (getIntent().hasExtra(EXTRA_LOCAL_IMAGE)) {
            localImage = getIntent().getStringExtra(EXTRA_LOCAL_IMAGE);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        shareHandler.doResultIntent(intent, this);
    }

    /**
     * 微博分享注册
     */
    private void registerToSina() {
        WbSdk.install(this, new AuthInfo(this, ModuleConfigureConstant.APP_KEY, ModuleConfigureConstant.REDIRECT_URL, ModuleConfigureConstant.SCOPE));
        shareHandler = new WbShareHandler(this);
        shareHandler.registerApp();
        shareHandler.setProgressColor(0xff33b5e5);

        mSsoHandler = new SsoHandler(this);
        mSsoHandler.authorize(new SelfWbAuthListener());
    }

    /**
     * 微博分享
     *
     */
    private void share2Sina() {
        boolean haseText = !StringUtil.isEmpt(title) || !StringUtil.isEmpt(content);
        boolean haseImage = (SinaShareManager.posterData != null && SinaShareManager.posterThumbData != null) || !StringUtil.isEmpt(localImage);
        if (!haseText && !haseImage) {
            RxBus.getIntanceBus().post(new ShareEventMessage(ShareResultType.SHARE_FAIL));
            finish();
        } else {
            share(haseText,haseImage);
        }
    }

    /**
     * 微博海报分享
     *
     * @param haseText 是否带有text
     *
     */
    private void share(boolean haseText,boolean haseImage) {
        WeiboMultiMessage weiboMessage = new WeiboMultiMessage();
        if (haseText) {
            weiboMessage.textObject = getTextObject();
        }
        // 图片模式并且本地图片地址不为空
        if (haseImage && !StringUtil.isEmpt(localImage)) {
            weiboMessage.multiImageObject = getMultiImageObject();
        } else {
            weiboMessage.imageObject = getImageObject(SinaShareManager.posterData, SinaShareManager.posterThumbData);
        }
        shareHandler.shareMessage(weiboMessage, false);
    }

    /**
     * 文本分享内容
     * @return
     */
    private TextObject getTextObject() {
        TextObject textObject = new TextObject();
        textObject.text = content + url;
        textObject.title = title;
        textObject.actionUrl = url;
        textObject.thumbData = thumbData;
        return textObject;
    }

    /**
     * 获取图片分享内容
     * @param imageData
     * @param thumbData
     * @return
     */
    private ImageObject getImageObject(byte[] imageData,byte[] thumbData) {
        ImageObject imageObject = new ImageObject();
        imageObject.imageData = imageData;
        imageObject.thumbData = thumbData;
        return imageObject;
    }

    private MultiImageObject getMultiImageObject() {
        File file = new File(localImage);
        MultiImageObject multiImageObject = new MultiImageObject();
        ArrayList<Uri> pathList = new ArrayList<>();
        pathList.add(Uri.fromFile(new File(localImage)));
        multiImageObject.setImageList(pathList);
        return multiImageObject;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (mSsoHandler != null) {
            mSsoHandler.authorizeCallBack(requestCode, resultCode, data);
        }
    }

    @Override
    public void onWbShareSuccess() {
        RxBus.getIntanceBus().post(new ShareEventMessage(ShareResultType.SHARE_SUCCESS));
        this.finish();
    }

    @Override
    public void onWbShareCancel() {
        RxBus.getIntanceBus().post(new ShareEventMessage(ShareResultType.SHARE_CANCLE));
        this.finish();
    }

    @Override
    public void onWbShareFail() {
        RxBus.getIntanceBus().post(new ShareEventMessage(ShareResultType.SHARE_CANCLE));
        this.finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 清除缓存数据
        SinaShareManager.onDestory();
    }

    private class SelfWbAuthListener implements WbAuthListener {
        @Override
        public void onSuccess(final Oauth2AccessToken token) {
            SinaShareActivity.this.runOnUiThread(() -> {
                mAccessToken = token;
                if (mAccessToken.isSessionValid()) {
                    // 保存 Token 到 SharedPreferences
                    AccessTokenKeeper.writeAccessToken(SinaShareActivity.this, mAccessToken);
                    Toast.makeText(SinaShareActivity.this,"验证成功", Toast.LENGTH_SHORT).show();
                    share2Sina();
                }
            });
        }

        @Override
        public void cancel() {
            Toast.makeText(SinaShareActivity.this, "登录验证失败", Toast.LENGTH_LONG).show();
            finish();
        }

        @Override
        public void onFailure(WbConnectErrorMessage errorMessage) {
            Toast.makeText(SinaShareActivity.this, errorMessage.getErrorMessage(), Toast.LENGTH_LONG).show();
            finish();
        }
    }
}
