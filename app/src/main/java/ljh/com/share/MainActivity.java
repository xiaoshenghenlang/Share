package ljh.com.share;

import android.Manifest;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;


/**
 * Description:
 * Date on 2018/4/9
 * Author: LJH
 */

public class MainActivity extends Activity {

    private Uri uri;
    private String[] permission = {Manifest.permission.READ_EXTERNAL_STORAGE};
    private File file1;
    private File file;
    private File dir;
    private ImageView iv;

    //在这个方法中可以直接获取控件的宽高
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);

        //资源文件转换为bitmap
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher);

        //创建文件，指定文件夹名为"iv"
        dir = new File(Environment.getExternalStorageDirectory(), "iv");
        if (!dir.exists()) {
            dir.mkdir();
        }

        //拼接路径并将图片命名为"System.currentTimeMillis().jpg"
        file = new File(dir, System.currentTimeMillis() + ".jpg");
        //把bitmap转file
        file1 = bitmapToFile(bitmap, file);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        iv = (ImageView) findViewById(R.id.iv);

        //检查版本，大于23就申请权限
        checkPermission();


        iv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //把图片存入图库
//                insertMap(file1);
                //分享至微信朋友
//                share(file1);

                //分享至微信朋友圈
                shareToCommunity(file1);
                //分享至QQ好友
//                shareToQQFriend();
            }
        });
    }

    //判断是否大于安卓7.0
    private Uri isSeven(File file, Intent intent) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            //大于7.0，应使用该路径
            uri = FileProvider.getUriForFile(MainActivity.this, "com.xxx.fileprovider", file);
            // 给目标应用一个临时授权
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        } else {
            //小于7.0
            uri = Uri.fromFile(file);
        }
        return uri;
    }

    //检查版本，大于23就申请权限
    private void checkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // 检查该权限是否已经获取
            int i = ContextCompat.checkSelfPermission(this, permission[0]);
            // 权限是否已经 授权 GRANTED---授权  DINIED---拒绝
            if (i != PackageManager.PERMISSION_GRANTED) {
                //无权限，准备申请权限
                ActivityCompat.requestPermissions(this, permission, 321);
            } else {
                //有权限
            }
        } else {
            //版本小于23
        }
    }

    //权限回调
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 321) {
            if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "权限获取失败，可能导致您无法分享,如需打开权限请到设置界面开启", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "权限获取成功", Toast.LENGTH_SHORT).show();
            }
        }
    }

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

    /**
     * 把View转换成Bitmap
     * 这个方法不要在onCreate的初始化中调用，否则获取控件的宽高为0
     */
//    public Bitmap viewToBitmap(final View v) {
//        if (v == null) {
//            return null;
//        }
//        Bitmap bitmap;
//
//        int width = v.getWidth();
//        int height = v.getHeight();
//
//        bitmap = Bitmap.createBitmap(width, height
//                , Bitmap.Config.ARGB_8888 /*HDConstantSet.BITMAP_QUALITY*/);
//        Canvas c = new Canvas(bitmap);
//        c.translate(-v.getScrollX(), -v.getScrollY());
//        v.draw(c);
//        return bitmap;
//    }

    //把文件插入到系统图库
//    private void insertMap(File file) {
//        try {
//            MediaStore.Images.Media.insertImage(getContentResolver(),
//                    file.getAbsolutePath(), System.currentTimeMillis() + ".jpg", null);
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        }
//        Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
//
//        //判断是否大于安卓7.0
//        Uri uri = isSeven(file, intent);
//
//        intent.setData(uri);
//        sendBroadcast(intent);
//    }

    //分享图片至朋友圈
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

    //分享文本到QQ好友（微信，朋友圈同理）
    private void shareToQQFriend() {
        Intent intent = new Intent();
        ComponentName componentName = new ComponentName("com.tencent.mobileqq", "com.tencent.mobileqq.activity.JumpActivity");
        intent.setComponent(componentName);
        intent.setAction(Intent.ACTION_SEND);
        intent.setType("text/*");
        intent.putExtra(Intent.EXTRA_TEXT, "这是分享内容");
        startActivity(intent);
    }


    //分享图片到微信朋友
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

    //判断微信是否安装
//    public static boolean isWeixinAvilible(Context context) {
//        // 获取packagemanager
//        final PackageManager packageManager = context.getPackageManager();
//        // 获取所有已安装程序的包信息
//        List<PackageInfo> pinfo = packageManager.getInstalledPackages(0);
//        if (pinfo != null) {
//            for (int i = 0; i < pinfo.size(); i++) {
//                String pn = pinfo.get(i).packageName;
//                if (pn.equals("com.tencent.mm")) {
//                    return true;
//                }
//            }
//        }
//        return false;
//    }
}

