package cn.missfresh.sharelib.ui;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetDialog;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import java.util.List;

import cn.missfresh.sharelib.R;
import cn.missfresh.sharelib.bean.IMediaObject;
import cn.missfresh.sharelib.util.PlamForm;

/**
 * 分享控件
 * Created by lihongjun on 2018/1/11.
 */

public class ShareDialog<T extends IMediaObject> extends BottomSheetDialog implements View.OnClickListener {

    private final String TAG = "ShareDialog";
    private List<T> mPlamforms; // 分享平台集合 集成 微信 微博 QQ
    private TextView mTvWxMoment; // 微信朋友圈
    private TextView mTvWxChat; // 微信好友
    private TextView mTvWeiBo; // 微博
    private TextView mTvQQ; // QQ
    private int mLayoutResId; //默认布局
    private View mCustomView; // 默认分享dialog自定义布局
    private float mDimAmount = 0.5f; // 对话框的蒙层清晰度
    private OnPlamFormClickListener mPlamFormClickListener;

    public ShareDialog(@NonNull Context context) {
        super(context);
        mLayoutResId = R.layout.share_dialog_share_view;
    }


    /**
     * 自定义分享View
     * @param customView
     */
    public void setCustomView(View customView) {
        mCustomView = customView;
    }

    /**
     * 设置对话框的蒙层清晰度
     * @param dimAmount
     */
    public void setDimAmount(float dimAmount) {
        mDimAmount = dimAmount;
    }

    /**
     * 设置平台
     * @param plamforms
     */
    public void setPlamforms(List<T> plamforms) {
        mPlamforms = plamforms;
    }

    /**
     * 设置平台点击回调
     * @param plamFormClickListener 设置分享平台点击回调
     */
    public void setPlamFormClickListener(OnPlamFormClickListener plamFormClickListener) {
        mPlamFormClickListener = plamFormClickListener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (mCustomView == null) {
            setContentView(mLayoutResId);
        } else {
            setContentView(mCustomView);
        }
        WindowManager m = getWindow().getWindowManager();
        Display d = m.getDefaultDisplay();
        WindowManager.LayoutParams p = getWindow().getAttributes();
        p.width = d.getWidth(); //设置dialog的宽度为当前手机屏幕的宽度
        p.dimAmount = mDimAmount;
        getWindow().setAttributes(p);

        initView();
        initData();
    }

    /**
     * 初始化布局
     */
    private void initView() {
        mTvWxMoment = findViewById(R.id.btn_share_wechat_moments);
        mTvWxChat = findViewById(R.id.btn_share_wechat);
        mTvWeiBo = findViewById(R.id.btn_share_weibo);
        mTvQQ = findViewById(R.id.btn_share_qq);
        mTvWxMoment.setOnClickListener(this);
        mTvWxChat.setOnClickListener(this);
        mTvWeiBo.setOnClickListener(this);
        mTvQQ.setOnClickListener(this);
    }

    /**
     * 初始化数据
     */
    private void initData() {
        // 如果未设置平台
        if (mPlamforms == null || mPlamforms.size() == 0) {
            try {
                throw new Exception("请设置分享平台");
            } catch (Exception e) {
                e.printStackTrace();
            }
            return;
        }

        for (T t: mPlamforms) {
            checkPlamforVisibility(t.getPlamform());
        }
    }

    /**
     * 检测平台是否应该显示
     * @param plamform 分享平台
     */
    private void checkPlamforVisibility(int plamform) {
        switch (plamform) {
            case PlamForm.WX_MOUMENT:
                mTvWxMoment.setVisibility(View.VISIBLE);
                break;
            case PlamForm.WX_CHAT:
                mTvWxChat.setVisibility(View.VISIBLE);
                break;
            case PlamForm.SINA:
                mTvWeiBo.setVisibility(View.VISIBLE);
                break;
            case PlamForm.QQ:
                mTvQQ.setVisibility(View.VISIBLE);
                break;
        }
    }

    /**
     * Called when a view has been clicked.
     *
     * @param v The view that was clicked.
     */
    @Override
    public void onClick(View v) {
        if (mPlamFormClickListener == null) {
            new IllegalArgumentException("mPlamFormClickListener can not be null");
            return;
        }
        //平台类型
        int plamformType = -1;
        int i = v.getId();
        if (i == R.id.btn_share_wechat_moments) {
            plamformType = PlamForm.WX_MOUMENT;

        } else if (i == R.id.btn_share_wechat) {
            plamformType = PlamForm.WX_CHAT;

        } else if (i == R.id.btn_share_weibo) {
            plamformType = PlamForm.SINA;

        } else if (i == R.id.btn_share_qq) {
            plamformType = PlamForm.QQ;
        }

        mPlamFormClickListener.onPlamFormClick(v,plamformType);
    }


    /**
     * 点击分享平台回调
     */
    public interface OnPlamFormClickListener {

        /**
         *
         * @param view 点击的View
         * @param plamForm 点击的平台
         */
        void onPlamFormClick(View view, int plamForm);
    }
}
