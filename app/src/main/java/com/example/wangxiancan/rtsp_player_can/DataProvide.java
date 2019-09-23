package com.example.wangxiancan.rtsp_player_can;

public interface DataProvide {

    public int init();
    public int deinit();

    /**
     * @param dataBuf //接受数据缓冲区
     * @param len //缓冲区的容量 字节
     * @return //实际获得的 Nal 数据大小 字节
     */
    public int getNal(byte[] dataBuf, int len);

    public int control(int cmd,String data);
}

