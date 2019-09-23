package com.example.wangxiancan.rtsp_player_can;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    private SurfaceView mSurfaceView;
    private VideoDecoder mVideoDecoder;


    private class cMyListenner implements View.OnTouchListener{
        private Context icontext ;

        cMyListenner(Context context) {
            icontext = context;
        }
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            Log.i("onTuch ", "ontuch");
            //这里弹出对话框
            final EditText et = new EditText(icontext);
            new AlertDialog.Builder(icontext).setTitle("请输入URL,比如:rtsp://192.168.43.1:8554")
                    .setIcon(android.R.drawable.sym_def_app_icon)
                    .setView(et)
                    .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                           String url = et.getText().toString();
                            mVideoDecoder.openNewUrl(url);
                            Log.i("onTuch ", url);
                            //按下确定键后的事件
                            Toast.makeText(getApplicationContext(),url,Toast.LENGTH_LONG).show();
                        }
                    }).setNegativeButton("取消",null).show();

            return false;
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        verifyStoragePermissions(this);

        mSurfaceView = (SurfaceView)findViewById(R.id.surfaceView);
        SurfaceHolder holder = mSurfaceView.getHolder();
        mVideoDecoder = null;

        //实现SurfaceView的状态监听
        holder.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                // TODO Auto-generated method stub
                Log.i("SURFACE","destroyed");
                if(mVideoDecoder != null){
                    mVideoDecoder.stop();
                    mVideoDecoder = null;
                }
            }

            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                // TODO Auto-generated method stub
                Log.i("SURFACE","create");
                if(mVideoDecoder== null){
                    mVideoDecoder = new VideoDecoder(holder.getSurface());
                }
                mVideoDecoder.start();
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width,
                                       int height) {
                // TODO Auto-generated method stub

            }
        });
        mSurfaceView.setOnTouchListener(new cMyListenner(this));

    }



    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.INTERNET};
    public static boolean verifyStoragePermissions(Activity activity) {

        /*******below android 6.0*******/
        if(Build.VERSION.SDK_INT < 23)
        {
            return true;
        }
        // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(activity,
                Manifest.permission.READ_EXTERNAL_STORAGE);
        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(activity,PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE);
            return false;
        }
        else
        {
            return true;
        }
    }
}
