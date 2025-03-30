/*
 * Copyright (C) 2025 Kevin Buzeau
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

#include "ocr_result.hpp"
#include "../jni/jni_helper.h"

using namespace smartautoclicker;

jobject OcrResult::toJavaObject(JNIEnv *env) const {
    // Find the OcrResult Java class
    jclass javaClass = env->FindClass("com/buzbuz/smartautoclicker/core/detection/OcrResult");
    if (javaClass == nullptr) {
        LOGE("Failed to find OcrResult Java class");
        return nullptr;
    }

    // Find the constructor
    jmethodID constructor = env->GetMethodID(javaClass, "<init>", "(ZLjava/lang/String;FLandroid/graphics/Rect;)V");
    if (constructor == nullptr) {
        LOGE("Failed to find OcrResult constructor");
        env->DeleteLocalRef(javaClass);
        return nullptr;
    }

    // Create the Java string for detected text
    jstring text = env->NewStringUTF(_text.c_str());

    // Create the Java Rect for bounding box
    jclass rectClass = env->FindClass("android/graphics/Rect");
    if (rectClass == nullptr) {
        LOGE("Failed to find Rect class");
        env->DeleteLocalRef(javaClass);
        env->DeleteLocalRef(text);
        return nullptr;
    }

    jmethodID rectConstructor = env->GetMethodID(rectClass, "<init>", "(IIII)V");
    if (rectConstructor == nullptr) {
        LOGE("Failed to find Rect constructor");
        env->DeleteLocalRef(javaClass);
        env->DeleteLocalRef(text);
        env->DeleteLocalRef(rectClass);
        return nullptr;
    }

    jobject boundingBox = env->NewObject(rectClass, rectConstructor, 
                                        _boundingBox.x, _boundingBox.y, 
                                        _boundingBox.x + _boundingBox.width, 
                                        _boundingBox.y + _boundingBox.height);

    // Create and return the OcrResult Java object
    jobject result = env->NewObject(javaClass, constructor, 
                                  static_cast<jboolean>(_recognized), 
                                  text, 
                                  static_cast<jfloat>(_confidence),
                                  boundingBox);

    // Clean up local references
    env->DeleteLocalRef(javaClass);
    env->DeleteLocalRef(text);
    env->DeleteLocalRef(rectClass);
    env->DeleteLocalRef(boundingBox);

    return result;
}
