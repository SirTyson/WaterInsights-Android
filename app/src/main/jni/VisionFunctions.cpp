#include "VisionFunctions.h"
#include "VisionConstants.h"
#include <opencv2/imgproc/imgproc.hpp>
#include <iostream>

/* TARGET FINDING */
static bool acceptLinePair(cv::Vec2f line1, cv::Vec2f line2, float minTheta);
static cv::Point2f computeIntersect(cv::Vec2f line1, cv::Vec2f line2);
static std::vector<cv::Point2f> lineToPointPair(cv::Vec2f line);

/* COLOR ANALYSIS */
static cv::Vec3f getDominantColor(const cv::Mat srcImg, const cv::Rect& targetRect);
static void RGBtoHSV(float& fR, float& fG, float fB, float& fH, float& fS, float& fV);
static size_t getClosestColorIndex(std::vector<cv::Vec3f>& ref, cv::Vec3f& target, bool debug = true);
static void normalizeColors(std::vector<cv::Vec3f>& colors, cv::Vec3f& target);

/* GENERAL CORRECTION */
static float getRelativeVecLength(float sideLength, float length);
static cv::Point2f getOrthogonalEndpoint(const cv::Point2f& p1, const cv::Point2f& p2, float length);
static cv::Point2f getAxelPoint(const cv::Point2f& p1, const cv::Point2f& p2, float vectorLen);

/* HELPER FUNCTIONS */
static bool isNullSquare(const Target& target);
static float getDistance(const cv::Point2f& p1, const cv::Point2f& p2);

/* DEBUG FUNCTION */
static std::ostream& operator<<(std::ostream& os, const Target& target);
static void print(const cv::Vec3f& v);

Target getTarget(const cv::Mat srcImage, bool debug)
{
	cv::Mat occludedSquare8u;
	cv::cvtColor(srcImage, occludedSquare8u, cv::COLOR_BGR2GRAY);

	cv::Mat thresh;
	cv::threshold(occludedSquare8u, thresh, TARGET_THRESH_LOWER, TARGET_THRESH_UPPER, cv::THRESH_BINARY);

	GaussianBlur(thresh, thresh, cv::Size(TARGET_BLUR_SIZE, TARGET_BLUR_SIZE),
		TARGET_BLUR_SIGX, TARGET_BLUR_SIGY);

	cv::Mat edges;
	Canny(thresh, edges, TARGET_CANNY_THRESH_LOWER, TARGET_CANNY_THRESH_UPPER, TARGET_CANNY_AP_SIZE);


	std::vector<std::vector<cv::Point>> contours;
	cv::Mat img(edges.size(), edges.type(), cv::Scalar(0));

	static const double rectFF = TARGET_RECT_FUDGE_FACTOR;
	findContours(edges, contours, cv::RETR_CCOMP, cv::CHAIN_APPROX_NONE);
	for (size_t i = 0; i < contours.size(); i++)
	{
		cv::Scalar color = cv::Scalar(255, 105, 180);
		cv::Rect rect = boundingRect(contours[i]);
		if (rect.height > TARGET_SIDE_FILTER &&
			rect.width > TARGET_SIDE_FILTER &&
			abs(1 - (rect.width / rect.height)) < rectFF)
		{
			drawContours(img, contours, i, color, 1, 8);
		}
	}

	edges = img;

	std::vector<cv::Vec2f> lines;
	HoughLines(edges, lines, TARGET_HOUGH_RHO, CV_PI / 180,
		TARGET_HOUGH_THRESH, 0, 0);

	/* DEBUG: */
	if (debug) std::cout << "Detected " << lines.size() << " lines." << std::endl;

	/* compute the intersection from the lines detected */
	std::vector<cv::Point2f> intersections;
	for (size_t i = 0; i < lines.size(); i++)
	{
		for (size_t j = 0; j < lines.size(); j++)
		{
			cv::Vec2f line1 = lines[i];
			cv::Vec2f line2 = lines[j];
			if (acceptLinePair(line1, line2, CV_PI / 32))
			{
				cv::Point2f intersection = computeIntersect(line1, line2);
				intersections.push_back(intersection);
			}
		}
	}

	if (intersections.size() >= 4)
	{
		std::vector<cv::Point2f>::iterator i;
		cv::Point2f topLeft;
		cv::Point2f bottomRight;
		float topLeftVal = FLT_MAX;
		float bottomRightVal = 0;
		
		Target target;

		/* Get top left and bottom right of target */
		float currMin = FLT_MAX;
		float currMax = 0.0f;
		int tlIndex = -1;
		int brIndex = -1;
		for (size_t i = 0; i < intersections.size(); i++)
		{
			float sum = intersections[i].x + intersections[i].y;
			if (sum < currMin)
			{
				currMin = sum;
				tlIndex = i;
			}
			if (sum > currMax)
			{
				currMax = sum;
				brIndex = i;
			}
		}
		target.tl = intersections[tlIndex];
		target.br = intersections[brIndex];

		/* Get bottom left and top right of target */
		for (size_t i = 0; i < intersections.size(); i++)
		{
			float x = intersections[i].x;
			float y = intersections[i].y;
			if (abs(x - target.tl.x) < TARGET_POINT_DETECTION_FUDGE_FACTOR &&
				abs(y - target.br.y) < TARGET_POINT_DETECTION_FUDGE_FACTOR)
			{
				target.bl = intersections[i];
			}
			else if (abs(x - target.br.x) < TARGET_POINT_DETECTION_FUDGE_FACTOR &&
				abs(y - target.tl.y) < TARGET_POINT_DETECTION_FUDGE_FACTOR)
			{
				target.tr = intersections[i];
			}
		}
		return target;
	}

	/* Return 0 Target if not enough points found */
	return Target{
		cv::Point2f(0,0),
		cv::Point2f(0,0),
		cv::Point2f(0,0),
		cv::Point2f(0,0)
	};
}

std::vector<cv::Rect> getReferenceSquares(const cv::Mat srcImage, const Target& target, bool debug)
{
	std::vector<cv::Rect> referenceSquares;

	if (debug)
	{
		float targetArea = ((target.tr.x - target.tl.x + target.br.x - target.bl.x) / 2) * ((target.br.y - target.tr.y + target.bl.y - target.tl.y) / 2);
		std::cout << "TARGET AREA " << targetArea << std::endl;
		std::cout << "IMAGE SIZE: " << srcImage.size() << std::endl;
		std::cout << "SIDE LENGTH " << (target.tr.x - target.tl.x + target.br.x - target.bl.x) / 2 << std::endl;
	}

	for (size_t i = 0; i < NUM_PPM_REFERENCES; i++) {
		float length = getRelativeVecLength((target.tr.x - target.tl.x + target.br.x - target.bl.x) / 2,
			REF_DIST_FIRST_SQUARE + i * REF_DIST_BTWN_SQUARES);

		cv::Point2f point = getOrthogonalEndpoint(target.tl, target.tr, length);
		float size = getRelativeVecLength((target.tr.x - target.tl.x + target.br.x - target.bl.x) / 2, REF_SQUARE_SIZE);

		cv::Rect rect(
			cv::Point2f(point.x - size, point.y - size),
			cv::Point2f(point.x + size, point.y + size)
		);
		referenceSquares.push_back(rect);
	}
	return referenceSquares;
}

cv::Rect getSampleSquare(const cv::Mat srcImage, const Target& target, bool debug)
{
	cv::Point2f midpoint(
		(target.tl.x + target.br.x) / 2,
		(target.tl.y + target.br.y) / 2
	);

	float theta = SAMPLE_ANGLE * CV_PI / 180.0;
	float length = getRelativeVecLength((target.tr.x - target.tl.x + target.br.x - target.bl.x) / 2, SAMPLE_LENGTH);

	cv::Point2f samplePoint(
		midpoint.x + length * cosf(theta),
		midpoint.y + length * sinf(theta)
	);

	float size = getRelativeVecLength((target.tr.x - target.tl.x + target.br.x - target.bl.x) / 2, REF_SQUARE_SIZE);
	cv::Rect rect(
		cv::Point2f(samplePoint.x - size, samplePoint.y - size),
		cv::Point2f(samplePoint.x + size, samplePoint.y + size)
	);
	return rect;
}

int processImage(const cv::Mat src, bool debug)
{
	/* Get target and return on error */
	Target target = getTarget(src, debug);
	if (isNullSquare(target)) return -1;
	if (debug)
		cv::rectangle(src, cv::Rect(target.tl, target.br), GREEN, 2);

	/* Get references and return on error */
	std::vector<cv::Rect> references = getReferenceSquares(src, target, debug);
	if (references.size() < NUM_PPM_REFERENCES) return -1;
	if (debug)
	{
		for (auto& rect : references)
			cv::rectangle(src, rect, GREEN, 2);
	}

	/* Get sample and return on error */
	cv::Rect sample = getSampleSquare(src, target, debug);
	if (sample.area() == 0) return -1;
	if (debug)
		cv::rectangle(src, sample, GREEN, 2);

	std::vector<cv::Vec3f> refColors;
	for (auto rect : references) 
		refColors.insert(refColors.begin(), getDominantColor(src, rect));

	cv::Vec3f sampleColor = getDominantColor(src, sample);
	if (debug)
	{
		std::cout << "SAMPLE COLOR (HSV): ";
		for (size_t i = 0; i < 3; i++)
			std::cout << sampleColor[i] << " ";
		std::cout << std::endl;
	}
	
	size_t sampleIndex = getClosestColorIndex(refColors, sampleColor, debug);
	switch (sampleIndex) {
	case 0:
		return PPM_0;
		break;
	case 1:
		return PPM_1;
		break;
	case 2:
		return PPM_2;
		break;
	case 3:
		return PPM_3;
		break;
	case 4:
		return PPM_4;
		break;
	case 5:
		return PPM_5;
		break;
	default:
		return -1;
		break;
	}
}

static bool acceptLinePair(cv::Vec2f line1, cv::Vec2f line2, float minTheta)
{
	float theta1 = line1[1], theta2 = line2[1];

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
	float denom = (p1[0].x - p1[1].x) * (p2[0].y - p2[1].y) - (p1[0].y - p1[1].y) * (p2[0].x - p2[1].x);
	cv::Point2f intersect(((p1[0].x * p1[1].y - p1[0].y * p1[1].x) * (p2[0].x - p2[1].x) -
		(p1[0].x - p1[1].x) * (p2[0].x * p2[1].y - p2[0].y * p2[1].x)) / denom,
		((p1[0].x * p1[1].y - p1[0].y * p1[1].x) * (p2[0].y - p2[1].y) -
		(p1[0].y - p1[1].y) * (p2[0].x * p2[1].y - p2[0].y * p2[1].x)) / denom);

	return intersect;
}

static std::vector<cv::Point2f> lineToPointPair(cv::Vec2f line)
{
	std::vector<cv::Point2f> points;

	float r = line[0], t = line[1];
	double cos_t = cos(t), sin_t = sin(t);
	double x0 = r * cos_t, y0 = r * sin_t;
	double alpha = 1000;

	points.push_back(cv::Point2f(x0 + alpha * (-sin_t), y0 + alpha * cos_t));
	points.push_back(cv::Point2f(x0 - alpha * (-sin_t), y0 - alpha * cos_t));

	return points;
}

/// TODO: Better kmeans
static cv::Vec3f getDominantColor(const cv::Mat srcImg, const cv::Rect& targetRect) {
	/* Get target Mat */
	cv::Mat target(srcImg, targetRect);
	target.convertTo(target, CV_8UC3);

	/* convert to float & reshape to a [3 x W*H] Mat so every pixel is on a row of it's own */
	cv::Mat data;
	target.convertTo(data, CV_32F);
	data = data.reshape(1, data.total());

	/* do kmeans clustering */
	cv::Mat labels, centers;
	kmeans(data, DOMINANT_COLOR_K, labels, 
		cv::TermCriteria(cv::TermCriteria::COUNT, TERM_CRITERIA_MAX_COUNT, TERMI_CRITERIA_EPSILON),
		DOMINANT_COLOR_ATTEMPTS, cv::KMEANS_PP_CENTERS, centers);

	/* reshape both to a single row of Vec3f pixels */
	centers = centers.reshape(3, centers.rows);
	data = data.reshape(3, data.rows);

	/* replace pixel values with their center value */
	float total[3] = { 0, 0, 0 };
	cv::Vec3f* p = data.ptr<cv::Vec3f>();
	for (size_t i = 0; i < data.rows; i++) {
		int center_id = labels.at<int>(i);
		p[i] = centers.at<cv::Vec3f>(center_id);
		total[0] += p[i].val[0];
		total[1] += p[i].val[1];
		total[2] += p[i].val[2];
	}

	float r = (total[2] / data.rows) / BGR_SCALE;
	float g = (total[1] / data.rows) / BGR_SCALE;
	float b = (total[0] / data.rows) / BGR_SCALE;
	float h, s, v;
	RGBtoHSV(r, g, b, h, s, v);
	return cv::Vec3f(h, s, v);
}

static void RGBtoHSV(float& fR, float& fG, float fB, float& fH, float& fS, float& fV) {
	float fCMax = std::max(std::max(fR, fG), fB);
	float fCMin = std::min(std::min(fR, fG), fB);
	float fDelta = fCMax - fCMin;

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

static void print(const cv::Vec3f& v)
{
	std::cout << "H: " << v.val[0] << " S: " << v.val[1] << " V: " << v.val[2];
}

static size_t getClosestColorIndex(std::vector<cv::Vec3f>& ref, cv::Vec3f& target, bool debug)
{
	normalizeColors(ref, target);
	double min = FLT_MAX;
	size_t index = -1;

	std::vector<cv::Vec3f> normalizedRef;
	normalizedRef.push_back(PPM_0_COLOR);
	normalizedRef.push_back(PPM_1_COLOR);
	normalizedRef.push_back(PPM_2_COLOR);
	normalizedRef.push_back(PPM_3_COLOR);
	normalizedRef.push_back(PPM_4_COLOR);
	normalizedRef.push_back(PPM_5_COLOR);

	for (size_t i = 0; i < ref.size(); i++)
	{
		double dist = norm(normalizedRef[i] - target);
		if (debug)
		{
			std::cout << "Target: ";
			print(target);
			std::cout << std::endl;
			std::cout << "Reference: ";
			print(ref[i]);
			std::cout << std::endl;
			std::cout << "Distance: " << dist << std::endl;
		}
		if (dist < min)
		{
			min = dist;
			index = i;
		}
	}
	return index;
}

static void normalizeColors(std::vector<cv::Vec3f>& colors, cv::Vec3f& target)
{
	std::vector<cv::Vec3f> ref;
	ref.push_back(REF_COLOR_0);
	ref.push_back(REF_COLOR_1);
	ref.push_back(REF_COLOR_2);
	ref.push_back(REF_COLOR_3);
	ref.push_back(REF_COLOR_4);
	ref.push_back(REF_COLOR_5);

	float rh = 0;
	float rs = 0;
	float rv = 0;
	float ah = 0;
	float as = 0;
	float av = 0;
	for (size_t i = 0; i < colors.size(); i++)
	{
		rh += ref[i].val[0];
		rs += ref[i].val[1];
		rv += ref[i].val[2];

		ah += colors[i].val[0];
		as += colors[i].val[1];
		av += colors[i].val[2];
	}

	float dh = (rh - ah) / 6;
	float ds = (rs - as) / 6;
	float dv = (rv - av) / 6;

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

static cv::Point2f getOrthogonalEndpoint(const cv::Point2f& p1, const cv::Point2f& p2, float length)
{
	cv::Point2f midpoint((p1.x + p2.x) / 2, (p1.y + p2.y) / 2);
	cv::Point2f point3 = getAxelPoint(midpoint, p1, -length);

	/* Case: horizontal line causes division by 0 */
	if (isnan(point3.x) || isnan(point3.y))
		return cv::Point2f(midpoint.x, midpoint.y - length);
	return point3;
}

static cv::Point2f getAxelPoint(const cv::Point2f& p1, const cv::Point2f& p2, float vectorLen)
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

static float getRelativeVecLength(float sideLength, float length)
{
	static const float REF = REFERENCE_LENGTH;
	float ratio = sideLength / REF;
	return length * ratio;
}

static bool isNullSquare(const Target& target)
{
	return target.tl == cv::Point2f(0, 0) &&
		target.tr == cv::Point2f(0, 0) &&
		target.bl == cv::Point2f(0, 0) &&
		target.br == cv::Point2f(0, 0);
}

static float getDistance(const cv::Point2f& p1, const cv::Point2f& p2)
{
	float centerX = p2.x - p1.x;
	float centerY = p2.y - p1.y;
	float length = sqrt(static_cast<float>(centerX * centerX + centerY + centerY));
	return length;
}

/*
 * DEBUG ONLY, makes Target object printable in streams
*/
static std::ostream& operator<<(std::ostream& os, const Target& target)
{
	return os << "TOP LEFT: " << target.tl << "\n"
		<< "TOP RIGHT: " << target.tr << "\n"
		<< "BOTTOM LEFT: " << target.bl << "\n"
		<< "BOTTOM RIGHT: " << target.br << "\n";
}

void DEBUG_DRAW_TARGET(cv::Mat src) 
{
	std::cout << "DRAWING TARGET" << std::endl;
	Target target = getTarget(src, false);
	if (isNullSquare(target))
	{
		std::cout << "ERROR: Target not found" << std::endl;
		return;
	}
	cv::Rect rect(target.tl, target.br);
	cv::rectangle(src, rect, GREEN, 2);
}

void DEBUG_DRAW_REFERENCE(cv::Mat src)
{
	/* Get target and return on error */
	Target target = getTarget(src, true);
	if (isNullSquare(target))
	{
		std::cout << "ERROR: Target not found" << std::endl;
		return;
	}
	cv::rectangle(src, cv::Rect(target.tl, target.br), GREEN, 2);

	/* Get references and return on error */
	std::vector<cv::Rect> references = getReferenceSquares(src, target, true);
	if (references.size() < NUM_PPM_REFERENCES)
	{
		std::cout << "ERROR: only " << references.size() << " reference squares found" << std::endl;
		return;
	}
	for (auto& rect : references)
		cv::rectangle(src, rect, GREEN, 2);
}

void DEBUG_DRAW_SAMPLE(cv::Mat src)
{
	/* Get target and return on error */
	Target target = getTarget(src, true);
	if (isNullSquare(target))
	{
		std::cout << "ERROR: Target not found" << std::endl;
		return;
	}
	cv::rectangle(src, cv::Rect(target.tl, target.br), GREEN, 2);

	/* Get references and return on error */
	std::vector<cv::Rect> references = getReferenceSquares(src, target, true);
	if (references.size() < NUM_PPM_REFERENCES)
	{
		std::cout << "ERROR: only " << references.size() << " reference squares found" << std::endl;
		return;
	}
	for (auto& rect : references)
		cv::rectangle(src, rect, GREEN, 2);

	/* Get sample and return on error */
	cv::Rect sample = getSampleSquare(src, target, true);
	if (sample.area() == 0)
	{
		std::cout << "ERROR: could not find sample" << std::endl;
		return;
	}
	cv::rectangle(src, sample, GREEN, 2);
}