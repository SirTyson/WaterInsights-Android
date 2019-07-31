#include "VisionFunctions.h"
#include "VisionConstants.h"
#include <opencv2/imgproc/imgproc.hpp>
#include <opencv2/highgui/highgui.hpp>
#include <iostream>
#include <android/log.h>

/* TARGET FINDING */
static bool acceptLinePair(cv::Vec2f line1, cv::Vec2f line2, double minTheta);
static cv::Point2f computeIntersect(cv::Vec2f line1, cv::Vec2f line2);
static std::vector<cv::Point2f> lineToPointPair(cv::Vec2f line);
static cv::RotatedRect getReferenceRegion(const cv::Mat srcImage, const cv::RotatedRect& target);

/* COLOR ANALYSIS */
static cv::Vec3d getDominantColor(const cv::Mat srcImg, const cv::RotatedRect& targetRect);
static void RGBtoHSV(double& fR, double& fG, double fB, double& fH, double& fS, double& fV);
static size_t getClosestColorIndex(std::vector<cv::Vec3d>& ref, cv::Vec3d& target, int OP_CODE, bool debug = true);
static void normalizeColors(std::vector<cv::Vec3d>& colors, cv::Vec3d& target);

/* GENERAL CORRECTION */
static double getRelativeVecLength(const cv::RotatedRect& target, double length);
static double getRelativeArea(const cv::RotatedRect& target, double area);
static cv::Point2f getOrthogonalEndpoint(const cv::Point2f& p1, const cv::Point2f& p2, double length);
static cv::Point2f getAxelPoint(const cv::Point2f& p1, const cv::Point2f& p2, double vectorLen);

/* HELPER FUNCTIONS */
static bool isNullSquare(const cv::RotatedRect& rect);
static double getDistance(const cv::Point2f& p1, const cv::Point2f& p2);
static double computeProduct(cv::Point p, cv::Point2f a, cv::Point2f b);
static bool isInROI(cv::Point p, cv::Point2f roi[]);
static float indexToValue(int index, int OP_CODE);
static bool doesRectangleContainPoint(const cv::RotatedRect& rectangle, const cv::Point2f& point);

/* DEBUG FUNCTION */
static void print(const cv::Vec3d& v);
static void drawRotatedRect(cv::Mat src, const cv::RotatedRect& rect);

std::vector<float> processImageFromFile(const std::string& file, int OP_CODE)
{
    __android_log_print(ANDROID_LOG_ERROR, "OpenCV", "Processing Image");
    cv::Mat src = cv::imread(file);

    double ratio = src.size().height / 600.0;
    cv::resize(src, src, cv::Size(src.size().width / ratio, 600));

    return processImage(src, OP_CODE);
}

cv::RotatedRect getTarget(const cv::Mat srcImage, bool debug)
{
	cv::Mat occludedSquare8u;
	cv::cvtColor(srcImage, occludedSquare8u, cv::COLOR_BGR2GRAY);

	for (double threshLower = TARGET_THRESH_LOWER_MAX;
		threshLower >= TARGET_THRESH_LOWER_MIN;
		threshLower -= TARGET_THRESH_LOWER_STEP)
	{
		cv::Mat thresh;
		cv::threshold(occludedSquare8u, thresh, threshLower, TARGET_THRESH_UPPER, cv::THRESH_BINARY);

		GaussianBlur(thresh, thresh, cv::Size(TARGET_BLUR_SIZE, TARGET_BLUR_SIZE),
			TARGET_BLUR_SIGX, TARGET_BLUR_SIGY);

		cv::Mat edges;
		Canny(thresh, edges, TARGET_CANNY_THRESH_LOWER, TARGET_CANNY_THRESH_UPPER, TARGET_CANNY_AP_SIZE);

		std::vector<std::vector<cv::Point>> contours;
		cv::Mat img(edges.size(), edges.type(), cv::Scalar(0));

		std::vector<cv::RotatedRect> foundRects;

		static const double rectFF = TARGET_RECT_FUDGE_FACTOR;
		findContours(edges, contours, cv::RETR_CCOMP, cv::CHAIN_APPROX_NONE);
		for (size_t i = 0; i < contours.size(); i++)
		{
			cv::Scalar color = cv::Scalar(255, 105, 180);
			cv::RotatedRect rect = cv::minAreaRect(contours[i]);
			if (rect.size.height > TARGET_SIDE_FILTER &&
				rect.size.width > TARGET_SIDE_FILTER &&
				rect.size.width < TARGET_SIDE_MAX &&
				rect.size.height < TARGET_SIDE_MAX &&
				abs(1 - (rect.size.width / rect.size.height)) < rectFF)
			{
				if (debug) std::cout << "Target Ratio: " << abs(1 - (rect.size.width / rect.size.height)) << std::endl;
				foundRects.push_back(rect);
			}
		}

		/* Filter out duplicate squares to get smallest */
		if (!foundRects.empty())
		{
			auto smallest = foundRects.begin();
			float smallestValue = smallest->size.area();
			for (auto iter = foundRects.begin(); iter != foundRects.end(); iter++)
			{
				if (iter->size.area() < smallestValue)
				{
					smallest = iter;
					smallestValue = iter->size.area();
				}
			}
			return *smallest;
		}
	}
	return cv::RotatedRect(
		cv::Point2f(0, 0),
		cv::Point2f(0, 0),
		cv::Point2f(0, 0));
}

std::pair<std::vector<cv::RotatedRect>, cv::RotatedRect> getReferenceSquares(const cv::Mat srcImage, const cv::RotatedRect& target, bool debug)
{
	std::vector<cv::RotatedRect> referenceSquares;

	cv::RotatedRect region = getReferenceRegion(srcImage, target);

	if (isNullSquare(region)) 
		return std::make_pair(referenceSquares, cv::RotatedRect( cv::Point2f(0,0), cv::Point2f(0, 0), cv::Point2f(0, 0)));

	cv::Point2f vrts[4];
	region.points(vrts);

	std::vector<cv::Point2f> vertices;
	for (const auto& point : vrts)
		vertices.push_back(point);

	/* Get 2 points closest to target */
	cv::Point2f refPoints[2];
	for (size_t i = 0; i < 2; i++)
	{
		double least = FLT_MAX;
		size_t leastIndex = -1;
		for (size_t j = 0; j < vertices.size(); j++)
		{
			if (getDistance(target.center, vertices[j]) < least)
			{
				least = getDistance(target.center, vertices[j]);
				leastIndex = j;
			}
		}
		refPoints[i] = vertices[leastIndex];
		vertices.erase(vertices.begin() + leastIndex);
	}

	float length = region.size.width > region.size.height ? region.size.width : region.size.height;
	double lengthSquare = (length - (getRelativeVecLength(target, REF_REGION_OFFSET) * 2)) / NUM_ALKALINITY_REFERENCES;

	/* Determine orientation of reference region */
	cv::Point2f left, right;
	if (abs(refPoints[0].x - refPoints[1].x) > abs(refPoints[0].y - refPoints[1].y))
	{
		if (refPoints[0].x < refPoints[1].x) 
		{
			left = refPoints[0];
			right = refPoints[1];
		}
		else 
		{
			left = refPoints[1];
			right = refPoints[0];
		}
	}
	else
	{
		left = refPoints[0];
		right = refPoints[1];
	}

	static const float SQUARE_SIZE = getRelativeVecLength(target, REF_SQUARE_SIZE);
	for (size_t i = 0; i < NUM_ALKALINITY_REFERENCES; i++)
	{
		cv::Point2f center = getOrthogonalEndpoint(left, right, REF_REGION_OFFSET + ((lengthSquare / 2) + lengthSquare * i));
		cv::RotatedRect rect(center, cv::Size(SQUARE_SIZE, SQUARE_SIZE), region.angle);
		referenceSquares.push_back(rect);
	}

	return std::make_pair(referenceSquares, region);
}

std::vector<cv::RotatedRect> getSampleSquare(const cv::Mat srcImage, const cv::RotatedRect& target, const cv::RotatedRect& ref, const int& OP_CODE, bool debug)
{
	std::vector<cv::RotatedRect> samples;

	/// TODO: Maybe center allign region more
	float ROI_WIDTH, ROI_HEIGHT;
	if (abs(ref.size.width) < abs(ref.size.height))
	{
		ROI_WIDTH = getRelativeVecLength(target, SAMPLE_ROI_WIDTH);
		ROI_HEIGHT = getRelativeVecLength(target, SAMPLE_ROI_HEIGHT);
	}
	else
	{
		ROI_WIDTH = getRelativeVecLength(target, SAMPLE_ROI_HEIGHT);
		ROI_HEIGHT = getRelativeVecLength(target, SAMPLE_ROI_WIDTH);
	}
	cv::Point2f searchRegionMidpoint = getOrthogonalEndpoint(target.center, ref.center, getRelativeVecLength(target, -SAMPLE_REGION_DIST));

	cv::RotatedRect ROI(searchRegionMidpoint, cv::Size(ROI_WIDTH, ROI_HEIGHT), ref.angle);

	cv::Point2f vertices[4];
	ROI.points(vertices);

	cv::Mat mask = cv::Mat(srcImage.size(), CV_8UC1, cv::Scalar(0));
	std::vector<std::vector<cv::Point>> pts{ {vertices[0], vertices[1], vertices[2], vertices[3] } };
	fillPoly(mask, pts, cv::Scalar(255));

	cv::Mat MatROI;
	srcImage.copyTo(MatROI, mask);

	cv::Mat occludedSquare8u;
	cv::cvtColor(MatROI, occludedSquare8u, cv::COLOR_BGR2GRAY);

	cv::Mat thresh;
	cv::threshold(occludedSquare8u, thresh, SAMPLE_THRESH_LOWER, SAMPLE_THRESH_UPPER, cv::THRESH_BINARY);

	GaussianBlur(thresh, thresh, cv::Size(TARGET_BLUR_SIZE, TARGET_BLUR_SIZE),
		TARGET_BLUR_SIGX, TARGET_BLUR_SIGY);

	cv::Mat edges;
	Canny(thresh, edges, TARGET_CANNY_THRESH_LOWER, TARGET_CANNY_THRESH_UPPER, TARGET_CANNY_AP_SIZE);

	std::vector<std::vector<cv::Point>> contours;
	cv::Mat img(edges.size(), edges.type(), cv::Scalar(0));
	std::vector<cv::Point> merged;

	/* Merge non-filtered contours */
	static const float WIDTH_MIN = getRelativeVecLength(target, SAMPLE_WIDTH_LOWER);
	static const float WIDTH_MAX = getRelativeVecLength(target, SAMPLE_WIDTH_UPPER);
	static const float HEIGHT_MIN = getRelativeVecLength(target, SAMPLE_HEIGHT_LOWER);
	static const float HEIGHT_MAX = getRelativeVecLength(target, SAMPLE_HEIGHT_UPPER);
	findContours(edges, contours, cv::RETR_CCOMP, cv::CHAIN_APPROX_NONE);
	for (size_t i = 0; i < contours.size(); i++)
	{
		cv::Scalar color = cv::Scalar(255, 105, 180);
		cv::RotatedRect rect = cv::minAreaRect(contours[i]);
		float width, height;
		if (rect.size.width < rect.size.height)
		{
			width = rect.size.width;
			height = rect.size.height;
		}
		else
		{
			width = rect.size.height;
			height = rect.size.width;
		}

		if (width > WIDTH_MIN &&
			width < WIDTH_MAX &&
			height < HEIGHT_MAX &&
			height > HEIGHT_MIN)
		{
			for (const auto& pt : contours[i])
				merged.push_back(pt);
		}
	}
	if (merged.size() == 0)
		return samples;
	
	cv::RotatedRect testStrip = cv::minAreaRect(merged);

	cv::Point2f points[4];
	testStrip.points(points);

	std::vector<cv::Point2f> vrt;
	for (const auto& point : points)
		vrt.push_back(point);

	/* Get 2 points closest to target */
	cv::Point2f refPoints[2];
	for (size_t i = 0; i < 2; i++)
	{
		double least = FLT_MAX;
		size_t leastIndex = -1;
		for (size_t j = 0; j < vrt.size(); j++)
		{
			if (getDistance(ref.center, vrt[j]) < least)
			{
				least = getDistance(ref.center, vrt[j]);
				leastIndex = j;
			}
		}
		refPoints[i] = vrt[leastIndex];
		vrt.erase(vrt.begin() + leastIndex);
	}

	float testWidth, testHeight;
	if (testStrip.size.width > testStrip.size.height)
	{
		testHeight = testStrip.size.width;
		testWidth = testStrip.size.height;
	}
	else
	{
		testWidth = testStrip.size.width;
		testHeight = testStrip.size.height;
	}
	float targetHeight = target.size.width < target.size.height ? target.size.height : target.size.width;

	if (debug)
	{
		std::cout << "Target-Strip Ratio: " << testHeight / targetHeight << std::endl;
		drawRotatedRect(srcImage, testStrip);
	}

	static const float SQUARE_SIZE = getRelativeArea(target, REF_SQUARE_SIZE);

	/* Correct if entire test strip is detected instead of just lower region */
	if (debug) std::cout << "Test Strip Size Ratio: " << testWidth / testHeight << std::endl << std::endl;

	switch (OP_CODE)
	{
	case CODE_STRIP_3:
		for (int i = 0; i < 2; i++)
		{
			double sampleLength = testHeight / targetHeight > SAMPLE_STRIP_CORRECTION_RATIO ? getRelativeVecLength(target, SAMPLE_TEST_STRIP_CORRECTION) : 0;
			int dist = i == 0 ? SAMPLE_DIST_NITRATE : SAMPLE_DIST_NITRITE;
			sampleLength += getRelativeVecLength(target, dist);
			cv::Point2f center = getOrthogonalEndpoint(refPoints[0], refPoints[1], sampleLength);
			samples.push_back(cv::RotatedRect(center, cv::Size(SQUARE_SIZE, SQUARE_SIZE), testStrip.angle));
		}
		break;

	case CODE_STRIP_4:
	{
		double sampleLength = testHeight / targetHeight > SAMPLE_STRIP_CORRECTION_RATIO ? getRelativeVecLength(target, SAMPLE_TEST_STRIP_CORRECTION) : 0;
		sampleLength += getRelativeVecLength(target, SAMPLE_DIST_COPPER);
		cv::Point2f center = getOrthogonalEndpoint(refPoints[0], refPoints[1], sampleLength);
		samples.push_back(cv::RotatedRect(center, cv::Size(SQUARE_SIZE, SQUARE_SIZE), testStrip.angle));
		break;
	}

	case CODE_STRIP_1:
	{
		for (int i = 0; i < 3; i++)
		{
			double sampleLength = testHeight / targetHeight > SAMPLE_STRIP_CORRECTION_RATIO ? getRelativeVecLength(target, SAMPLE_TEST_STRIP_1_CORRECTION) : 0;
			float length;
			if (i == 0) length = SAMPLE_DIST_TOTAL_HARDNESS;
			else if (i == 1) length = SAMPLE_DIST_ALKALINITY;
			else if (i == 2) length = SAMPLE_DIST_PH;
			sampleLength += getRelativeVecLength(target, length);
			cv::Point2f center = getOrthogonalEndpoint(refPoints[0], refPoints[1], sampleLength);
			samples.push_back(cv::RotatedRect(center, cv::Size(SQUARE_SIZE, SQUARE_SIZE), testStrip.angle));
		}
		break;
	}

	case CODE_STRIP_2:
	{
		for (int i = 0; i < 2; i++)
		{
			double sampleLength = testHeight / targetHeight > SAMPLE_STRIP_CORRECTION_RATIO ? getRelativeVecLength(target, SAMPLE_TEST_STRIP_CORRECTION) : 0;
			float length;
			if (i == 0) length = SAMPLE_DIST_TOTAL_CHLORINE;
			else if (i == 1) length = SAMPLE_DIST_FREE_CHLORINE;
			sampleLength += getRelativeVecLength(target, length);
			cv::Point2f center = getOrthogonalEndpoint(refPoints[0], refPoints[1], sampleLength);
			samples.push_back(cv::RotatedRect(center, cv::Size(SQUARE_SIZE, SQUARE_SIZE), testStrip.angle));
		}
		break;
	}

	default:
		if (debug) std::cout << "ERROR: Invalid OP CODE" << std::endl;
		break;
	}

	return samples;
}

std::vector<float> processImage(const cv::Mat src, int OP_CODE, bool debug)
{
	std::vector<float> values;

	/* Get target and return on error */
	if (debug) std::cout << "########## TARGET ##########\n" << std::endl;
	cv::RotatedRect target = getTarget(src, true);
	if (isNullSquare(target))
	{
		std::cout << "ERROR: Target not found" << std::endl;
		return values;
	}

	/* Get references and return on error */
	if (debug) std::cout << "\n########## REFERENCE ##########\n" << std::endl;
	auto ret = getReferenceSquares(src, target, true);
	std::vector<cv::RotatedRect>& references = ret.first;
	cv::RotatedRect& referenceRegion = ret.second;
	if (references.size() < NUM_ALKALINITY_REFERENCES)
	{
		std::cout << "ERROR: only " << references.size() << " reference squares found" << std::endl;
		return values;
	}

	/* Get sample and return on error */
	if (debug) std::cout << "\n########## SAMPLE ##########\n" << std::endl;

	std::vector<cv::RotatedRect> samples = getSampleSquare(src, target, referenceRegion, OP_CODE, debug);
	if (samples.size() == 0)
	{
		std::cout << "ERROR: could not find sample" << std::endl;
		return values;
	}

	if (debug) std::cout << "\n########## COLORS ##########\n" << std::endl;
	std::vector<cv::Vec3d> refColors;
	for (auto rect : references)
		refColors.insert(refColors.begin(), getDominantColor(src, rect));

	if (debug)
	{
		std::cout << "Reference Colors Raw:" << std::endl;
		for (auto color : refColors)
			std::cout << "H: " << color[0] << " S: " << color[1] << " V: " << color[2] << std::endl;
		std::cout << std::endl;
	}

	switch (OP_CODE)
	{
	case CODE_STRIP_3:
		for (int i = 0; i < 2; i++)
		{
			cv::Vec3d sampleColor = getDominantColor(src, samples[i]);
			if (debug)
			{
				if (i == 0) std::cout << "## NITRATE ##\n" << std::endl;
				else std::cout << "## NITRITE ##\n" << std::endl;
				std::cout << "Sample Color Normal: " << std::endl;
				cv::Vec3d normal = sampleColor;
				normalizeColors(refColors, normal);
				print(normal);
				std::cout << std::endl << std::endl;
			}

			int code = i == 0 ? CODE_NITRATE : CODE_NITRITE;
			size_t sampleIndex = getClosestColorIndex(refColors, sampleColor, code, debug);
			values.push_back(indexToValue(sampleIndex, code));
		}
		break;

	case CODE_STRIP_4:
	{
		cv::Vec3d sampleColor = getDominantColor(src, samples[0]);
		if (debug)
		{
			std::cout << "## COPPER ##\n" << std::endl;
			std::cout << "Sample Color Normal: " << std::endl;
			cv::Vec3d normal = sampleColor;
			normalizeColors(refColors, normal);
			print(normal);
			std::cout << std::endl << std::endl;
		}
		size_t sampleIndex = getClosestColorIndex(refColors, sampleColor, CODE_COPPER, debug);
		values.push_back(indexToValue(sampleIndex, CODE_COPPER));
		break;
	}

	case CODE_STRIP_1:
	{
		for (int i = 0; i < 3; i++)
		{
			int code;
			if (i == 0)
			{
				code = CODE_TOTAL_HARDNESS;
				if (debug) std::cout << "## TOTAL HARDNESS ##\n" << std::endl;
			}
			else if (i == 1)
			{
				code = CODE_ALKALINITY;
				if (debug) std::cout << "## ALKALINITY ##\n" << std::endl;
			}
			else if (i == 2)
			{
				code = CODE_PH;
				if (debug) std::cout << "## PH ##\n" << std::endl;
			}
			cv::Vec3d sampleColor = getDominantColor(src, samples[i]);
			if (debug)
			{
				std::cout << "Sample Color Normal: " << std::endl;
				cv::Vec3d normal = sampleColor;
				normalizeColors(refColors, normal);
				print(normal);
				std::cout << std::endl << std::endl;
			}
			size_t sampleIndex = getClosestColorIndex(refColors, sampleColor, code, debug);
			values.push_back(indexToValue(sampleIndex, code));
		}
		break;
	}

	case CODE_STRIP_2:
	{
		for (int i = 0; i < 2; i++)
		{
			int code;
			if (i == 0)
			{
				code = CODE_TOTAL_CHLORINE;
				if (debug) std::cout << "## TOTAL CHLORINE ##\n" << std::endl;
			}
			else if (i == 1)
			{
				code = CODE_FREE_CHLORINE;
				if (debug) std::cout << "## FREE CHLORINE ##\n" << std::endl;
			}
			cv::Vec3d sampleColor = getDominantColor(src, samples[i]);
			if (debug)
			{
				std::cout << "Sample Color Normal: " << std::endl;
				cv::Vec3d normal = sampleColor;
				normalizeColors(refColors, normal);
				print(normal);
				std::cout << std::endl << std::endl;
			}
			size_t sampleIndex = getClosestColorIndex(refColors, sampleColor, code, debug);
			values.push_back(indexToValue(sampleIndex, code));
		}
		break;

	}

	default:
		std::cout << "ERROR: INVALID OP CODE" << std::endl;
		break;
	}
	
	if (debug) 
	{
		drawRotatedRect(src, target);
		for (auto& rect : references) drawRotatedRect(src, rect);
		for (auto& rect : samples) drawRotatedRect(src, rect);
	}

	return values;
}

static float indexToValue(int index, int OP_CODE)
{
	switch (OP_CODE)
	{
	case CODE_NITRITE:
		switch (index) {
		case 0:
			return NITRITE_0;
			break;
		case 1:
			return NITRITE_1;
			break;
		case 2:
			return NITRITE_2;
			break;
		case 3:
			return NITRITE_3;
			break;
		case 4:
			return NITRITE_4;
			break;
		case 5:
			return NITRITE_5;
			break;
		default:
			return -1;
			break;
		}
		break;

	case CODE_NITRATE:
		switch (index) {
		case 0:
			return NITRATE_0;
			break;
		case 1:
			return NITRATE_1;
			break;
		case 2:
			return NITRATE_2;
			break;
		case 3:
			return NITRATE_3;
			break;
		case 4:
			return NITRATE_4;
			break;
		case 5:
			return NITRATE_5;
			break;
		case 6:
			return NITRATE_6;
			break;
		default:
			return -1;
			break;
		}
		break;

	case CODE_COPPER:
		switch (index) {
		case 0:
			return COPPER_0;
			break;
		case 1:
			return COPPER_1;
			break;
		case 2:
			return COPPER_2;
			break;
		case 3:
			return COPPER_3;
			break;
		default:
			return -1;
			break;
		}
		break;

	case CODE_ALKALINITY:
		switch (index) {
		case 0:
			return ALKALINITY_0;
			break;
		case 1:
			return ALKALINITY_1;
			break;
		case 2:
			return ALKALINITY_2;
			break;
		case 3:
			return ALKALINITY_3;
			break;
		case 4:
			return ALKALINITY_4;
			break;
		case 5:
			return ALKALINITY_5;
			break;
		default:
			return -1;
			break;
		}
		break;

	case CODE_TOTAL_CHLORINE:
		switch (index) {
		case 0:
			return TOTAL_CHLORINE_0;
			break;
		case 1:
			return TOTAL_CHLORINE_1;
			break;
		case 2:
			return TOTAL_CHLORINE_2;
			break;
		case 3:
			return TOTAL_CHLORINE_3;
			break;
		case 4:
			return TOTAL_CHLORINE_4;
			break;
		case 5:
			return TOTAL_CHLORINE_5;
			break;
		default:
			return -1;
			break;
		}
		break;

	case CODE_FREE_CHLORINE:
		switch (index) {
		case 0:
			return FREE_CHLORINE_0;
			break;
		case 1:
			return FREE_CHLORINE_1;
			break;
		case 2:
			return FREE_CHLORINE_2;
			break;
		case 3:
			return FREE_CHLORINE_3;
			break;
		case 4:
			return FREE_CHLORINE_4;
			break;
		case 5:
			return FREE_CHLORINE_5;
			break;
		default:
			return -1;
			break;
		}
		break;

	case CODE_TOTAL_HARDNESS:
		switch (index) {
		case 0:
			return TOTAL_HARDNESS_0;
			break;
		case 1:
			return TOTAL_HARDNESS_1;
			break;
		case 2:
			return TOTAL_HARDNESS_2;
			break;
		case 3:
			return TOTAL_HARDNESS_3;
			break;
		case 4:
			return TOTAL_HARDNESS_4;
			break;
		case 5:
			return TOTAL_HARDNESS_5;
			break;
		default:
			return -1;
			break;
		}
		break;

	case CODE_PH:
		switch (index) {
		case 0:
			return PH_0;
			break;
		case 1:
			return PH_1;
			break;
		case 2:
			return PH_2;
			break;
		case 3:
			return PH_3;
			break;
		case 4:
			return PH_4;
			break;
		default:
			return -1;
			break;
		}
		break;

	default:
		std::cout << "ERROR: INVALID OP CODE" << std::endl;
		return -1;
	}
}

static bool acceptLinePair(cv::Vec2f line1, cv::Vec2f line2, double minTheta)
{
	double theta1 = line1[1], theta2 = line2[1];

	/* Deal with 0 and 180 degree ambiguities */
	if (theta1 < minTheta) theta1 += CV_PI;
	if (theta2 < minTheta) theta2 += CV_PI;

	return abs(theta1 - theta2) > minTheta;
}

static cv::Point2f computeIntersect(cv::Vec2f line1, cv::Vec2f line2)
{
	std::vector<cv::Point2f> p1 = lineToPointPair(line1);
	std::vector<cv::Point2f> p2 = lineToPointPair(line2);

	/* Ref: Wikipedia line-intersection equation */
	double denom = (p1[0].x - p1[1].x) * (p2[0].y - p2[1].y) - (p1[0].y - p1[1].y) * (p2[0].x - p2[1].x);
	cv::Point2f intersect(((p1[0].x * p1[1].y - p1[0].y * p1[1].x) * (p2[0].x - p2[1].x) -
		(p1[0].x - p1[1].x) * (p2[0].x * p2[1].y - p2[0].y * p2[1].x)) / denom,
		((p1[0].x * p1[1].y - p1[0].y * p1[1].x) * (p2[0].y - p2[1].y) -
		(p1[0].y - p1[1].y) * (p2[0].x * p2[1].y - p2[0].y * p2[1].x)) / denom);

	return intersect;
}

static std::vector<cv::Point2f> lineToPointPair(cv::Vec2f line)
{
	std::vector<cv::Point2f> points;

	double r = line[0], t = line[1];
	double cos_t = cos(t), sin_t = sin(t);
	double x0 = r * cos_t, y0 = r * sin_t;
	double alpha = 1000;

	points.push_back(cv::Point2f(x0 + alpha * (-sin_t), y0 + alpha * cos_t));
	points.push_back(cv::Point2f(x0 - alpha * (-sin_t), y0 - alpha * cos_t));

	return points;
}

static cv::RotatedRect getReferenceRegion(const cv::Mat srcImage, const cv::RotatedRect& target)
{
	cv::Mat occludedSquare8u;
	cv::cvtColor(srcImage, occludedSquare8u, cv::COLOR_BGR2GRAY);

	for (double threshLower = REF_REGION_THRESH_LOWER_MAX;
		threshLower >= REF_REGION_THRESH_LOWER_MIN;
		threshLower -= REF_REGION_THRESH_LOWER_STEP)
	{
		cv::Mat thresh;
		cv::threshold(occludedSquare8u, thresh, threshLower, REF_REGION_THRESH_UPPER, cv::THRESH_BINARY);

		GaussianBlur(thresh, thresh, cv::Size(TARGET_BLUR_SIZE, TARGET_BLUR_SIZE),
			TARGET_BLUR_SIGX, TARGET_BLUR_SIGY);

		cv::Mat edges;
		Canny(thresh, edges, TARGET_CANNY_THRESH_LOWER, TARGET_CANNY_THRESH_UPPER, TARGET_CANNY_AP_SIZE);

		std::vector<std::vector<cv::Point>> contours;
		cv::Mat img(edges.size(), edges.type(), cv::Scalar(0));

		static const double rectFF = TARGET_RECT_FUDGE_FACTOR;
		findContours(edges, contours, cv::RETR_CCOMP, cv::CHAIN_APPROX_NONE);

		static const float MIN_AREA = getRelativeArea(target, REF_REGION_AREA_MIN);
		static const float MAX_AREA = getRelativeArea(target, REF_REGION_AREA_MAX);
		for (size_t i = 0; i < contours.size(); i++)
		{
			cv::RotatedRect rect = cv::minAreaRect(contours[i]);
			float width, height;
			if (abs(rect.size.width) < abs(rect.size.height))
			{
				width = rect.size.width;
				height = rect.size.height;
			}
			else
			{
				width = rect.size.height;
				height = rect.size.width;
			}
			if (rect.size.area() > MIN_AREA &&
				rect.size.area() < MAX_AREA &&
				REF_REGION_RATIO_MIN < abs(width / height) &&
				abs(width / height) < REF_REGION_RATIO_MAX)
			{
				if (!doesRectangleContainPoint(rect, target.center))
					return rect;
			}
		}
	}
	return cv::RotatedRect(cv::Point2f(0, 0), cv::Point2f(0, 0), cv::Point2f(0, 0));
}

static cv::Vec3d getDominantColor(const cv::Mat srcImg, const cv::RotatedRect& targetRect) {
	cv::Point2f vertices[4];
	targetRect.points(vertices);

	cv::Mat mask = cv::Mat(srcImg.size(), CV_8UC1, cv::Scalar(0));
	std::vector<std::vector<cv::Point>> pts{ {vertices[0], vertices[1], vertices[2], vertices[3] } };
	fillPoly(mask, pts, cv::Scalar(255));

	cv::Mat target;
	srcImg.copyTo(target, mask);

	/* Get target Mat */
	cv::Mat data;
	target.convertTo(data, CV_32F);
	data = data.reshape(1, data.total());

	cv::Mat labels, centers;
	cv::TermCriteria criteria(TERM_CRITERIA_MAX_COUNT + TERM_CRITERIA_EPSILON, TERM_CRITERIA_MAX_COUNT, TERM_CRITERIA_EPSILON);

	cv::kmeans(data, DOMINANT_COLOR_K, labels, criteria, DOMINANT_COLOR_ATTEMPTS, cv::KMEANS_RANDOM_CENTERS, centers);
	centers.convertTo(centers, CV_8UC3);

	centers = centers.reshape(3, centers.rows);
	data = data.reshape(3, data.rows);

	double totalR = 0.0;
	double totalG = 0.0;
	double totalB = 0.0;

	for (int i = 0; i < centers.rows; i++)
	{
		totalR += centers.at<cv::Vec3b>(i, 0)[2];
		totalG += centers.at<cv::Vec3b>(i, 0)[1];
		totalB += centers.at<cv::Vec3b>(i, 0)[0];
	}

	double r = totalR / BGR_SCALE;
	double g = totalG / BGR_SCALE;
	double b = totalB / BGR_SCALE;

	double h, s, v;
	RGBtoHSV(r, g, b, h, s, v);
	return cv::Vec3d(h, s, v);
}

static void RGBtoHSV(double& fR, double& fG, double fB, double& fH, double& fS, double& fV) {
	double fCMax = std::max(std::max(fR, fG), fB);
	double fCMin = std::min(std::min(fR, fG), fB);
	double fDelta = fCMax - fCMin;

	if (fDelta > 0) 
	{
		if (fCMax == fR)
			fH = 60 * (fmod(((fG - fB) / fDelta), 6));

		else if (fCMax == fG)
			fH = 60 * (((fB - fR) / fDelta) + 2);

		else if (fCMax == fB)
			fH = 60 * (((fR - fG) / fDelta) + 4);

		if (fCMax > 0)
			fS = fDelta / fCMax;

		else
			fS = 0;

		fV = fCMax;
	}
	else
	{
		fH = 0;
		fS = 0;
		fV = fCMax;
	}

	if (fH < 0)
		fH = 360 + fH;
}

static void print(const cv::Vec3d& v)
{
	std::cout << "H: " << v.val[0] << " S: " << v.val[1] << " V: " << v.val[2];
}

static size_t getClosestColorIndex(std::vector<cv::Vec3d>& ref, cv::Vec3d& target, int OP_CODE, bool debug)
{
	normalizeColors(ref, target);

	std::vector<cv::Vec3d> normalizedRef;
	float MAX_DISTANCE = 10.0;
	switch (OP_CODE)
	{
	case CODE_NITRITE:
		MAX_DISTANCE = NITRITE_MAX_DISTANCE;
		normalizedRef.push_back(NITRITE_0_COLOR);
		normalizedRef.push_back(NITRITE_015_COLOR);
		normalizedRef.push_back(NITRITE_03_COLOR);
		normalizedRef.push_back(NITRITE_1_COLOR);
		normalizedRef.push_back(NITRITE_15_COLOR);
		normalizedRef.push_back(NITRITE_3_COLOR);
		break;

	case CODE_NITRATE:
		MAX_DISTANCE = NITRITE_MAX_DISTANCE;
		normalizedRef.push_back(NITRATE_0_COLOR);
		normalizedRef.push_back(NITRATE_1_COLOR);
		normalizedRef.push_back(NITRATE_2_COLOR);
		normalizedRef.push_back(NITRATE_5_COLOR);
		normalizedRef.push_back(NITRATE_10_COLOR);
		normalizedRef.push_back(NITRATE_20_COLOR);
		normalizedRef.push_back(NITRATE_50_COLOR);
		break;

	case CODE_COPPER:
		MAX_DISTANCE = COPPER_MAX_DISTANCE;
		normalizedRef.push_back(COPPER_0_COLOR);
		normalizedRef.push_back(COPPER_02_COLOR);
		normalizedRef.push_back(COPPER_05_COLOR);
		normalizedRef.push_back(COPPER_1_COLOR);
		break;

	case CODE_ALKALINITY:
		MAX_DISTANCE = ALKALINITY_MAX_DISTANCE;
		normalizedRef.push_back(ALKALINITY_0_COLOR);
		normalizedRef.push_back(ALKALINITY_1_COLOR);
		normalizedRef.push_back(ALKALINITY_2_COLOR);
		normalizedRef.push_back(ALKALINITY_3_COLOR);
		normalizedRef.push_back(ALKALINITY_4_COLOR);
		normalizedRef.push_back(ALKALINITY_5_COLOR);
		break;

	case CODE_TOTAL_CHLORINE:
		MAX_DISTANCE = TOTAL_CHLORINE_MAX_DISTANCE;
		normalizedRef.push_back(TOTAL_CHLORINE_0_COLOR);
		normalizedRef.push_back(TOTAL_CHLORINE_05_COLOR);
		normalizedRef.push_back(TOTAL_CHLORINE_1_COLOR);
		normalizedRef.push_back(TOTAL_CHLORINE_2_COLOR);
		normalizedRef.push_back(TOTAL_CHLORINE_4_COLOR);
		normalizedRef.push_back(TOTAL_CHLORINE_10_COLOR);
		break;

	case CODE_FREE_CHLORINE:
		MAX_DISTANCE = FREE_CHLORINE_MAX_DISTANCE;
		normalizedRef.push_back(FREE_CHLORINE_0_COLOR);
		normalizedRef.push_back(FREE_CHLORINE_05_COLOR);
		normalizedRef.push_back(FREE_CHLORINE_1_COLOR);
		normalizedRef.push_back(FREE_CHLORINE_2_COLOR);
		normalizedRef.push_back(FREE_CHLORINE_4_COLOR);
		normalizedRef.push_back(FREE_CHLORINE_10_COLOR);
		break;

	case CODE_TOTAL_HARDNESS:
		MAX_DISTANCE = TOTAL_HARDNESS_MAX_DISTANCE;
		normalizedRef.push_back(TOTAL_HARDNESS_0_COLOR);
		normalizedRef.push_back(TOTAL_HARDNESS_25_COLOR);
		normalizedRef.push_back(TOTAL_HARDNESS_50_COLOR);
		normalizedRef.push_back(TOTAL_HARDNESS_120_COLOR);
		normalizedRef.push_back(TOTAL_HARDNESS_250_COLOR);
		normalizedRef.push_back(TOTAL_HARDNESS_425_COLOR);
		break;

	case CODE_PH:
		MAX_DISTANCE = PH_MAX_DISTANCE;
		normalizedRef.push_back(PH_62_COLOR);
		normalizedRef.push_back(PH_68_COLOR);
		normalizedRef.push_back(PH_72_COLOR);
		normalizedRef.push_back(PH_78_COLOR);
		normalizedRef.push_back(PH_84_COLOR);
		break;

	default:
		std::cout << "ERROR: INVALID OP CODE" << std::endl;
		return -1;
	}

	double min = FLT_MAX;
	size_t index = -1;

	for (size_t i = 0; i < normalizedRef.size(); i++)
	{
		double dh = std::min(abs(normalizedRef[i](0) - target(0)), 360 - abs(normalizedRef[i](0) - target(0)));
		double ds = abs(normalizedRef[i](1) - target(1));
		double dv = abs(normalizedRef[i](2) - target(2));
		double dist = sqrt(dh * dh + ds * ds + dv * dv);
		if (debug)
		{
			std::cout << "Normalized Reference: ";
			print(normalizedRef[i]);
			std::cout << std::endl;
			std::cout << "Distance: " << dist << std::endl;
		}
		if (dist < min)
		{
			min = dist;
			index = i;
		}
	}
	return abs(min) < MAX_DISTANCE ? index : -1;
}

static void normalizeColors(std::vector<cv::Vec3d>& colors, cv::Vec3d& target)
{
	std::vector<cv::Vec3d> ref;
	ref.push_back(REF_COLOR_0);
	ref.push_back(REF_COLOR_1);
	ref.push_back(REF_COLOR_2);
	ref.push_back(REF_COLOR_3);
	ref.push_back(REF_COLOR_4);
	ref.push_back(REF_COLOR_5);

	double rh = 0;
	double rs = 0;
	double rv = 0;
	double ah = 0;
	double as = 0;
	double av = 0;
	for (size_t i = 0; i < colors.size(); i++)
	{
		rh += ref[i].val[0];
		rs += ref[i].val[1];
		rv += ref[i].val[2];

		ah += colors[i].val[0];
		as += colors[i].val[1];
		av += colors[i].val[2];
	}

	double dh = (rh - ah) / 6;
	double ds = (rs - as) / 6;
	double dv = (rv - av) / 6;

	/*cout << "RAW TARGET ";
	print(target);
	cout << endl;*/

	/* Normalize target */
	target.val[0] += dh;
	target.val[1] += ds;
	target.val[2] += dv;

	/*cout << "DH: " << dh << " DS: " << ds << " DV: " << dv << endl;

	cout << "NORMALIZED: ";
	print(target);
	cout << endl;*/
}

static cv::Point2f getOrthogonalEndpoint(const cv::Point2f& p1, const cv::Point2f& p2, double length)
{
	cv::Point2f midpoint((p1.x + p2.x) / 2, (p1.y + p2.y) / 2);
	cv::Point2f point3 = getAxelPoint(midpoint, p1, -length);

	/* Case: horizontal line causes division by 0 */
	if (isnan(point3.x) || isnan(point3.y))
		return cv::Point2f(midpoint.x, midpoint.y - length);
	return point3;
}

static cv::Point2f getAxelPoint(const cv::Point2f& p1, const cv::Point2f& p2, double vectorLen)
{
	float delX = p2.x - p1.x;
	float delY = p2.y - p1.y;

	cv::Point2f p3(p1.x + delY, p1.y - delX);

	float unitVector = getDistance(p3, p1);

	p3.x = (p3.x - p1.x) / unitVector;
	p3.y = (p3.y - p1.y) / unitVector;

	p3.x = p1.x + p3.x * vectorLen;
	p3.y = p1.y + p3.y * vectorLen;

	return p3;
}

static double getRelativeVecLength(const cv::RotatedRect& target, double length)
{
	double ratio = (target.size.width + target.size.height) / 2 / REFERENCE_LENGTH;
	return length * ratio;
}

static double getRelativeArea(const cv::RotatedRect& target, double area)
{
	double ratio = (target.size.width + target.size.height) / 2 / REFERENCE_LENGTH;
	return (sqrt(area) * ratio) * (sqrt(area) * ratio);
}

static bool isNullSquare(const cv::RotatedRect& rect)
{
	return rect.size.area() == 0;
}

static double getDistance(const cv::Point2f& p1, const cv::Point2f& p2)
{
	cv::Point2f diff = p1 - p2;
	return cv::sqrt(diff.x * diff.x + diff.y * diff.y);
}

static void drawRotatedRect(cv::Mat src, const cv::RotatedRect& rect)
{
	cv::Point2f vertices[4];
	rect.points(vertices);
	for (int i = 0; i < 4; i++)
		line(src, vertices[i], vertices[(i + 1) % 4], cv::Scalar(0, 255, 0), 2);
}

static bool isInROI(cv::Point p, cv::Point2f roi[])
{
	double pro[4];
	for (int i = 0; i < 4; ++i)
		pro[i] = computeProduct(p, roi[i], roi[(i + 1) % 4]);

	if (pro[0] * pro[2] < 0 && pro[1] * pro[3] < 0) return true;

	return false;
}

static double computeProduct(cv::Point p, cv::Point2f a, cv::Point2f b)
{
	double k = (a.y - b.y) / (a.x - b.x);
	double j = a.y - k * a.x;
	return k * p.x - p.y + j;
}

static bool doesRectangleContainPoint(const cv::RotatedRect& rectangle, const cv::Point2f& point) {

	//Get the corner points.
	cv::Point2f corners[4];
	rectangle.points(corners);

	//Convert the point array to a vector.
	//https://stackoverflow.com/a/8777619/1997617
	cv::Point2f* lastItemPointer = (corners + sizeof corners / sizeof corners[0]);
	std::vector<cv::Point2f> contour(corners, lastItemPointer);

	//Check if the point is within the rectangle.
	double indicator = pointPolygonTest(contour, point, false);
	bool rectangleContainsPoint = (indicator >= 0);
	return rectangleContainsPoint;
}

void DEBUG_DRAW_TARGET(cv::Mat src) 
{
	std::cout << "DRAWING TARGET" << std::endl;
	cv::RotatedRect target = getTarget(src, false);
	if (isNullSquare(target))
	{
		std::cout << "ERROR: Target not found" << std::endl;
		return;
	}
	drawRotatedRect(src, target);
}

void DEBUG_DRAW_REFERENCE(cv::Mat src)
{
	/* Get target and return on error */
	cv::RotatedRect target = getTarget(src, true);
	if (isNullSquare(target))
	{
		std::cout << "ERROR: Target not found" << std::endl;
		return;
	}
	drawRotatedRect(src, target);

	/* Get references and return on error */
	auto ret = getReferenceSquares(src, target, true);
	std::vector<cv::RotatedRect>& references = ret.first;
	cv::RotatedRect& referenceRegion = ret.second;
	if (references.size() < NUM_ALKALINITY_REFERENCES)
	{
		std::cout << "ERROR: only " << references.size() << " reference squares found" << std::endl;
		return;
	}
	for (auto& rect : references) drawRotatedRect(src, rect);
}

void DEBUG_DRAW_SAMPLE(cv::Mat src, int OP_CODE)
{

	/* Get target and return on error */
	cv::RotatedRect target = getTarget(src, true);
	if (isNullSquare(target))
	{
		std::cout << "ERROR: Target not found" << std::endl;
		return;
	}
	drawRotatedRect(src, target);

	/* Get references and return on error */
	auto ret = getReferenceSquares(src, target, true);
	std::vector<cv::RotatedRect>& references = ret.first;
	cv::RotatedRect& referenceRegion = ret.second;
	if (references.size() < NUM_ALKALINITY_REFERENCES)
	{
		std::cout << "ERROR: only " << references.size() << " reference squares found" << std::endl;
		return;
	}
	for (auto& rect : references) drawRotatedRect(src, rect);

	/* Get sample and return on error */
	//cv::RotatedRect sample = getSampleSquare(src, target, referenceRegion, OP_CODE, true);
	//if (isNullSquare(sample))
	//{
	//	std::cout << "ERROR: could not find sample" << std::endl;
	//	return;
	//}
	//drawRotatedRect(src, sample);
}

void DEBUG_DRAW_REFERENCE_REGION(cv::Mat src)
{
	std::cout << "DRAWING REFERENCE REGION" << std::endl;
	cv::RotatedRect target = getTarget(src, true);
	if (isNullSquare(target))
	{
		std::cout << "ERROR: Target not found" << std::endl;
		return;
	}

	cv::RotatedRect referenceRegion = getReferenceRegion(src, target);
	if (isNullSquare(referenceRegion))
	{
		std::cout << "ERROR: REFERENCE REGION NOT FOUND" << std::endl;
		return;
	}
	drawRotatedRect(src, referenceRegion);
}