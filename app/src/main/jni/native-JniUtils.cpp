#include "com_example_wangxiancan_rtsp_player_can_JniUtils.h"
#include "jnilog.h"
#include "geth264Frame.cpp"

#include "live555.h"
static int bInit = 0;
static uint8_t *gbuffer = NULL;

//if  you want  to get h264 data  from a test file, to define TEST_FROM_FILE 1
 // the test file path : fp_inH264 = fopen("/storage/emulated/0/display_send_data","r");
  // in geth264Frame.cpp
#define  TEST_FROM_FILE 0


 char* jstringToChar(JNIEnv* env, jstring jstr) {
    char* rtn = NULL;
    jclass clsstring = env->FindClass("java/lang/String");
    jstring strencode = env->NewStringUTF("utf-8");
    jmethodID mid = env->GetMethodID(clsstring, "getBytes", "(Ljava/lang/String;)[B");
    jbyteArray barr = (jbyteArray) env->CallObjectMethod(jstr, mid, strencode);
    jsize alen = env->GetArrayLength(barr);
    jbyte* ba = env->GetByteArrayElements(barr, JNI_FALSE);
    if (alen > 0) {
        rtn = (char*) malloc(alen + 1);
        memcpy(rtn, ba, alen);
        rtn[alen] = 0;
    }
    env->ReleaseByteArrayElements(barr, ba, 0);
    return rtn;
}

JNIEXPORT jint JNICALL Java_com_example_wangxiancan_rtsp_1player_1can_JniUtils_OpenUrl
        (JNIEnv * env, jobject obj, jstring url) {
    if (bInit) {
        //this will block until the live555 thread over
        live555_stop();
    }

    char *pUrl = NULL;
    pUrl = jstringToChar(env, url);
    if (pUrl) {
        live555_start(pUrl);
        free(pUrl);
    }

return 0;
}
JNIEXPORT jint JNICALL Java_com_example_wangxiancan_rtsp_1player_1can_JniUtils_GetH264Frame
        (JNIEnv * env, jobject obj, jbyteArray buf, jint buflen)
{
    int datalen =0;
#if TEST_FROM_FILE
    if(!bInit)
    {
        bInit = 1;
        init();
       gbuffer = (uint8_t*)malloc(CACH_LEN);
    }


    do{
        datalen = getOneNal(gbuffer,CACH_LEN);
    }while(checkNal(gbuffer[4]) == NALU_TYPE_AUD); //过滤掉结束符，注意这里不是指 audio ,只是h264的一种nal类型
    //}while(0);

    //输出太快，解码器器会丢帧，导致花屏
    usleep(1000*1000/30); //帧率30

    #if 0// for .c
        (*env)->SetByteArrayRegion(env, buf, 0, datalen,(jbyte *)gbuffer);
    #else //for  .cpp
        env->SetByteArrayRegion( buf, 0, datalen,(jbyte *)gbuffer);
    #endif

    return datalen;
#else
    if(!bInit)
    {
        TRACK("live555_start");
        bInit = 1;
       if( live555_start("rtsp://192.168.43.1:10086/stream") == -1)
       {
           TRACK("live555_start erro \n");
           bInit =0;
       }

    }

   unsigned char *pdata;
   int bufIndex = 0;

   bufIndex = live555_requestFrameBuffer(&pdata, &datalen);
   do {
        if (buf != NULL) {// rtp包中没有nal 标记,需要自己添加
            unsigned char nal[4]={0x00,0x00,0x00,0x01};
            env->SetByteArrayRegion(buf, 0, 4, (jbyte *) nal);
            env->SetByteArrayRegion(buf, 4, datalen, (jbyte *) pdata);
       }
   }while(0);
   live555_releaseBuffer(bufIndex);

   return datalen+4;

#endif


}