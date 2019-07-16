#pragma once

#include <opencv2/core/core.hpp>

struct Target {
	cv::Point2f tl;
	cv::Point2f tr;
	cv::Point2f bl;
	cv::Point2f br;
};

/*
 * Processes Image and returns the target, or empty target on error. 
 * If debug == true, function draws target on srcImage.
 */
Target getTarget(const cv::Mat srcImage, bool debug = false);

/*
 * Processes Image and returns a vector of all reference squares, or empty vector on error.
 * If debug == true, function draws reference squares on srcImage.
 */
std::vector<cv::Rect> getReferenceSquares(const cv::Mat srcImage, const Target& target, bool debug = false);

/*
 * Processes Image and returns the sample square, or empty target on error.
 * If debug == true, function draws sample square on srcImage.
 */
cv::Rect getSampleSquare(const cv::Mat srcImage, const Target& target, bool debug = false);

/*
 * Processes Image and returns the PPM value. Returns -1 on error. Assumes 
 * Image appropriately resized. Prints debug info if debug == true.
 */
int processImage(const cv::Mat src, bool debug = false);

/* DEBUG FUNCTIONS */
void DEBUG_DRAW_TARGET(cv::Mat src);
void DEBUG_DRAW_REFERENCE(cv::Mat src);
void DEBUG_DRAW_SAMPLE(cv::Mat src);
