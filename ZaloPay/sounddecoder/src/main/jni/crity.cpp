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

JNIEXPORT jstring JNICALL Java_vn_com_vng_grd_crity_CrityWrapper_createSecureKeyPart(
		JNIEnv* env, jobject jPartName, jobject jContext) {
	//LOGI("\n *******************Begin sigTicketBookRequest************************ ");
	//check package name truoc da
	char emptyCHARS[17];
	strcpy(emptyCHARS, "SD");
	strcat(emptyCHARS, "R");
	strcat(emptyCHARS, "DSzNS");
	strcat(emptyCHARS, "J");
	strcat(emptyCHARS, "g=");
	std::string empty(emptyCHARS);//H4CK3R&
//	LOGI("NativeWrapper_createSecureKeyPart truoc khi checkPackageSign");
	jstring result;
	if (!checkPackageName(env, jContext)) {
//		LOGI("NativeWrapper_createSecureKeyPart thang nao vay?????????");
		result = convertCharToJstring(env, empty);
		return result;
	} else {
//		LOGI("NativeWrapper_createSecureKeyPart ngon roi tiep tuc di thoi");
		if (!checkPackageSign(env, jContext)) {
//			LOGI("NativeWrapper_createSecureKeyPart thang nao vay?????????");
			result = convertCharToJstring(env, empty);
			return result;
		} else {
//			LOGI("NativeWrapper_createSecureKeyPart ngon roi tiep tuc di thoi");
		}
//		char PRIVATE_KEY_CHARS[20];
//		strcpy(PRIVATE_KEY_CHARS, "");
//		strcat(PRIVATE_KEY_CHARS, "");
//		strcat(PRIVATE_KEY_CHARS, "");
//		strcat(PRIVATE_KEY_CHARS, "");
//		strcat(PRIVATE_KEY_CHARS, "");
//		strcat(PRIVATE_KEY_CHARS, "");
//		char PRIVATE_KEY_CHARS[25];
//		strcpy(PRIVATE_KEY_CHARS, "");
//		strcat(PRIVATE_KEY_CHARS, "");
//		strcat(PRIVATE_KEY_CHARS, "");
//		strcat(PRIVATE_KEY_CHARS, "");
//		strcat(PRIVATE_KEY_CHARS, "=");
//		strcat(PRIVATE_KEY_CHARS, "=");
		std::string PRIVATE_KEY(emptyCHARS);
		result = convertCharToJstring(env, PRIVATE_KEY);
		return result;
	}

}

//bool checkPackageName(JNIEnv* env, jstring data, jobject jobj_object) {
bool checkPackageName(JNIEnv* env, jobject jContextObject) {
	//	if (!checkIsInstanceOf(env, jContextObject, "android/content/Context")){return 0;}
	jclass jClassContext = env->FindClass( "android/content/Context");
//	LOGI("checkPackageName: check xong context");

	std::string methoStr = "getPackageName";
	jmethodID jmth_get_package = env->GetMethodID( jClassContext, methoStr.c_str(),
			"()Ljava/lang/String;");
	if (jmth_get_package == NULL || jmth_get_package == 0) {return 0;}
//	LOGI("checkPackageName: check duoc getPackageName method");

	jstring jstr_package = (jstring)env->CallObjectMethod( jContextObject,
			jmth_get_package);
	if (jstr_package == NULL) {	return 0;}
//	LOGI("checkPackageName: get duoc jstr_package");

	const char *request_package = convertJstringToChar(env, jstr_package);
//	std::string packageStr = "com.timxekhach.passenger";
//	const char* packagenameZBus = packageStr.c_str();

	char packagenameZBus[17];
	strcpy(packagenameZBus, "v");
	strcat(packagenameZBus, "n.c");
	strcat(packagenameZBus, "om.vn");
	strcat(packagenameZBus, "g.vmp");
	strcat(packagenameZBus, "ay");
//	LOGI("pack cua app nao = %s", request_package);
	int bResult = std::strcmp(request_package, packagenameZBus);
	if (bResult == 0) {
//		LOGI("Tao la zbus day cho tao vao voi");
		return true;
	} else {
		LOGI("Thang bo lao vao day lam j");
		return false;
	}
}

bool checkIsInstanceOf(JNIEnv* env, jobject jObject, const char* className){
	jclass classOfName = env->FindClass(className);
	if (className == NULL) {return 0;}
//	LOGI("checkIsInstanceOf: found class");

	jclass jClass = env->GetObjectClass( jObject);
	if (jClass == NULL) {return 0;}

//	LOGI("checkIsInstanceOf: found object class");

	if (!env->IsInstanceOf(jObject, classOfName)) {return 0;}
//	LOGI("checkIsInstanceOf: true");
	return 1;
}

bool checkPackageSign(JNIEnv* env, jobject jContextObject) {
//	if (!checkIsInstanceOf(env, jContextObject, "android/content/Context")){return 0;}
	jclass jClassContext = env->FindClass( "android/content/Context");
//	LOGI("checkPackageSign: check xong context");

	std::string getPackageNameMethodStr = "getPackageName";
	jmethodID getPackageNameMethodId = env->GetMethodID( jClassContext, getPackageNameMethodStr.c_str(),"()Ljava/lang/String;");
	if (getPackageNameMethodId == NULL || getPackageNameMethodId == 0) {return 0;}
//	LOGI("checkPackageSign: check xong getPackageName method");

	jstring jPackageString = (jstring)env->CallObjectMethod( jContextObject,getPackageNameMethodId);
	if (jPackageString == NULL) {return 0;}
//	LOGI("checkPackageSign: get duoc packageName");

	std::string getPackageManagerStr = "getPackageManager";
	jmethodID getPackageManagerMethodId = env->GetMethodID( jClassContext, getPackageManagerStr.c_str(),"()Landroid/content/pm/PackageManager;");
	if (getPackageManagerMethodId == NULL || getPackageManagerMethodId == 0) {return 0;}
//	LOGI("checkPackageSign: check xong getPackageManager method");

	jobject packageManager = env->CallObjectMethod( jContextObject,getPackageManagerMethodId);
	if (packageManager == NULL) {return 0;}
//	LOGI("checkPackageSign: get duoc packageManager");

//	if (!checkIsInstanceOf(env,packageManager,"android/content/pm/PackageManager")){return 0;}
	jclass classOfPackageManager = env->FindClass("android/content/pm/PackageManager");
//	LOGI("checkPackageSign: check xong packageManager");

//	std::string getSignatureStaticStr = "GET_SIGNATURES";
//	jfieldID getSignatureStaticId = env->GetStaticFieldID(classOfPackageManager, getSignatureStaticStr.c_str(), "I");
//	if (getSignatureStaticId == NULL || getSignatureStaticId == 0) {return 0;}
//	jint getSignature = env->GetStaticIntField(classOfPackageManager, getSignatureStaticId);
//	LOGI("checkPackageSign: get duoc GET_SIGNATURES");
	jint getSignature = 0x00000040;
	std::string getPackageInfoStr = "getPackageInfo";
	jmethodID getPackageInfoMethodId = env->GetMethodID( classOfPackageManager, getPackageInfoStr.c_str(),
					"(Ljava/lang/String;I)Landroid/content/pm/PackageInfo;");
	if (getPackageInfoMethodId == NULL || getPackageInfoMethodId == 0) {return 0;}
//	LOGI("checkPackageSign: check xong getPackageInfo method");

	jobject packageInfo = env->CallObjectMethod( packageManager,getPackageInfoMethodId,jPackageString,getSignature);
	if (packageInfo == NULL) {return 0;}
//	LOGI("checkPackageSign: get duoc packageInfo");

//	if (!checkIsInstanceOf(env,packageInfo,"android/content/pm/PackageInfo")){return 0;}
	jclass classOfPackageInfo = env->FindClass("android/content/pm/PackageInfo");
//	LOGI("checkPackageSign: check xong packageInfo");

	std::string signaturesStr = "signatures";
	jfieldID signaturesId = env->GetFieldID(classOfPackageInfo, signaturesStr.c_str(), "[Landroid/content/pm/Signature;");
	if (signaturesId == NULL || signaturesId == 0) {return 0;}
	jobject signaturesObj = env->GetObjectField(packageInfo, signaturesId);
	jobjectArray* signaturesArray = reinterpret_cast<jobjectArray*>(&signaturesObj);
//	LOGI("checkPackageSign: get duoc signaturesArray");

	jsize len = env->GetArrayLength(*signaturesArray);
	// Get the elements
//	LOGI("checkPackageSign: count = %d", len);
	for(jint i=0; i<len; i++)
	{
		jobject signature = env->GetObjectArrayElement(*signaturesArray, i);
//		LOGI("checkPackageSign: got signature at %d", i);
//		if (!checkIsInstanceOf(env,signature,"android/content/pm/Signature")){return 0;}
		jclass classOfSignature = env->FindClass("android/content/pm/Signature");
//		LOGI("checkPackageSign: get duoc signature");

		std::string toByteArrayStr = "toByteArray";
		jmethodID toByteArrayMethodId = env->GetMethodID( classOfSignature, toByteArrayStr.c_str(),"()[B");
		if (toByteArrayMethodId == NULL || toByteArrayMethodId == 0) {return 0;}
//		LOGI("checkPackageSign: check xong toByteArray method");

		jobject bytesObj = env->CallObjectMethod( signature,toByteArrayMethodId);
		if (bytesObj == NULL) {return 0;}
		jbyteArray* byteArray = reinterpret_cast<jbyteArray*>(&bytesObj);
//		LOGI("checkPackageSign: get duoc bytesObj");

		jclass classOfMessageDigest = env->FindClass("java/security/MessageDigest");

		std::string getInstanceStr = "getInstance";
		jmethodID getInstanceMethodId = env->GetStaticMethodID( classOfMessageDigest, getInstanceStr.c_str(),"(Ljava/lang/String;)Ljava/security/MessageDigest;");
		if (getInstanceMethodId == NULL || toByteArrayMethodId == 0) {return 0;}
//		LOGI("checkPackageSign: check xong getInstance method");
		jstring shaDigest = convertCharToJstring(env, "SHA");
		jobject messageDigest = env->CallStaticObjectMethod( classOfMessageDigest,getInstanceMethodId,shaDigest);
		if (messageDigest == NULL) {return 0;}
//		LOGI("checkPackageSign: get duoc messageDigest");

		std::string updateStr = "update";
		jmethodID updateMethodId = env->GetMethodID( classOfMessageDigest, updateStr.c_str(),"([B)V");
		if (updateMethodId == NULL || updateMethodId == 0) {return 0;}
//		LOGI("checkPackageSign: check xong update method");
		env->CallVoidMethod( messageDigest,updateMethodId,*byteArray);

		std::string digestStr = "digest";
		jmethodID digestMethodId = env->GetMethodID( classOfMessageDigest, digestStr.c_str(),"()[B");
		if (digestMethodId == NULL || digestMethodId == 0) {return 0;}
//		LOGI("checkPackageSign: check xong digest method");

		jobject digestedBytesObj = env->CallObjectMethod(messageDigest,digestMethodId);
		if (digestedBytesObj == NULL) {return 0;}
		jbyteArray* digestedByteArray = reinterpret_cast<jbyteArray*>(&digestedBytesObj);

		jclass classOfBase64 = env->FindClass("android/util/Base64");
		std::string encodeStr = "encode";
		jmethodID encodeMethodId = env->GetStaticMethodID( classOfBase64, encodeStr.c_str(),"([BI)[B");
		if (encodeMethodId == NULL || encodeMethodId == 0) {return 0;}
//		LOGI("checkPackageSign: check xong encode method");
		jint flags = 0;
		jobject base64BytesObj = env->CallStaticObjectMethod( classOfBase64,encodeMethodId,*digestedByteArray,flags);
		if (base64BytesObj == NULL) {return 0;}
		jbyteArray* base64ByteArray = reinterpret_cast<jbyteArray*>(&base64BytesObj);
//		LOGI("checkPackageSign: get duoc messageDigest");

		//LOGI("chuan bi check package sign xem co dung ko nhe");
		const char *request_package_sign = as_unsigned_char_array(env, *base64ByteArray);

		std::vector<std::string> packageSignStrs;
		{
			char packageSignChars[31];
			strcpy(packageSignChars, "");
			strcat(packageSignChars, "");
			strcat(packageSignChars, "");
			strcat(packageSignChars, "");
			strcat(packageSignChars, "");
			strcat(packageSignChars, "");
			strcat(packageSignChars, "");
			std::string packageSignStr(packageSignChars); //
			packageSignStrs.push_back(packageSignStr);
		}
		{
			char packageSignChars[31];
			strcpy(packageSignChars, "");
			strcat(packageSignChars, "");
			strcat(packageSignChars, "");
			strcat(packageSignChars, "");
			strcat(packageSignChars, "");
			strcat(packageSignChars, "");
			strcat(packageSignChars, "=");
			//package sign zbus debug may hacon:
			std::string packageSignStr(packageSignChars);
			packageSignStrs.push_back(packageSignStr);
		}

//		LOGI("pack cua app nao = %s", request_package_sign);
		//LOGI("pack zbus = %s", packagenameZBus);

		//LOGI("check xem pack1 co giong pack2 khong");
		for (int i = 0; i<packageSignStrs.size(); i++) {
			int bResult = std::memcmp(request_package_sign, packageSignStrs[i].data(), packageSignStrs[i].length());
			if (bResult == 0) {
				//LOGI("Tao la zbus day cho tao vao voi");
				return true;
			}
		}

	}
	// Don't forget to release it
	LOGI("Thang bo lao vao day lam j");
	return false;
}



