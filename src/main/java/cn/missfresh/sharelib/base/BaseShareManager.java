package cn.missfresh.sharelib.base;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.KeyEvent;

/**
 * File description.
 *
 * @author lihongjun
 * @date 2018/3/9
 */

public class BaseShareManager {

    public String title = ""; // 标题
    public String content = ""; // 文本年内容
    public String targetUrl = ""; // 跳转链接

    protected ProgressDialog progressDialog;
    protected Context mContext;

    public BaseShareManager(Context context) {
        mContext = context;
    }

    public void showProgressDialog(boolean cancelable) {
        initProgressDialog(cancelable);
        if (!((Activity)mContext).isFinishing() && !progressDialog.isShowing()) {
            progressDialog.show();
        }
    }

    public void showProgressDialog() {
        showProgressDialog(false);
    }

    public void hideProgressDialog(boolean cancelable) {
        initProgressDialog(cancelable);
        if (progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }

    public void hideProgressDialog() {
        initProgressDialog(false);
        if (progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }

    private void initProgressDialog(boolean cancelable) {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(mContext);
            progressDialog.setCanceledOnTouchOutside(cancelable);
            progressDialog.setOnKeyListener(new DialogInterface.OnKeyListener() {

                @Override
                public boolean onKey(DialogInterface dialog, int keyCode,
                                     KeyEvent event) {
                    return true;
                }
            });
            progressDialog.setMessage("请稍候...");
            progressDialog.setCancelable(cancelable);
            progressDialog.dismiss();
        }
    }
}
