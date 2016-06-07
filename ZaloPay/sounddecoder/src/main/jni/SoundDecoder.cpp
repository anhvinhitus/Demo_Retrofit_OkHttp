//
// Created by Nguyễn Hữu Hoà on 6/7/16.
//
#include <string>
#include "SoundDecoder.h"
#include "filt.h"
#include "fskdemodulation++.h"

class SoundDecoder {
private:
    struct FSKDemodulationConfig _demoConfig;
    FSKDemodulation *_fskDemodulation;
    FSKDemodulationStatus _status;
    std::string _outputBuffer;

public:
    bool initializeConfig();
    void releaseSession();
    FSKDemodulationStatus detect(const void* bytes, uint32_t length);
    std::string getOutput() const {
        return _outputBuffer;
    }
};

// JNI Implementation

jfieldID getHandleField(JNIEnv *env, jobject obj) {
    jclass c = env->GetObjectClass(obj);
    // J is the type signature for long:
    return env->GetFieldID(c, "nativeHandle", "J");
}

template <typename T>
T *getHandle(JNIEnv *env, jobject obj) {
    jlong handle = env->GetLongField(obj, getHandleField(env, obj));
    return reinterpret_cast<T *>(handle);
}

template <typename T>
void setHandle(JNIEnv *env, jobject obj, T *t) {
    jlong handle = reinterpret_cast<jlong>(t);
    env->SetLongField(obj, getHandleField(env, obj), handle);
}

jobject getDelegateObject(JNIEnv *env, jobject obj) {
    jclass c = env->GetObjectClass(obj);
    // J is the type signature for long:
    jfieldID field = env->GetFieldID(c, "listener", "Lvn/com/vng/zalopay/sound/transcoder/DecoderListener;");

    jobject listener = env->GetObjectField(obj, field);
    return listener;
}

void callDelegateObjectMethod_ErrorDetectData(JNIEnv *env, jobject obj) {
    // First get the class that contains the method you need to call
    jclass clazz = env->FindClass("vn/com/vng/zalopay/sound/transcoder/Decoder");
    // Get the method that you want to call
    jmethodID methodID = env->GetMethodID(clazz, "onErrorDetectData", "()V");

    env->CallVoidMethod(obj, methodID);
}

void callDelegateObjectMethod_DidDetectData(JNIEnv *env, jobject obj, jbyteArray data) {
    // First get the class that contains the method you need to call
    jclass clazz = env->FindClass("vn/com/vng/zalopay/sound/transcoder/Decoder");
    // Get the method that you want to call
    jmethodID methodID = env->GetMethodID(clazz, "onDidDetectData", "([B)V");

    env->CallVoidMethod(obj, methodID, data);
}

JNIEXPORT jlong JNICALL Java_vn_com_vng_zalopay_sound_transcoder_Decoder_initializeDecoder
        (JNIEnv *env, jobject obj) {
    SoundDecoder *p = new SoundDecoder();
    setHandle<SoundDecoder>(env, obj, p);

    bool result = p->initializeConfig();
    if (!result) {
        return 0;
    } else {
        return 1;
    }
}

/*
 * Class:     vn_com_vng_zalopay_sound_transcoder_Decoder
 * Method:    processBuffer
 * Signature: ([B)J
 */
JNIEXPORT jlong JNICALL Java_vn_com_vng_zalopay_sound_transcoder_Decoder_processBuffer
        (JNIEnv *env, jobject obj, jbyteArray buffer) {
    SoundDecoder* p = getHandle<SoundDecoder>(env, obj);
    if (p == NULL) {
        return 0;
    }

    jbyte* bufferPtr = env->GetByteArrayElements(buffer, NULL);
    jsize lengthOfArray = env->GetArrayLength(buffer);
    FSKDemodulationStatus status = p->detect(bufferPtr, (uint32_t)lengthOfArray);
    env->ReleaseByteArrayElements(buffer, bufferPtr, 0);
    if (status == fskDemodulationStatusEnd) {
        std::string output = p->getOutput();
        if (output.empty()) {
            callDelegateObjectMethod_ErrorDetectData(env, obj);
        } else {
            jbyteArray arr = env->NewByteArray(output.size());
            env->SetByteArrayRegion(arr, 0, output.size(), (const jbyte*) output.c_str());
            callDelegateObjectMethod_DidDetectData(env, obj, arr);
        }
    }

    return status;
}

/*
 * Class:     vn_com_vng_zalopay_sound_transcoder_Decoder
 * Method:    releaseDecoder
 * Signature: ()J
 */
JNIEXPORT jlong JNICALL Java_vn_com_vng_zalopay_sound_transcoder_Decoder_releaseDecoder
        (JNIEnv *env, jobject obj) {
    SoundDecoder* p = getHandle<SoundDecoder>(env, obj);
    if (p == NULL) {
        return 0;
    }

    p->releaseSession();
    return 1;
}


/// CPP Implementation

bool SoundDecoder::initializeConfig() {
    _demoConfig.dutyCycle = 0.5;
    _demoConfig.activeTime = 5;
    _demoConfig.silenceTime = 5;

    _demoConfig.samplingRate = 44100;
    _demoConfig.maxBitNumber = 1000;
    _demoConfig.header1PeakNumber = 4;

    // TODO: Why 2 centers of intensity are 12400 and 17500, not 13000, 19000 one reason is BUG
//    _demoConfig.f0 = 12400;
//    _demoConfig.f1 = 17500;

    // New Buzzer
    _demoConfig.f0 = 12000;
    _demoConfig.f1 = 17000;

    _demoConfig.bandWidth = 200;
    _demoConfig.numTaps = 30;

    // in ms
    _demoConfig.enjNSum = (int)(_demoConfig.samplingRate * (_demoConfig.activeTime / 1000.0) / 2);
    _demoConfig.maximumInterval = 64; //TODO: set depend on _demoConfig.*
    _demoConfig.thresholdEnj0 = 0.00005;
    _demoConfig.thresholdEnj1 = 0.00005;
    _demoConfig.binaryThreshold = 1;

    _fskDemodulation = new FSKDemodulation(_demoConfig);

    _status = fskDemodulationStatusStart;
    return true;
}

void SoundDecoder::releaseSession() {
    if (_fskDemodulation != NULL) {
        delete _fskDemodulation;
        _fskDemodulation = NULL;
    }
}

FSKDemodulationStatus SoundDecoder::detect(const void* bytes, uint32_t length) {
    if (_status == fskDemodulationStatusEnd) {
        return _status;
    }

    for (uint32_t index = 0; index < length/2; index ++) {
        int16_t value = ((int16_t*)bytes)[index];
        float sample = value / 32768.0;
        _status = _fskDemodulation->processOne(sample);
        if (_status == fskDemodulationStatusEnd) {
            int textLength;
            char *text = _fskDemodulation->output(textLength);
            if (text) {
                _outputBuffer.assign(text, textLength);
            }
            else {
                _outputBuffer.clear();
            }

            break;
        }
    }

    return _status;
}
