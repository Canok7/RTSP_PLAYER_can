package com.example.wangxiancan.rtsp_player_can;

public class JniUtils {
    static {
        System.loadLibrary("dataPriveder");
    }

    public native int GetH264Frame(byte[] buf,int buflen);

    public native int OpenUrl(String url);
}
