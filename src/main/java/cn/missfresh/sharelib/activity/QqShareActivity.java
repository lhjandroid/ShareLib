package cn.missfresh.sharelib.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.tencent.connect.share.QQShare;
import com.tencent.tauth.IUiListener;
import com.tencent.tauth.Tencent;
import com.tencent.tauth.UiError;

import cn.missfresh.sharelib.ModuleConfigureConstant;
import cn.missfresh.sharelib.event.RxBus;
import cn.missfresh.sharelib.event.ShareEventMessage;
import cn.missfresh.sharelib.event.ShareResultType;
import cn.missfresh.sharelib.util.MediaType;


/**
 * QQ分享对应的透明Activity
 */
public class QqShareActivity extends AppCompatActivity implements IUiListener {

    private static final String EXTRA_TITLE = "EXTRA_TITLE";
    private static final String EXTRA_CONTENT = "EXTRA_CONTENT";
    private static final String EXTRA_TARGET_URL = "EXTRA_TARGET_URL";
    private static final String EXTRA_IMAGE_URL = "EXTRA_IMAGE_URL";
    private static final String EXTRA_MEDIA_TYPE = "EXTRA_MEDIA_TYPE";

    private String title; // 标题
    private String content; // 分享内容
    private String targetUrl; // 跳转地址
    private String imageUrl; // 图片地址
    private int mediaType; // 内容类型

    /**
     * 成员属性  Tencent
     */
    private Tencent mTencent;

    /**
     * 跳转逻辑
     *
     * @param context 上下文
     */
    public static void skipTo(Context context, String title, String content, String targetUrl, String imageUrl, int mediaType) {
        Intent intent = new Intent(context, QqShareActivity.class);
        intent.putExtra(EXTRA_TITLE, title);
        intent.putExtra(EXTRA_CONTENT, content);
        intent.putExtra(EXTRA_TARGET_URL, targetUrl);
        intent.putExtra(EXTRA_IMAGE_URL, imageUrl);
        intent.putExtra(EXTRA_MEDIA_TYPE, mediaType);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        registerToQq();
        getData();
        share2Qq();
    }

    private void getData() {
        if (getIntent().hasExtra(EXTRA_TITLE)) {
            title = getIntent().getStringExtra(EXTRA_TITLE);
        }
        if (getIntent().hasExtra(EXTRA_CONTENT)) {
            content = getIntent().getStringExtra(EXTRA_CONTENT);
        }
        if (getIntent().hasExtra(EXTRA_TARGET_URL)) {
            targetUrl = getIntent().getStringExtra(EXTRA_TARGET_URL);
        }
        if (getIntent().hasExtra(EXTRA_IMAGE_URL)) {
            imageUrl = getIntent().getStringExtra(EXTRA_IMAGE_URL);
        }
        if (getIntent().hasExtra(EXTRA_MEDIA_TYPE)) {
            mediaType = getIntent().getIntExtra(EXTRA_MEDIA_TYPE, MediaType.MEDIA_TYPE_WEB);
        }
    }

    /**
     * QQ的注册
     */
    private void registerToQq() {
        mTencent = Tencent.createInstance(ModuleConfigureConstant.QQ_APPID, this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (null != mTencent) {
            Tencent.onActivityResultData(requestCode, resultCode, data, this);
        }
    }

    /**
     * 分享到QQ
     */
    private void share2Qq() {
        switch (mediaType) {
            case MediaType.MEDIA_TYPE_WEB:
                shareWeb();
                break;
            case MediaType.MEDIA_TYPE_IMG:
                shareImage();
                break;
            default:
                RxBus.getIntanceBus().post(new ShareEventMessage(ShareResultType.SHARE_FAIL));
                finish();
                break;
        }
    }

    /**
     * QQ链接分享
     */
    private void shareWeb() {
        Bundle params = new Bundle();
        params.putInt(QQShare.SHARE_TO_QQ_KEY_TYPE, QQShare.SHARE_TO_QQ_TYPE_DEFAULT);
        params.putString(QQShare.SHARE_TO_QQ_TITLE, title);
        params.putString(QQShare.SHARE_TO_QQ_SUMMARY, content);
        params.putString(QQShare.SHARE_TO_QQ_TARGET_URL, targetUrl);
        params.putString(QQShare.SHARE_TO_QQ_IMAGE_URL, imageUrl);
        mTencent.shareToQQ(this, params, this);
    }

    /**
     * QQ海报分享
     */
    private void shareImage() {
        Bundle params = new Bundle();
        params.putString(QQShare.SHARE_TO_QQ_IMAGE_LOCAL_URL, imageUrl);
        params.putInt(QQShare.SHARE_TO_QQ_KEY_TYPE, QQShare.SHARE_TO_QQ_TYPE_IMAGE);
        mTencent.shareToQQ(this, params, this);
    }

    @Override
    public void onComplete(Object o) {
        RxBus.getIntanceBus().post(new ShareEventMessage(ShareResultType.SHARE_SUCCESS));
        finish();
    }

    @Override
    public void onError(UiError uiError) {
        RxBus.getIntanceBus().post(new ShareEventMessage(ShareResultType.SHARE_FAIL));
        finish();
    }

    @Override
    public void onCancel() {
        RxBus.getIntanceBus().post(new ShareEventMessage(ShareResultType.SHARE_CANCLE));
        finish();
    }
}
