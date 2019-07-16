LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

OPENCV_INSTALL_MODULES:=on
OPENCV_CAMERA_MODULES:=on
OPENCV_LIB_TYPE :=STATIC

include $(LOCAL_PATH)/OpenCV.mk

LOCAL_MODULE := CvExample
LOCAL_SRC_FILES := insights_water_waterinsightsv005_CvUtil.cpp VisionFunctions.cpp
LOCAL_C_INCLUDES := $(LOCAL_PATH)/include
LOCAL_LDLIBS += -lm -llog

include $(BUILD_SHARED_LIBRARY)