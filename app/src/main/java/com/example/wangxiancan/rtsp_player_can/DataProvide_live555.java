package com.example.wangxiancan.rtsp_player_can;

public class DataProvide_live555 implements  DataProvide{

    @Override
    public int init() {
        return 0;
    }

    @Override
    public int deinit() {
        return 0;
    }

    @Override
    public int control(int cmd,String data){
        switch(cmd){
            case 0: { //just add temp, open new url
                mJni.OpenUrl(data);
            }
            break;
            default: break;
        }
        return 0;
    }
    @Override
    public int getNal(byte[] dataBuf, int len) {
        return mJni.GetH264Frame(dataBuf, len);
    }

    private JniUtils mJni = null;
    DataProvide_live555(){
        mJni = new JniUtils();
    }
}
