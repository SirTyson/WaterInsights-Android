#pragma once

#include <opencv2/core/core.hpp>

/*
 * Processes Image and returns the target, or empty target on error. 
 * If debug == true, function draws target on srcImage.
 */
cv::RotatedRect getTarget(const cv::Mat srcImage, bool debug = false);

/*
 * Processes Image and returns a vector of all reference squares, or empty vector on error.
 * If debug == true, function draws reference squares on srcImage.
 */
std::pair<std::vector<cv::RotatedRect>, cv::RotatedRect> getReferenceSquares(const cv::Mat srcImage, const cv::RotatedRect& target, bool debug);

/*
 * Processes Image and returns the sample square, or empty target on error.
 * If debug == true, function draws sample square on srcImage.
 */
cv::RotatedRect getSampleSquare(const cv::Mat srcImage, const cv::RotatedRect& target, const cv::RotatedRect& ref, bool debug = false);

/*
 * Processes Image and returns the PPM value. Returns -1 on error. Assumes 
 * Image appropriately resized. Prints debug info if debug == true.
 */
int processImage(const cv::Mat src, bool debug = false);

int processImageFromFile(const std::string& file);

/* DEBUG FUNCTIONS */
void DEBUG_DRAW_TARGET(cv::Mat src);
void DEBUG_DRAW_REFERENCE(cv::Mat src);
void DEBUG_DRAW_SAMPLE(cv::Mat src);
void DEBUG_DRAW_REFERENCE_REGION(cv::Mat src);
