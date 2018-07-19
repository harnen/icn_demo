#LOCAL_PATH := $(call my-dir) 
#LOCAL_PATH_SAVED := $(LOCAL_PATH)

NDK_TOOLCHAIN_VERSION := clang
LOCAL_CFLAGS += -Wno-error=format-security
LOCAL_PATH := $(call my-dir)
MY_PATH := $(LOCAL_PATH)
include $(call all-subdir-makefiles)

include $(CLEAR_VARS)

LOCAL_PATH := $(MY_PATH)

# nfd itself
include $(CLEAR_VARS)
LOCAL_MODULE := netguard
LOCAL_C_INCLUDES := netguard
LOCAL_SRC_FILES := \
    netguard/netguard.c \
    netguard/dhcp.c \
    netguard/dns.c \
    netguard/icmp.c \
    netguard/ip.c \
    netguard/pcap.c \
    netguard/session.c \
    netguard/tcp.c \
    netguard/udp.c \
    netguard/util.c 
LOCAL_LDLIBS := -L$(SYSROOT)/usr/lib -llog 
include $(BUILD_SHARED_LIBRARY)
