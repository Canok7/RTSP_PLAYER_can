package com.example.wangxiancan.rtsp_player_can;

import android.os.Environment;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Arrays;

public class DataProvide_file implements  DataProvide{

    public int control(int cmd,String data){return 0;}
    @Override
    public int init() {
        return 0;
    }

    @Override
    public int deinit() {
        release();
        return 0;
    }

    @Override
    public int getNal(byte[] dataBuf, int len) {

        return getdata(dataBuf,len);
    }

    // 提供 H264数据
    //private String h264fileName = "720pq.h264";
    private String h264fileName = "display_send_data";
    private String h264Path = Environment.getExternalStorageDirectory() + "/"
            + h264fileName;
    private File h264File = null;
    private FileInputStream fs = null;
    private BufferedInputStream BufIs = null;
    byte[] framebuffer = new byte[1024*600];// 帧数据缓冲区
    static final int CHECKLEN = 2;
    byte[] checkbak = new byte[2 * CHECKLEN];
    int frame = 0;//
    int offset = 0;
    boolean bFull = false;//是否溢出

    public int getdata(byte[] dataBuf, int len)
    {

        // byte[] buffer = new byte[100000];//读数据缓冲区
        // int h264Read = 0;
        int remainlen = framebuffer.length - offset;
        if (null == h264File) {
            initFile();
        }

        if (bFull) {
            Log.i("TRACK", "need new data" + offset + "re" + remainlen);
            int ret = readData(BufIs, framebuffer, 0, offset);
            if (ret < 0) {
                return ret;
            }
        }

        // 从帧缓冲中找 h264 NALU 开始标记
        int offsetnew = 0;
        if (bFull) {// 已经溢出，数据不连续, 跨断
            System.arraycopy(framebuffer, 0, checkbak, CHECKLEN, CHECKLEN);
            Log.i("TRACK", bytesToHexString(checkbak));
            Log.i("TRACK", "checkbaklen:" + checkbak.length);
            offsetnew = findHead(checkbak, 0, checkbak.length);
            if (offsetnew == 0) {
                offsetnew = findHead(framebuffer, 0, offset);
            }
            if (offsetnew > 0) {
                // //找到一完整帧，拷贝出去
                System.arraycopy(framebuffer, offset, dataBuf, 0, remainlen);
                System.arraycopy(framebuffer, 0, dataBuf, remainlen, offsetnew);

                // 用新数据填充 上一剩余空间
                int ret =readData(BufIs, framebuffer, offset, remainlen);
                if(ret <=0){
                    byte temp=0;
                    Arrays.fill(framebuffer,offset,offset+remainlen,temp);
                }
                bFull = false;

                int datalen = remainlen + offsetnew;
                Log.i("TRACK", "NEW find a nuall offset:" + offsetnew + "len"
                        + datalen+"remainlen"+remainlen);
                offset = offsetnew;
                return datalen;
            } else {
                // 出错，缓冲区不够长
                Log.i("TRACK", "new data but nofind" + offset);
                return -1;
            }

        } else {
            // +3, 跳过老的offset点
            offsetnew = findHead(framebuffer, offset + 3, remainlen);
            if (offsetnew > 0) {
                // //找到一完整帧，拷贝出去
                System.arraycopy(framebuffer, offset, dataBuf, 0, offsetnew
                        - offset);
                int datalen = offsetnew - offset;
                offset = offsetnew;
                Log.i("TRACK", "find a nuall offset:" + offsetnew + "len"
                        + datalen);
                return datalen;
            } else if (offsetnew == 0) {
                // 没有找到，已经溢出，需要拿新的数据
                bFull = true;
                Log.i("TRACK", "not find null");

                // 避免出现跨端区的NALL漏检
                System.arraycopy(framebuffer, framebuffer.length - CHECKLEN,
                        checkbak, 0, CHECKLEN);
            }
        }



        return 0;

    }



    public void initFile() {

        Log.i("TRACK", " " + h264Path);
        Log.i("TRACK", "init file");

        h264File = new File(h264Path);
        try {
            fs = new FileInputStream(h264File);
            BufIs = new BufferedInputStream(fs);
        } catch (FileNotFoundException e) {
            // TODO: handle exception
            Log.i("TRACK", "initerro" + e.getMessage());
            e.printStackTrace();
        }

        int counttemp = readData(BufIs, framebuffer, 0, framebuffer.length);
        if (counttemp > 0) {
            // Log.i("TRACK",bytesToHexString(framebuffer));
            int offsettemp = findHead(framebuffer, 0, counttemp);
            Log.i("TRACK", "init headoffset:" + offsettemp);
            // 必须保证找到一帧 否则可能就是缓冲区不够，只能扔数据
            offset = offsettemp;
        }

    }

    public void release() {
        h264File = null;
        fs = null;
        BufIs = null;
    }

    private int readData(BufferedInputStream bufin, byte[] buf, int offset,
                         int len) {
        try {
            int length = bufin.available();
            Log.i("TRACK", "available"+length);
            if (length > 0) {
                int counttemp = bufin.read(buf, offset, len);
                if (counttemp == -1) {
                    Log.i("TRACK", "red EOF");
                    return -1;
                }
                Log.i("TRACK", "read dat " + counttemp);
                return counttemp;
            } else {
                Log.i("TRACK", "EOF");
                return -1;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * Find H264 frame head
     *
     * @param buffer
     * @param len
     * @return the offset of frame head, return 0 if can not find one
     */
    static int findHead(byte[] buffer, int offset, int len) {
        int i;
        if (len < 3) {
            Log.i("!", "too short!");
            return 0;
        }
        for (i = offset; i < len - 3; i++) {
            if (checkHead(buffer, i)){
                return i;
            }
        }

        return 0;
    }

    /**
     * Check if is H264 frame head
     *
     * @param buffer
     * @param offset
     * @return whether the src buffer is frame head
     */
    static boolean checkHead(byte[] buffer, int offset) {
        /*
         * // 00 00 00 01 if (buffer[offset] == 0 && buffer[offset + 1] == 0 &&
         * buffer[offset + 2] == 0 && buffer[3] == 1) return true;
         */
        // 00 00 01
        if (buffer[offset] == 0 && buffer[offset + 1] == 0 && buffer[offset + 2] == 1)
        //  if (buffer[offset] == 0 && buffer[offset + 1] == 1 && buffer[offset + 2] == 9)
            return true;
        return false;
    }

    /* *
     * Convert byte[] to hex
     * string.这里我们可以将byte转换成int，然后利用Integer.toHexString(int)来转换成16进制字符串。
     *
     * @param src byte[] data
     *
     * @return hex string
     */
    public static String bytesToHexString(byte[] src) {
        StringBuilder stringBuilder = new StringBuilder("");
        if (src == null || src.length <= 0) {
            return null;
        }
        for (int i = 0; i < src.length; i++) {
            int v = src[i] & 0xFF;
            String hv = Integer.toHexString(v);
            if (hv.length() < 2) {
                stringBuilder.append(0);
            }
            stringBuilder.append(hv);
        }
        return stringBuilder.toString();
    }
}
