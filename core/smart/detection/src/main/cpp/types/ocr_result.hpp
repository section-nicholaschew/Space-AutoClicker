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
#ifndef SMART_AUTOCLICKER_OCR_RESULT_HPP
#define SMART_AUTOCLICKER_OCR_RESULT_HPP

#include <jni.h>
#include <opencv2/core.hpp>

namespace smartautoclicker {

/** Holds the OCR text recognition results. */
class OcrResult {
public:
    /**
     * Create a new OCR recognition result.
     * @param recognized true if the text was recognized, false if not.
     * @param text the detected text if recognized is true, empty otherwise.
     * @param confidence the confidence level of the recognition (0-100).
     * @param boundingBox the bounding box of the detected text.
     */
    OcrResult(bool recognized, const std::string& text, float confidence, const cv::Rect& boundingBox) :
            _recognized(recognized), _text(text), _confidence(confidence), _boundingBox(boundingBox) {}
    
    // For compatibility when OCR is not enabled
    OcrResult() : _recognized(false), _text(""), _confidence(0.0f), _boundingBox(cv::Rect()) {}

    /** @return true if the text was detected correctly, false if not. */
    [[nodiscard]] bool isRecognized() const { return _recognized; }
    
    /** @return the detected text if recognized is true, empty otherwise. */
    [[nodiscard]] const std::string& getText() const { return _text; }
    
    /** @return the confidence level of the detection (0-100). */
    [[nodiscard]] float getConfidence() const { return _confidence; }
    
    /** @return the bounding box of the detected text. */
    [[nodiscard]] const cv::Rect& getBoundingBox() const { return _boundingBox; }

    /**
     * Convert this OCR result to a Java OcrResult object.
     * @param env the JNI environment.
     * @return a new Java OcrResult object.
     */
    jobject toJavaObject(JNIEnv *env) const;

private:
    bool _recognized;
    std::string _text;
    float _confidence;
    cv::Rect _boundingBox;
};

} // namespace smartautoclicker

#endif // SMART_AUTOCLICKER_OCR_RESULT_HPP
