# Share
不用第三方实现微信QQ分享
## 说明
>其实网上已经有非常多不用三方的demo了，不过都没有考虑权限的问题，权限问题可参考[Android 6.0 动态权限申请](https://blog.csdn.net/xietansheng/article/details/54315674),[安卓7.0文件存储权限变更](https://blog.csdn.net/jianghe_130/article/details/78019482)，我就自己写一个demo给大家参考，可能会有点乱，本人小白轻喷

## 看看效果图↓ 

![](https://upload-images.jianshu.io/upload_images/926685-6bceb1bd2e5c17c7.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

![](https://upload-images.jianshu.io/upload_images/926685-8b6d42e10005814a.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

# 一共三步
### 1.配置安卓7.0权限
首先,在AndoridManifest文件下的application下配置
```
<application>
...
  <provider
              android:name="android.support.v4.content.FileProvider"
              android:authorities="com.xxx.fileprovider"
              android:exported="false"
              android:grantUriPermissions="true">
              <meta-data
                 android:name="android.support.FILE_PROVIDER_PATHS"
                  android:resource="@xml/file_paths" />
          </provider>
...
</application>
```

android:authorities一般是包名+fileprovider,然后在res下创建xml文件，添加filepaths.xml文件
![](https://upload-images.jianshu.io/upload_images/926685-9f8d7843373e2f11.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)
###### filepaths文件
```
<path>
    <external-path
        name="getExternalStorageDirectory_path"
        path="./storage/emulated/0/" />
    <root-path
        name="root_path"
        path="." />
</path>
```

这里我只配置了俩个，第一个表示在App内置存储区域下的缓存文件，它和 getExternalFilesDir()返回的路径一样，第二个为根路径，更多的配置请点击文章开头的超链接。
###### 另外还配置权限
```
    <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE"/>
    <uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE"/>
```

###### 还得申请权限（安卓5.0动态权限和安卓7.0访问文件权限），真麻烦
```
private String[] permission = {Manifest.permission.READ_EXTERNAL_STORAGE};
    //检查版本，大于23就申请权限
    private void checkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // 检查该权限是否已经获取
            int i = ContextCompat.checkSelfPermission(this, permission[0]);
            // 权限是否已经 授权 GRANTED---授权  DINIED---拒绝
            if (i != PackageManager.PERMISSION_GRANTED) {
                //无权限，准备申请权限
                ActivityCompat.requestPermissions(this, permission, 321);
             }
    }

    //判断是否大于安卓7.0
    private Uri isSeven(File file, Intent intent) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            //大于7.0，应使用该路径
            uri = FileProvider.getUriForFile(ShareDemo.this, "com.ljh.fileprovider", file);
            // 给目标应用一个临时授权
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        } else {
            //小于7.0
            uri = Uri.fromFile(file);
        }
        return uri;
    }
```
### 2.准备图片
终于可以开始分享了，你可以用各种图片，我这里就拿安卓的icon了
```
private File file,file1,dir;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.share_layout);
        ImageView iv = (ImageView) findViewById(R.id.iv);
        //资源文件转换为bitmap
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher);
        //创建文件，指定文件夹名为"iv"
        dir = new File(Environment.getExternalStorageDirectory(), "iv");
        if (!dir.exists()) {
            dir.mkdir();
        }
        iv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //分享至微信朋友
//                share(file1);
                //分享至微信朋友圈
                shareToCommunity(file1);
                //分享至QQ好友
//                shareToQQFriend();
            }
        });
    }

        //拼接路径并将图片命名为"System.currentTimeMillis().jpg"
        file = new File(dir, System.currentTimeMillis() + ".jpg");
        //把bitmap转file
        file1 = bitmapToFile(bmp, file);

    //把Bitmap转换成File
    public File bitmapToFile(Bitmap bitmap, File file) {
        try {
            FileOutputStream fos = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return file;
    }
```
### 3.终于到分享了 ٩(๑>◡<๑)۶
```
    //分享图片 到微信朋友
    private void share(File file) {
        Intent intent = new Intent();

        //判断是否大于安卓7.0
        Uri uri = isSeven(file, intent);

        ComponentName componentName = new ComponentName("com.tencent.mm", "com.tencent.mm.ui.tools.ShareImgUI");
        intent.setComponent(componentName);
        intent.setAction(Intent.ACTION_SEND);
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_STREAM, uri);

        startActivity(intent);
    }

    //分享图片 至朋友圈
    private void shareToCommunity(File file) {
        Intent intent = new Intent();

        //判断是否大于安卓7.0
        Uri uri = isSeven(file, intent);

        ComponentName componentName = new ComponentName("com.tencent.mm", "com.tencent.mm.ui.tools.ShareToTimeLineUI");
        intent.setComponent(componentName);
        intent.setAction(Intent.ACTION_SEND);
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_STREAM, uri);

        startActivity(intent);
    }

    //分享文本 到QQ好友（微信，朋友圈同理,这里分享文本不涉及访问文件就不用判断安卓是否大于7.0了）
    private void shareToQQFriend() {
        Intent intent = new Intent();
        ComponentName componentName = new ComponentName("com.tencent.mobileqq", "com.tencent.mobileqq.activity.JumpActivity");
        intent.setComponent(componentName);
        intent.setAction(Intent.ACTION_SEND);
        intent.setType("text/*");
        intent.putExtra(Intent.EXTRA_TEXT, "这是分享内容");
        startActivity(intent);
    }
```
这里最好判断一下用户是否安装了微信,QQ
```
    //判断微信是否安装（判断QQ改包名就行啦"com.tencent.mobileqq"）
    public static boolean isWeixinAvilible(Context context) {
        // 获取packagemanager
        final PackageManager packageManager = context.getPackageManager();
        // 获取所有已安装程序的包信息
        List<PackageInfo> pinfo = packageManager.getInstalledPackages(0);
        if (pinfo != null) {
            for (int i = 0; i < pinfo.size(); i++) {
                String pn = pinfo.get(i).packageName;
                if (pn.equals("com.tencent.mm")) {
                    return true;
                }
            }
        }
        return false;
    }
```

## 大功告成

参考文章：
[Android 6.0 动态权限申请](https://blog.csdn.net/xietansheng/article/details/54315674)
[安卓7.0文件存储权限变更](https://blog.csdn.net/jianghe_130/article/details/78019482)
