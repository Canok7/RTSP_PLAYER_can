#ifndef  __JNI_LOG_HEAD_H__
#define __JNI_LOG_HEAD_H__

#include<android/log.h>
#define MY_LOG_TAG    "from-jni" // 这个是自定义的LOG的标识
#define LOGD(...)  __android_log_print(ANDROID_LOG_DEBUG,MY_LOG_TAG,__VA_ARGS__) // 定义LOGD类型
#define LOGI(...)  __android_log_print(ANDROID_LOG_INFO,MY_LOG_TAG,__VA_ARGS__) // 定义LOGI类型
#define LOGW(...)  __android_log_print(ANDROID_LOG_WARN,MY_LOG_TAG,__VA_ARGS__) // 定义LOGW类型
#define LOGE(...)  __android_log_print(ANDROID_LOG_ERROR,MY_LOG_TAG,__VA_ARGS__) // 定义LOGE类型
#define LOGF(...)  __android_log_print(ANDROID_LOG_FATAL,MY_LOG_TAG,__VA_ARGS__) // 定义LOGF类型


#define TRACK(...)  __android_log_print(ANDROID_LOG_INFO,MY_LOG_TAG,"[%d %s]",__LINE__,__FUNCTION__);__android_log_print(ANDROID_LOG_INFO,MY_LOG_TAG,__VA_ARGS__)
#endif