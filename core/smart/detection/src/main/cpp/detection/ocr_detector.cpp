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

#include "ocr_detector.hpp"
#include "../utils/log.h"

#include <opencv2/imgproc.hpp>

#define LOG_TAG "OcrDetector"
#include <algorithm>

using namespace smartautoclicker;

OcrDetector::OcrDetector() : _initialized(false) {
#ifdef ENABLE_OCR
    _tessApi = std::make_unique<tesseract::TessBaseAPI>();
#endif
}

OcrDetector::~OcrDetector() {
#ifdef ENABLE_OCR
    if (_tessApi) {
        _tessApi->End();
    }
#endif
}

bool OcrDetector::initialize(const std::string& dataPath, const std::string& language) {
#ifdef ENABLE_OCR
    if (_tessApi) {
        int result = _tessApi->Init(dataPath.c_str(), language.c_str());
        if (result == 0) {
            _initialized = true;
            LOGI("Tesseract OCR initialized successfully with language: %s", language.c_str());
            return true;
        } else {
            LOGE("Failed to initialize Tesseract OCR. Code: %d", result);
        }
    }
    return false;
#else
    LOGW(LOG_TAG, "OCR is not enabled in this build");
    return false;
#endif
}

OcrResult OcrDetector::detectText(const cv::Mat& image, const cv::Rect& roi, float minConfidence) {
#ifdef ENABLE_OCR
    if (!_initialized || !_tessApi) {
        LOGE("OCR not initialized");
        return OcrResult();
    }

    cv::Mat processedImage;
    cv::Rect boundingBox;
    
    // Process region of interest if specified
    if (roi.width > 0 && roi.height > 0) {
        cv::Rect safeRoi = roi & cv::Rect(0, 0, image.cols, image.rows);
        processedImage = image(safeRoi);
        boundingBox = safeRoi;
    } else {
        processedImage = image;
        boundingBox = cv::Rect(0, 0, image.cols, image.rows);
    }

    // Ensure image is in grayscale for OCR
    if (processedImage.channels() != 1) {
        cv::cvtColor(processedImage, processedImage, cv::COLOR_BGR2GRAY);
    }
    
    // Optional: Improve OCR accuracy with preprocessing
    // cv::GaussianBlur(processedImage, processedImage, cv::Size(3, 3), 0);
    // cv::threshold(processedImage, processedImage, 0, 255, cv::THRESH_BINARY | cv::THRESH_OTSU);
    
    // Perform OCR
    _tessApi->SetImage(processedImage.data, processedImage.cols, processedImage.rows, 
                       processedImage.channels(), static_cast<int>(processedImage.step));
    
    // Get text
    char* text = _tessApi->GetUTF8Text();
    float confidence = _tessApi->MeanConfidence() / 100.0f; // Convert to 0-1 range
    
    // Cleanup and create result
    std::string detectedText(text);
    delete[] text;
    
    // Trim whitespace from detected text
    detectedText.erase(detectedText.begin(), 
                       std::find_if(detectedText.begin(), detectedText.end(), 
                                   [](unsigned char ch) { return !std::isspace(ch); }));
    detectedText.erase(std::find_if(detectedText.rbegin(), detectedText.rend(), 
                                   [](unsigned char ch) { return !std::isspace(ch); }).base(), 
                       detectedText.end());
    
    // Check if confidence threshold is met
    bool recognized = !detectedText.empty() && confidence >= minConfidence;
    
    return OcrResult(recognized, detectedText, confidence * 100.0f, boundingBox); // Convert back to 0-100 range
#else
    LOGW(LOG_TAG, "OCR is not enabled in this build");
    return OcrResult();
#endif
}

OcrResult OcrDetector::findText(const cv::Mat& image, const std::string& textToFind, 
                            const cv::Rect& roi, bool exactMatch, float minConfidence) {
#ifdef ENABLE_OCR
    // First detect any text in the image
    OcrResult result = detectText(image, roi, minConfidence);
    
    if (!result.isRecognized()) {
        return result; // No text was recognized or confidence too low
    }
    
    const std::string& detectedText = result.getText();
    bool textFound;
    
    // Check if the text matches what we're looking for
    if (exactMatch) {
        // For exact match, compare strings directly (case-sensitive)
        textFound = detectedText == textToFind;
    } else {
        // For partial match, convert both to lowercase and search
        std::string lowerDetected = detectedText;
        std::string lowerSearched = textToFind;
        std::transform(lowerDetected.begin(), lowerDetected.end(), lowerDetected.begin(), ::tolower);
        std::transform(lowerSearched.begin(), lowerSearched.end(), lowerSearched.begin(), ::tolower);
        
        textFound = lowerDetected.find(lowerSearched) != std::string::npos;
    }
    
    // Return the result with updated recognition status
    return OcrResult(textFound, detectedText, result.getConfidence(), result.getBoundingBox());
#else
    LOGW(LOG_TAG, "OCR is not enabled in this build");
    return OcrResult();
#endif
}

bool OcrDetector::isInitialized() const {
    return _initialized;
}

OcrResult OcrDetector::processOcrResult(const std::string& detectedText, float confidence, const cv::Rect& boundingBox) {
    bool recognized = !detectedText.empty() && confidence > 0.0f;
    return OcrResult(recognized, detectedText, confidence, boundingBox);
}
