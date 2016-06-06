LOCAL_PATH := $(call my-dir)

include $(call all-subdir-makefiles)
include $(CLEAR_VARS)
LOCAL_MODULE    := crity
LOCAL_SRC_FILES := crity.cpp
LOCAL_LDLIBS := -llog
include $(BUILD_SHARED_LIBRARY)