package com.example.wangxiancan.rtsp_player_can;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

import android.util.Log;
import android.view.Surface;
import android.media.MediaCodec;
import android.media.MediaFormat;
import android.os.Environment;
public class VideoDecoder  {
    private Surface mSurface;
    private Worker mWorker;
    private DataProvide dataProvide;

    //一些基本参数 final 类似与 const，不可修改
    //private final int TIMEOUT_US = 1000*1;//设置太小，会导致解码器来不及解码，导致解码丢帧花屏
    //这里不希望丢帧，设置为无限超时。
    private final int DECODE_TIMEOUT_US = -1; //

    //要把输出Buffer消耗掉，但其实没有用它做事情，不加延时，设置为-1可能出现死锁
    private final int OUT_TIMEOUT_US = 1000;
    //这个宽高，好像没什么卵用，实际播放的流中如果有 sps pps信息，会用流中的sps pps信息。
    private final int mWidth=1920;
    private final int mHeight=1080;
    //if you want to save the input data to file, set bSave2file true
    private final boolean bSave2file =true;

    public VideoDecoder(Surface surface)
    {
        mSurface = surface;
    }

    public void start() {
        if (mWorker == null) {
            mWorker = new Worker();
            // if you want to test videoDecoder with h264 data getted from file ,use DataProvide_file
            //dataProvide = new DataProvide_file();
            dataProvide = new DataProvide_live555();
            mWorker.setRunning(true);
            mWorker.start(); //将会调用 run执行
        }
    }

    public void stop() {
        if (mWorker != null) {
            mWorker.setRunning(false);
            mWorker = null;
        }

    }

    public void openNewUrl(String url){
        if(dataProvide != null){
            dataProvide.control(0,url);
        }

    }

    //内部类 worker
    private class Worker extends Thread{
        public static final String DECODE = "Decode";
        public static final String TAG = "WORK";
        private boolean isRunning;
        private  MediaCodec decoder;
        private MediaCodec.BufferInfo mBufferInfo;
        private byte[] dataBuf ;

        public void setRunning(boolean bRunning){
            isRunning= bRunning;
        }

        public void run() {
            //worker循环
            if (!prepare()) {
                Log.i("PRE", "视频解码器初始化失败");
                isRunning = false;
            }
            Log.i("PRE","初始化成功");
            while (isRunning) {
                //Log.i("TRACK","getdat befor!");
                int len = dataProvide.getNal(dataBuf, dataBuf.length);
                Log.i("TRACK", "getdat after:"+len);
                if(len == -1){
                    //eof
                    decodeEnd();
                    wirte2fileover();
                    setRunning(false);
                    break;
                }
                else if(len >0){
                    decode(dataBuf,0,len);
                   wirte2file(dataBuf,0,len);
                }

                /* //按照 数据源输入的速度播放，来一帧播一帧，所以这延时不应该加
                if(false) {
                    //Log.i("TRACK","decod over");
                    try {
                        sleep(1000/60);
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }*/
            }
            release();

        }

        private void release() {
            if (decoder != null) {
                Log.i("TRACK","release decoder");
                decoder.stop();
                decoder.release();
                decoder=null;
            }
            if(dataProvide != null){
                Log.i("TRACK","release dataProvide");
                dataProvide.deinit();
                dataProvide = null;
            }
        }

        public boolean prepare() {
            //初始化工作
            mBufferInfo = new MediaCodec.BufferInfo();
            byte[] sps= new byte[1024];
            byte[] pps= new byte[1024];
            dataBuf = new byte[1024*600];//要足够大

            //h264 格式
            MediaFormat format = MediaFormat.createVideoFormat(MediaFormat.MIMETYPE_VIDEO_AVC, mWidth, mHeight);

            //如果h264流文件中存在 sps pps 帧，（流起始就要求有sps pps）下面这些设置就没有用，会被覆盖
           // format.setInteger(MediaFormat.KEY_MAX_INPUT_SIZE, mHeight * mWidth);
           // format.setInteger(MediaFormat.KEY_MAX_HEIGHT, mHeight);
           // format.setInteger(MediaFormat.KEY_MAX_WIDTH, mWidth);
           // format.setByteBuffer("csd-0", ByteBuffer.wrap(sps));// wrap 创建新的缓冲区，之前的 1024 将不再使用，被自动回收
           // format.setByteBuffer("csd-1", ByteBuffer.wrap(pps));//


            try {
                //创建解码器
                decoder = MediaCodec.createDecoderByType(MediaFormat.MIMETYPE_VIDEO_AVC);
            } catch (IOException e) {
                e.printStackTrace();
            }
            //配置解码器,
            decoder.configure(format, mSurface, null, 0);
            decoder.start();

            Log.i("TRACK","prep!");
            return true;
        }

        private void decodeEnd(){
            int inputBufferIndex = decoder.dequeueInputBuffer(DECODE_TIMEOUT_US);
            decoder.queueInputBuffer(inputBufferIndex, 0, 0, 0,
                    MediaCodec.BUFFER_FLAG_END_OF_STREAM);

            int outIndex = decoder.dequeueOutputBuffer(mBufferInfo,
                    OUT_TIMEOUT_US);
            // Log.i(TAG, "video decoding .....");
            while (outIndex >= 0) {
                // ByteBuffer buffer =
                decoder.getOutputBuffer(outIndex);
                decoder.releaseOutputBuffer(outIndex, true);
                outIndex = decoder.dequeueOutputBuffer(mBufferInfo,
                        OUT_TIMEOUT_US);// 再次获取数据，如果没有数据输出则outIndex=-1
                // 循环结束
            }

        }
        private void decode(byte[] buf, int offset, int length){
            //在这里进行解码并输出数据
           // Log.i("TRACK","decoding");
            //从解码器请求获取一个输入缓冲区
            int inputBufferIndex = decoder.dequeueInputBuffer(DECODE_TIMEOUT_US);
            if(inputBufferIndex >= 0){
                Log.i(DECODE, "video decoding");
                ByteBuffer buffer = decoder.getInputBuffer(inputBufferIndex);

                buffer.clear();
                buffer.limit(length);
                buffer.put(buf, offset, length);
                //入队列，放入数据
                decoder.queueInputBuffer(inputBufferIndex, 0, length,0,
                        MediaCodec.BUFFER_FLAG_SYNC_FRAME);
            }else {
                Log.i(DECODE, "video getInput timeout ----------to drop");
            }

            //以下需要将解码器解码的输出数据获取一遍，消耗掉，。。。。。。。个人感觉既然已经绑定了surface,不应该
            //还要这么一个操作啊，没有深入研究
            int outIndex = decoder.dequeueOutputBuffer(mBufferInfo,
                    OUT_TIMEOUT_US);
            while (outIndex >= 0) {
                // ByteBuffer buffer =
                decoder.getOutputBuffer(outIndex);
                decoder.releaseOutputBuffer(outIndex, true);
                outIndex = decoder.dequeueOutputBuffer(mBufferInfo,
                        OUT_TIMEOUT_US);// 再次获取数据，如果没有数据输出则outIndex=-1
                // 循环结束
            }
        }
    }

    private String dsetfilePath = Environment.getExternalStorageDirectory()+"/"+"dest.h264";
    private File destfile = null;
    private FileOutputStream destfs =null;
    private BufferedOutputStream BufOs=null;
    private void wirte2file(byte[] buf, int offset, int length){
        if(bSave2file) {
            if(BufOs ==null)
            {
                destfile = new File(dsetfilePath);
                try {
                    destfs = new FileOutputStream(destfile);
                    BufOs= new BufferedOutputStream(destfs);
                } catch (FileNotFoundException e) {
                    // TODO: handle exception
                    Log.i("TRACK","initerro"+e.getMessage());
                    e.printStackTrace();
                }
            }

            try {
                BufOs.write(buf, 0, length);
                BufOs.flush();

            } catch (Exception e) {
                // TODO: handle exception
            }

        }
    }

    private void wirte2fileover(){
        if(bSave2file) {
            try {
                BufOs.flush();
                BufOs.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }
}
