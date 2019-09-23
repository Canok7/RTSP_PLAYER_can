ROOT_DIR := $(call my-dir)
include $(CLEAR_VARS)
LOCAL_PATH := $(ROOT_DIR)

#头文件目录
LOCAL_C_INCLUDES := $(LOCAL_PATH)/../../jniLibs/arm64-v8a/include/BasicUsageEnvironment/include/
LOCAL_C_INCLUDES += $(LOCAL_PATH)/../../jniLibs/arm64-v8a/include/groupsock/include/
LOCAL_C_INCLUDES += $(LOCAL_PATH)/../../jniLibs/arm64-v8a/include/liveMedia/
LOCAL_C_INCLUDES += $(LOCAL_PATH)/../../jniLibs/arm64-v8a/include/liveMedia/include/
LOCAL_C_INCLUDES += $(LOCAL_PATH)/../../jniLibs/arm64-v8a/include/UsageEnvironment/include/


#需要连接的库
#LOCAL_SHARED_LIBRARIES += liblog
#LOCAL_SHARED_LIBRARIES += libutils
#LOCAL_SHARED_LIBRARIES += libcutils
#LOCAL_SHARED_LIBRARIES += libc
#LOCAL_SHARED_LIBRARIES += libbinder
#LOCAL_SHARED_LIBRARIES += libgui

#编译的源文件
LOCAL_SRC_FILES := \
 live555.cpp \
 queue.cpp

 
#LOCAL_MODULE_TAGS := optional
LOCAL_CFLAGS += -DPLATFORM_ANDROID
#编译输出的模块名称
LOCAL_MODULE:= testRtspClient
LOCAL_LDLIBS := -L$(LOCAL_PATH)/../jniLibs/arm64-v8a/ \
 -lliveMedia  -lBasicUsageEnvironment -lUsageEnvironment -lgroupsock -lc++abi \
 -llog

#编译
include $(BUILD_EXECUTABLE)
#include $(BUILD_SHARED_LIBRARY)

