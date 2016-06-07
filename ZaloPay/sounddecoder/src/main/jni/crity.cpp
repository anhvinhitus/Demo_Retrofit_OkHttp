#include <stdlib.h>
#include <stdio.h>
#include <string.h>
#include <time.h>
#include <cmath>
#include <jni.h>
#include <stdbool.h>
#include <android/log.h>
#include <cstdio>
#include <iostream>
#include <cstring>
#include <vector>
#include "crity.h"

using namespace std;

#define LOGI(...) ((void)__android_log_print(ANDROID_LOG_INFO, "native-activity", __VA_ARGS__))

const char* convertJstringToChar(JNIEnv* env, const jstring jstr) {
	return (env->GetStringUTFChars(jstr, 0));
}

jstring convertCharToJstring(JNIEnv* env, const std::string& strBuff) {
	jstring result = env->NewStringUTF(strBuff.c_str());
	return result;
}

jstring convertCharToJstring(JNIEnv* env, const char* strBuff) {
	jstring result = env->NewStringUTF(strBuff);
	return result;
}

char* as_unsigned_char_array(JNIEnv* env, jbyteArray array) {
    int len = env->GetArrayLength (array);
    char* buf = new char[len];
    env->GetByteArrayRegion (array, 0, len, reinterpret_cast<jbyte*>(buf));
    return buf;
}

JNIEXPORT jstring JNICALL Java_vn_com_vng_zalopay_crity_CrityWrapper_createSecureKeyPart(
		JNIEnv* env, jobject jPartName, jobject jContext) {
	char emptyCHARS[17];
	strcpy(emptyCHARS, "SD");
	strcat(emptyCHARS, "R");
	strcat(emptyCHARS, "DSzNS");
	strcat(emptyCHARS, "J");
	strcat(emptyCHARS, "g=");
	std::string empty(emptyCHARS);//H4CK3R&
	return env->NewStringUTF(empty.c_str());
}


