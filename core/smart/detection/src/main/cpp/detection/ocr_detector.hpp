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
#ifndef SMART_AUTOCLICKER_OCR_DETECTOR_HPP
#define SMART_AUTOCLICKER_OCR_DETECTOR_HPP

#include <string>
#include <memory>
#include <opencv2/core.hpp>

#ifdef ENABLE_OCR
#include <tesseract/baseapi.h>
#endif

#include "../types/ocr_result.hpp"

namespace smartautoclicker {

/**
 * Class responsible for OCR text detection using Tesseract.
 */
class OcrDetector {
public:
    /**
     * Create a new OCR detector.
     */
    OcrDetector();
    
    /**
     * Destroy the OCR detector.
     */
    ~OcrDetector();
    
    /**
     * Initialize the OCR engine with the specified language.
     * @param dataPath path to the tessdata directory.
     * @param language language to use for OCR (e.g., "eng").
     * @return true if initialization was successful, false otherwise.
     */
    bool initialize(const std::string& dataPath, const std::string& language);
    
    /**
     * Detect text in the specified image.
     * @param image the image to detect text in.
     * @param roi region of interest in the image, or empty for the whole image.
     * @param minConfidence minimum confidence level for text to be considered valid (0-100).
     * @return the OCR result.
     */
    OcrResult detectText(const cv::Mat& image, const cv::Rect& roi = cv::Rect(), float minConfidence = 0.0f);
    
    /**
     * Check if text exists in the specified image.
     * @param image the image to check for text.
     * @param textToFind the text to find.
     * @param roi region of interest in the image, or empty for the whole image.
     * @param exactMatch if true, requires exact match; if false, checks if textToFind is contained in detected text.
     * @param minConfidence minimum confidence level for text to be considered valid (0-100).
     * @return the OCR result.
     */
    OcrResult findText(const cv::Mat& image, const std::string& textToFind, 
                    const cv::Rect& roi = cv::Rect(), bool exactMatch = false, 
                    float minConfidence = 0.0f);
    
    /**
     * Check if the OCR engine is initialized.
     * @return true if initialized, false otherwise.
     */
    bool isInitialized() const;

private:
#ifdef ENABLE_OCR
    std::unique_ptr<tesseract::TessBaseAPI> _tessApi;
#endif
    bool _initialized;
    
    OcrResult processOcrResult(const std::string& detectedText, float confidence, const cv::Rect& boundingBox);
};

} // namespace smartautoclicker

#endif // SMART_AUTOCLICKER_OCR_DETECTOR_HPP
