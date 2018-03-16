# ShareLib
集成微信微博QQ分享功能

1.接入工程
在主工程的build.gradle 中添加maven仓库地址

```maven { url "https://dl.bintray.com/thelasterstar/maven/" }``` // 主要针对微博分享sdk
并添加sharelib依赖
2.初始化分享sdk(配置分享sdk key值
// 初始化分享sdk各个平台的注册key值
```ShareManager.initWxShareSdk("wxappid");```
```ShareManager.initSinaSdk("新浪AppId","重定向地址","scrop");```
```ShareManager.initQqSdk("qqAppId","分享成功后返回按钮的名称");```

3.构造分享平台及数据数据
// 构建分享平台数据
SharePlamFormData data1 = new SharePlamFormData()
 .withQqMessageBuilder(new SharePlamFormData.QQMessageBuilder().buidWebMessage("http://www.baidu.com","http://f8.topitme.com/8/25/80/1125177570eea80258o.jpg","标题","内容"))
 .withWxChatMessageBuilder(new SharePlamFormData.WxChatMessageBuilder().buildedrImageMessage("http://f8.topitme.com/8/25/80/1125177570eea80258o.jpg",null))
 .withWxMomentMessageBuilder(new SharePlamFormData.WxMomentMessageBuilder().buidWebMessage("http://www.baidu.com","http://f8.topitme.com/8/25/80/1125177570eea80258o.jpg","标题","内容"))
 .withSinaMessageBuilder(new SharePlamFormData.SinaMessageBuilder().builderImageWithTextMessage("标题","内容","http://www.baidu.com","http://f8.topitme.com/8/25/80/1125177570eea80258o.jpg"));
QQMessageBuilder 构造分享到QQ的数据
WxChatMessageBuilder 构造分享到微信好友的分享数据
WxMomentMessageBuilder 构造分享到朋友圈的分享数据
SinaMessageBuilder 构造分享到新浪微博的分享数据
可以根据需求构造需要的分享平台，如
 .withQqMessageBuilder(new SharePlamFormData.QQMessageBuilder().buidWebMessage("http://www.baidu.com","http://f8.topitme.com/8/25/80/1125177570eea80258o.jpg","标题","内容"))
 .withWxChatMessageBuilder(new SharePlamFormData.WxChatMessageBuilder().buildedrImageMessage("http://f8.topitme.com/8/25/80/1125177570eea80258o.jpg",null))
 .withWxMomentMessageBuilder(new SharePlamFormData.WxMomentMessageBuilder().buidWebMessage("http://www.baidu.com","http://f8.topitme.com/8/25/80/1125177570eea80258o.jpg","标题","内容"))
此种情况只会添加 QQ 微信好友 微信朋友圈 三个分享按钮.

4.发起分享
4.1 直接部分不调起分享控件
new ShareManager(this)
 .withPlamFormData(data1.getPlamFormDatas())
 .share();
此种分享只会分享平台集合数据的第一项数据。
4.2 调起分享控件进行分享
new ShareManager(this)
 .withPlamFormData(data1.getPlamFormDatas())
 .open();
5.自定义
分享lib支持自定义分享控件的布局 但平台按钮ID需固定
QQ：btn_share_qq 微信好友：btn_share_wechat 微信朋友圈：btn_share_wechat_moments 微博分享：btn_share_weibo
View customView = View.inflate(MainActivity.this,R.layout.view_custom_dialog,null);
        final ShareManager shareManager = new ShareManager(MainActivity.this)
                .withCustomDialogView(customView) // 自定义view
                .withDialogDimAmount(0)
                .withPlamFormData(data1.getPlamFormDatas())
                .withPlamformClickListener(new ShareDialog.OnPlamFormClickListener() { // 点击平台按钮动作回调 可以用于打点等
                    @Override
                    public void onPlamFormClick(View view, int plamForm) {
                        Toast.makeText(MainActivity.this, "plamForm:" + plamForm, Toast.LENGTH_LONG).show();
                    }
                });
自定义图片变换操作
// 额外图片的获取
private Observable<Bitmap> dowloadExtraObservable = Observable.create(emitter -> {
        Bitmap bitmap = Glide.with(getApplicationContext())
                .asBitmap()
                .load("https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1515476758278&di=1a65b298738b7467faff309b7df72fb3&imgtype=0&src=http%3A%2F%2Fscimg.jb51.net%2Fallimg%2F161207%2F102-16120H243090-L.jpg")
                .submit(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL)
                .get();
        Log.e(TAG, "onNext");
        emitter.onNext(bitmap);
        emitter.onComplete();
    });
// 当额外图片准备好后的后续操作 此功能可用于对分享的图片添加水印或者二维码等操作
shareManager.withExtralOperator(dowloadExtraObservable ,new BiFunction<Bitmap, Bitmap, Bitmap>() {
                            @Override
                            public Bitmap apply(Bitmap posterBitmap, Bitmap bitmap2) throws Exception {
                                Bitmap finalBitmap = Bitmap.createBitmap(posterBitmap.getWidth(), posterBitmap.getHeight(), Bitmap.Config.ARGB_8888);
                                Canvas canvas = new Canvas(finalBitmap);
                                canvas.drawBitmap(posterBitmap, 0, 0, null);
                                canvas.drawBitmap(bitmap2, 0, 0, null);
                                Log.e(TAG, "setBitmapOpreatorFuncation");
                                return finalBitmap; //返回最终的数据
                            }
                        })
//更新分享平台数据
当分享过程中某一平台的数据有变化时可以更新单个平台的数据。
shareManager.updataPlamForm(new SharePlamFormData.SinaMessageBuilder()
                        .builderImageWithTextMessage("标题1","内容1","http://www.baidu.com","http://f8.topitme.com/8/25/80/1125177570eea80258o.jpg")
                        .build());
