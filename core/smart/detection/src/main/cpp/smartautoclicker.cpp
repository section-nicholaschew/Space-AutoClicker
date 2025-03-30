/*
 * Copyright (C) 2022 Kevin Buzeau
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

#include "jni/jni_helper.h"
#include "detection/ocr_detector.hpp"

using namespace smartautoclicker;

extern "C" {

    JNIEXPORT jlong JNICALL Java_com_buzbuz_smartautoclicker_core_detection_NativeDetector_newDetector(
            JNIEnv *env,
            jobject self,
            jobject result) {

        auto detector = new Detector();
        detector->initialize(env, result);
        return reinterpret_cast<jlong>(detector);
    }

    JNIEXPORT void JNICALL Java_com_buzbuz_smartautoclicker_core_detection_NativeDetector_updateScreenMetrics(
            JNIEnv *env,
            jobject self,
            jstring metricsTag,
            jobject screenBitmap,
            jdouble detectionQuality) {

        getObject(env, self)->setScreenMetrics(env, metricsTag, screenBitmap, detectionQuality);
    }

    JNIEXPORT void JNICALL Java_com_buzbuz_smartautoclicker_core_detection_NativeDetector_setScreenImage(
            JNIEnv *env,
            jobject self,
            jobject screenBitmap) {

        getObject(env, self)->setScreenImage(env, screenBitmap);
    }

    JNIEXPORT void JNICALL Java_com_buzbuz_smartautoclicker_core_detection_NativeDetector_detect(
            JNIEnv *env,
            jobject self,
            jobject conditionBitmap,
            jint threshold) {

        getObject(env, self)->detectCondition(env, conditionBitmap, threshold);
    }

    JNIEXPORT void JNICALL Java_com_buzbuz_smartautoclicker_core_detection_NativeDetector_detectAt(
            JNIEnv *env,
            jobject self,
            jobject conditionBitmap,
            jint x,
            jint y,
            jint width,
            jint height,
            jint threshold) {

        getObject(env, self)->detectCondition(env, conditionBitmap, x, y, width, height, threshold);
    }

    JNIEXPORT void JNICALL Java_com_buzbuz_smartautoclicker_core_detection_NativeDetector_deleteDetector(
            JNIEnv *env,
            jobject self) {

        auto detector = getObject(env, self);
        detector->release(env);
        delete detector;
    }

    // OCR related functions
    JNIEXPORT jboolean JNICALL Java_com_buzbuz_smartautoclicker_core_detection_NativeDetector_initializeOcr(
            JNIEnv *env,
            jobject self,
            jstring dataPath,
            jstring language) {

        auto detector = getObject(env, self);
        
        // Convert jstring to std::string
        const char *dataPathChars = env->GetStringUTFChars(dataPath, nullptr);
        const char *languageChars = env->GetStringUTFChars(language, nullptr);
        
        std::string dataPathStr(dataPathChars);
        std::string languageStr(languageChars);
        
        // Release the string resources
        env->ReleaseStringUTFChars(dataPath, dataPathChars);
        env->ReleaseStringUTFChars(language, languageChars);
        
        // Initialize OCR with the provided parameters
        bool result = detector->initializeOcr(dataPathStr, languageStr);
        
        return static_cast<jboolean>(result);
    }

    JNIEXPORT jobject JNICALL Java_com_buzbuz_smartautoclicker_core_detection_NativeDetector_detectTextFull(
            JNIEnv *env,
            jobject self,
            jfloat minConfidence) {

        auto detector = getObject(env, self);
        auto result = detector->detectText(minConfidence);
        return result.toJavaObject(env);
    }

    JNIEXPORT jobject JNICALL Java_com_buzbuz_smartautoclicker_core_detection_NativeDetector_detectTextAt(
            JNIEnv *env,
            jobject self,
            jint x,
            jint y,
            jint width,
            jint height,
            jfloat minConfidence) {

        auto detector = getObject(env, self);
        cv::Rect roi(x, y, width, height);
        auto result = detector->detectText(roi, minConfidence);
        return result.toJavaObject(env);
    }

    JNIEXPORT jobject JNICALL Java_com_buzbuz_smartautoclicker_core_detection_NativeDetector_findTextFull(
            JNIEnv *env,
            jobject self,
            jstring textToFind,
            jboolean exactMatch,
            jfloat minConfidence) {

        auto detector = getObject(env, self);
        
        // Convert jstring to std::string
        const char *textChars = env->GetStringUTFChars(textToFind, nullptr);
        std::string textStr(textChars);
        env->ReleaseStringUTFChars(textToFind, textChars);
        
        auto result = detector->findText(textStr, static_cast<bool>(exactMatch), minConfidence);
        return result.toJavaObject(env);
    }

    JNIEXPORT jobject JNICALL Java_com_buzbuz_smartautoclicker_core_detection_NativeDetector_findTextAt(
            JNIEnv *env,
            jobject self,
            jstring textToFind,
            jint x,
            jint y,
            jint width,
            jint height,
            jboolean exactMatch,
            jfloat minConfidence) {

        auto detector = getObject(env, self);
        
        // Convert jstring to std::string
        const char *textChars = env->GetStringUTFChars(textToFind, nullptr);
        std::string textStr(textChars);
        env->ReleaseStringUTFChars(textToFind, textChars);
        
        cv::Rect roi(x, y, width, height);
        auto result = detector->findText(textStr, roi, static_cast<bool>(exactMatch), minConfidence);
        return result.toJavaObject(env);
    }
}
