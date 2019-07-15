#include <opencv2/core/core.hpp>
#include <opencv2/highgui/highgui.hpp>
#include <opencv2/imgproc/imgproc.hpp>
#include <iostream>
#include <vector>
#include <cfloat>
#include <insights_water_waterinsightsv005_CvUtil.h>

static const bool DEBUG_MODE = true;

using namespace cv;
using namespace std;

struct Target {
	Point2f tl;
	Point2f tr;
	Point2f bl;
	Point2f br;
};

/* DEBUG */
/* Make Target Printable */
std::ostream& operator<<(std::ostream& os, const Target& target)
{
	return os << "TOP LEFT: " << target.tl << "\n"
		<< "TOP RIGHT: " << target.tr << "\n"
		<< "BOTTOM LEFT: " << target.bl << "\n"
		<< "BOTTOM RIGHT: " << target.br << "\n";
}

/* Get Target */
Target getTarget(/*const*/ Mat srcImage, bool debug = false);
Point2f computeIntersect(Vec2f line1, Vec2f line2);
vector<Point2f> lineToPointPair(Vec2f line);
bool acceptLinePair(Vec2f line1, Vec2f line2, float minTheta);
void removeDuplicatePoints(vector<Point2f>& vec);

/* Get Reference Squares */
vector<Rect> getReferenceSquares(const Mat srcImage, const Target target);
float getDistance(const Point2f& p1, const Point2f& p2);
Point2f getAxelPoint(const Point2f& p1, const Point2f& p2, float vectorLen);
Point2f getOrthogonalEndpoint(const Point2f& p1, const Point2f& p2, float length);
float getRelativeVecLength(float sideLength, float length);
float getRelativeArea(float targetArea, float area);

/* Get Sample Square */
Rect getSampleSquare(/*const*/ Mat srcImage, const Target& target);

/* Get dominant color of region */
Vec3f getDominantColor(/*const*/ Mat srcImg, const Rect& targetRect);
void RGBtoHSV(float& fR, float& fG, float fB, float& fH, float& fS, float& fV);

/* Parse Color */
size_t getClosestColorIndex(vector<Vec3f>& ref, Vec3f& target);
void normalizeColors(vector<Vec3f>& colors, Vec3f& target);
int processImage(Mat* srcPtr);

/* Debug Functions */
void DEBUG_DRAW_TARGET(Mat* src);
//void DEBUG_DRAW_REFERENCE(Mat* src);
//void DEBUG_DRAW_SAMPLE(Mat* src);

JNIEXPORT jint JNICALL Java_insights_water_waterinsightsv005_CvUtil_processImage
  (JNIEnv * env, jclass cls, jlong image_addr)
{
    Mat* image = (Mat*) image_addr;
    return processImage(image);
}

JNIEXPORT void JNICALL Java_insights_water_waterinsightsv005_CvUtil_DEBUG_1DRAW_1REFERENCE
  (JNIEnv * env, jclass cls, jlong image_addr)
{
    Mat* image = (Mat*) image_addr;
    //DEBUG_DRAW_REFERENCE(image);
}

JNIEXPORT void JNICALL Java_insights_water_waterinsightsv005_CvUtil_DEBUG_1DRAW_1SAMPLE
  (JNIEnv * env, jclass cls, jlong image_addr)
{
    Mat* image = (Mat*) image_addr;
    //DEBUG_DRAW_SAMPLE(image);
}

JNIEXPORT void JNICALL Java_insights_water_waterinsightsv005_CvUtil_DEBUG_1DRAW_1TARGET
  (JNIEnv * env, jclass cls, jlong image_addr)
{
    Mat* image = (Mat*) image_addr;
    DEBUG_DRAW_TARGET(image);
}

void DEBUG_DRAW_TARGET(Mat* src)
{
	cout << "DRAWING TARGET" << endl;
	resize(*src, *src, Size(0, 0), 0.25, 0.25);
	Target target = getTarget(*src, true);
}

int processImage(Mat* src)
{
	if (DEBUG_MODE)
		cout << "IMAGE BEING PROCESS" << endl;

	resize(*src, *src, Size(0, 0), 0.25, 0.25);
	Target target = getTarget(*src);

	if (target.tl == Point2f(0, 0) && target.tr == Point2f(0, 0) && Point2f(0, 0) == target.bl && target.br == Point2f(0, 0)) return -1;

	//rectangle(src, Rect(target.tl, target.br), Scalar(0, 0, 255), 2);

	vector<Rect> references = getReferenceSquares(*src, target);
	Rect sample = getSampleSquare(*src, target);

	vector<Vec3f> refColors;
	for (auto rect : references) {
		refColors.insert(refColors.begin(), getDominantColor(*src, rect));
		rectangle(*src, rect, Scalar(0, 255, 0), 2);
	}

	//for (auto color : refColors)
	//{
	//	//cout << "COLOR: ";
	//	for (int i = 0; i < 3; i++) {
	//	//	cout << color.val[i] << "  ";
	//	}
	//	//cout << endl;
	//}

	Vec3f sampleColor = getDominantColor(*src, sample);
	rectangle(*src, sample, Scalar(0, 255, 0), 2);

	size_t sampleIndex = getClosestColorIndex(refColors, sampleColor);
	switch (sampleIndex) {
	case 0:
		return 0;
		break;
	case 1:
		return 40;
		break;
	case 2:
		return 80;
		break;
	case 3:
		return 120;
		break;
	case 4:
		return 180;
		break;
	case 5:
		return 240;
		break;
	default:
		return -1;
		break;
	}

}

/**
 * Given a srcImage, function returns Target struct containing 4 points defining target. Returns 0 Target if
 * at least 4 target corners not found.
*/
Target getTarget(/*const*/ Mat srcImage, bool debug) {
	Mat occludedSquare8u;
	cvtColor(srcImage, occludedSquare8u, COLOR_BGR2GRAY);

	Mat thresh;
	threshold(occludedSquare8u, thresh, 210.0, 255.0, THRESH_BINARY);

	GaussianBlur(thresh, thresh, Size(7, 7), 2.0, 2.0);

	Mat edges;
	Canny(thresh, edges, 66.0, 133.0, 3);

	vector<vector<Point> > contours;
	Mat img(edges.size(), edges.type(), Scalar(0));

	/* Rectangle Fudge Factor */
	static const double rectFF = 0.2;
	static const int w_thresh = 50;
	static const int h_thresh = 50;

	findContours(edges, contours, RETR_CCOMP, CHAIN_APPROX_NONE);
	for (size_t i = 0; i < contours.size(); i++)
	{
		Scalar color = Scalar(255, 105, 180);
		Rect rect = boundingRect(contours[i]);
		if (rect.height > h_thresh &&
			rect.width > w_thresh &&
			abs(1 - (rect.width / rect.height)) < rectFF)
		{
			drawContours(img, contours, i, color, 1, 8);
		}
	}

	edges = img;

	vector<Vec2f> lines;
	HoughLines(edges, lines, 1, CV_PI / 180, 50, 0, 0);

	/* DEBUG: */
	if (debug) cout << "Detected " << lines.size() << " lines." << endl;

	// compute the intersection from the lines detected
	vector<Point2f> intersections;
	for (size_t i = 0; i < lines.size(); i++)
	{
		for (size_t j = 0; j < lines.size(); j++)
		{
			Vec2f line1 = lines[i];
			Vec2f line2 = lines[j];
			if (acceptLinePair(line1, line2, CV_PI / 32))
			{
				Point2f intersection = computeIntersect(line1, line2);
				intersections.push_back(intersection);
				if (debug) circle(srcImage, intersection, 2, Scalar(0, 255, 0));
			}
		}

	}

	if (intersections.size() >= 4)
	{
		//removeDuplicatePoints(intersections);
		vector<Point2f>::iterator i;
		Point2f topLeft;
		Point2f bottomRight;
		float topLeftVal = FLT_MAX;
		float bottomRightVal = 0;
		/* DEBUG */
		/*for (i = intersections.begin(); i != intersections.end(); ++i)
		{
			cout << "Intersection is " << i->x << ", " << i->y << endl;
			circle(srcImage, *i, 1, Scalar(0, 255, 0), 3);
		}*/

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
		const static float FUDGE_FACTOR = 10;
		for (size_t i = 0; i < intersections.size(); i++)
		{
			float x = intersections[i].x;
			float y = intersections[i].y;
			if (abs(x - target.tl.x) < FUDGE_FACTOR && abs(y - target.br.y) < FUDGE_FACTOR)
				target.bl = intersections[i];
			else if (abs(x - target.br.x) < FUDGE_FACTOR && abs(y - target.tl.y) < FUDGE_FACTOR)
				target.tr = intersections[i];
		}
		return target;
	}

	/* Return 0 Target if not enough points found */
	return Target{
		Point2f(0,0),
		Point2f(0,0),
		Point2f(0,0),
		Point2f(0,0)
	};
}

bool acceptLinePair(Vec2f line1, Vec2f line2, float minTheta)
{
	float theta1 = line1[1], theta2 = line2[1];

	if (theta1 < minTheta)
	{
		theta1 += CV_PI; // dealing with 0 and 180 ambiguities
	}

	if (theta2 < minTheta)
	{
		theta2 += CV_PI; // dealing with 0 and 180 ambiguities
	}

	return abs(theta1 - theta2) > minTheta;
}

// wikipedia line-intersection equation
Point2f computeIntersect(Vec2f line1, Vec2f line2)
{
	vector<Point2f> p1 = lineToPointPair(line1);
	vector<Point2f> p2 = lineToPointPair(line2);

	float denom = (p1[0].x - p1[1].x) * (p2[0].y - p2[1].y) - (p1[0].y - p1[1].y) * (p2[0].x - p2[1].x);
	Point2f intersect(((p1[0].x * p1[1].y - p1[0].y * p1[1].x) * (p2[0].x - p2[1].x) -
		(p1[0].x - p1[1].x) * (p2[0].x * p2[1].y - p2[0].y * p2[1].x)) / denom,
		((p1[0].x * p1[1].y - p1[0].y * p1[1].x) * (p2[0].y - p2[1].y) -
		(p1[0].y - p1[1].y) * (p2[0].x * p2[1].y - p2[0].y * p2[1].x)) / denom);

	return intersect;
}

vector<Point2f> lineToPointPair(Vec2f line)
{
	vector<Point2f> points;

	float r = line[0], t = line[1];
	double cos_t = cos(t), sin_t = sin(t);
	double x0 = r * cos_t, y0 = r * sin_t;
	double alpha = 1000;

	points.push_back(Point2f(x0 + alpha * (-sin_t), y0 + alpha * cos_t));
	points.push_back(Point2f(x0 - alpha * (-sin_t), y0 - alpha * cos_t));

	return points;
}

/* Point fudge factor for judging if 2 points should be considered the same point */
static const float POINT_FF = 1.0f;
void removeDuplicatePoints(vector<Point2f>& vec)
{
	/// TOOD: Make this faster with a hashset implimentation
	for (size_t i = 0; i < vec.size(); i++) {
		for (size_t j = 0; j < vec.size(); j++) {
			if (j == i) continue;
			if (abs(vec[i].x - vec[j].x) < POINT_FF && abs(vec[i].y - vec[j].y) < POINT_FF) {
				vec.erase(vec.begin() + j);
				if (j == vec.size()) break;
			}
		}
		if (i == vec.size()) break;
	}
}

static const float LENGTH_FIRST_SQUARE = 41.5;
static const float SQUARE_DISTANCE = 27.3;
static const size_t NUM_REFERENCE_SQUARES = 6;
static const float REFERENCE_SQUARE_SIZE = 9.5;
vector<Rect> getReferenceSquares(const Mat srcImage, const Target target)
{
	vector<Rect> referenceSquares;

	/* DEBUG */
	//line(srcImage, target.tl, target.tr, Scalar(0, 255, 0), 2);
	/*float targetArea = ((target.tr.x - target.tl.x + target.br.x - target.bl.x) / 2) * ((target.br.y - target.tr.y + target.bl.y - target.tl.y) / 2);
	cout << "TARGET AREA " << targetArea << endl;
	cout << "IMAGE SIZE: " << srcImage.size() << endl;
	cout << "SIDE LENGTH " << (target.tr.x - target.tl.x + target.br.x - target.bl.x) / 2 << endl;*/

	for (size_t i = 0; i < NUM_REFERENCE_SQUARES; i++) {
		float length = getRelativeVecLength((target.tr.x - target.tl.x + target.br.x - target.bl.x) / 2, LENGTH_FIRST_SQUARE + i * SQUARE_DISTANCE);
		Point2f point = getOrthogonalEndpoint(target.tl, target.tr, length);
		float size = getRelativeVecLength((target.tr.x - target.tl.x + target.br.x - target.bl.x) / 2,REFERENCE_SQUARE_SIZE);
		Rect rect(
			Point2f(point.x - size, point.y - size),
			Point2f(point.x + size, point.y + size)
		);
		referenceSquares.push_back(rect);
	}
	return referenceSquares;
}

/* Returns distance between two points */
float getDistance(const Point2f& p1, const Point2f& p2)
{
	float centerX = p2.x - p1.x;
	float centerY = p2.y - p1.y;
	float length = sqrt(static_cast<float>(centerX * centerX + centerY + centerY));
	return length;
}

Point2f getAxelPoint(const Point2f& p1, const Point2f& p2, float vectorLen)
{
	float delX = p2.x - p1.x;
	float delY = p2.y - p1.y;

	Point2f p3(p1.x + delY, p1.y - delX);

	float unitVector = getDistance(p3, p1);

	p3.x = (p3.x - p1.x) / unitVector;
	p3.y = (p3.y - p1.y) / unitVector;

	p3.x = p1.x + p3.x * vectorLen;
	p3.y = p1.y + p3.y * vectorLen;

	return p3;
}

Point2f getOrthogonalEndpoint(const Point2f& p1, const Point2f& p2, float length)
{
	Point2f midpoint((p1.x + p2.x) / 2, (p1.y + p2.y) / 2);
	Point2f point3 = getAxelPoint(midpoint, p1, -length);
	/* Case: horizontal line causes division by 0*/
	if (isnan(point3.x) || isnan(point3.y))
		return Point2f(midpoint.x, midpoint.y - length);
	return point3;
}

const static float REFERENCE_LENGTH = 75;
float getRelativeVecLength(float sideLength, float length)
{
	float ratio = sideLength / REFERENCE_LENGTH;
	return length * ratio;
}

/// TODO: Actually impliment
float getRelativeArea(float targetArea, float area)
{
	return area;
}

static const float ANGLE = 15.5;
static const float SAMPLE_LENGTH = 134.0;
Rect getSampleSquare(/*const*/ Mat srcImage, const Target& target)
{
	Point2f midpoint(
		(target.tl.x + target.br.x) / 2,
		(target.tl.y + target.br.y) / 2
	);

	//circle(srcImage, midpoint, 1, Scalar(0, 255, 0), 2);

	float theta = ANGLE * CV_PI / 180.0;
	float length = getRelativeVecLength((target.tr.x - target.tl.x + target.br.x - target.bl.x) / 2, SAMPLE_LENGTH);

	Point2f samplePoint(
		midpoint.x + length * cosf(theta),
		midpoint.y + length * sinf(theta)
	);

	float size = getRelativeVecLength((target.tr.x - target.tl.x + target.br.x - target.bl.x) / 2, REFERENCE_SQUARE_SIZE);
	Rect rect(
		Point2f(samplePoint.x - size, samplePoint.y - size),
		Point2f(samplePoint.x + size, samplePoint.y + size)
	);

	//line(srcImage, midpoint, samplePoint, Scalar(0, 255, 0), 2);
	//circle(srcImage, samplePoint, 1, Scalar(0, 0, 255), 2);
	//rectangle(srcImage, rect, Scalar(0, 255, 0), 1);

	return rect;
}

/// TODO: Better kmeans
Vec3f getDominantColor(/*const*/ Mat srcImg, const Rect& targetRect) {
	/* Get target Mat */
	Mat target(srcImg, targetRect);
	target.convertTo(target, CV_8UC3);

	// convert to float & reshape to a [3 x W*H] Mat so every pixel is on a row of it's own
	Mat data;
	target.convertTo(data, CV_32F);
	data = data.reshape(1, data.total());

	// do kmeans clustering
	Mat labels, centers;
	kmeans(data, 8, labels, TermCriteria(TermCriteria::COUNT, 10, 1.0), 3,
		KMEANS_PP_CENTERS, centers);

	// reshape both to a single row of Vec3f pixels:
	centers = centers.reshape(3, centers.rows);
	data = data.reshape(3, data.rows);

	// replace pixel values with their center value:
	float total[3] = { 0, 0, 0 };
	Vec3f* p = data.ptr<Vec3f>();
	for (size_t i = 0; i < data.rows; i++) {
		int center_id = labels.at<int>(i);
		p[i] = centers.at<Vec3f>(center_id);
		total[0] += p[i].val[0];
		total[1] += p[i].val[1];
		total[2] += p[i].val[2];
	}

	float r = (total[2] / data.rows) / 255;
	float g = (total[1] / data.rows) / 255;
	float b = (total[0] / data.rows) / 255;
	float h, s, v;
	RGBtoHSV(r, g, b, h, s, v);
	return Vec3f(h, s, v);
}

/// TODO: Clean up
void RGBtoHSV(float& fR, float& fG, float fB, float& fH, float& fS, float& fV) {
	float fCMax = max(max(fR, fG), fB);
	float fCMin = min(min(fR, fG), fB);
	float fDelta = fCMax - fCMin;

	if (fDelta > 0) {
		if (fCMax == fR) {
			fH = 60 * (fmod(((fG - fB) / fDelta), 6));
		}
		else if (fCMax == fG) {
			fH = 60 * (((fB - fR) / fDelta) + 2);
		}
		else if (fCMax == fB) {
			fH = 60 * (((fR - fG) / fDelta) + 4);
		}

		if (fCMax > 0) {
			fS = fDelta / fCMax;
		}
		else {
			fS = 0;
		}

		fV = fCMax;
	}
	else {
		fH = 0;
		fS = 0;
		fV = fCMax;
	}

	if (fH < 0) {
		fH = 360 + fH;
	}
}

void print(Vec3f v)
{
	//cout << "H: " << v.val[0] << " S: " << v.val[1] << " V: " << v.val[2];
}

size_t getClosestColorIndex(vector<Vec3f>& ref, Vec3f& target)
{
	normalizeColors(ref, target);
	double min = FLT_MAX;
	size_t index = -1;

	vector<Vec3f> normalizedRef;
	normalizedRef.push_back(Vec3f(48.9638, 0.678046, 0.794535));
	normalizedRef.push_back(Vec3f(63.1896, 0.358716, 0.607069));
	normalizedRef.push_back(Vec3f(88.0652, 0.171686, 0.486555));
	normalizedRef.push_back(Vec3f(168.906, 0.241388, 0.458003));
	normalizedRef.push_back(Vec3f(189.37, 0.20602, 0.477649));
	normalizedRef.push_back(Vec3f(200.559, 0.208543, 0.395491));

	for (size_t i = 0; i < ref.size(); i++)
	{
		double dist = norm(normalizedRef[i] - target);
		/*cout << "Target: ";
		print(target);
		cout << endl;
		cout << "Reference: ";
		print(ref[i]);
		cout << endl;
		cout << "Distance: " << dist << endl;*/
		if (dist < min)
		{
			min = dist;
			index = i;
		}
	}
	return index;
}

void normalizeColors(vector<Vec3f>& colors, Vec3f& target)
{
	vector<Vec3f> ref;
	ref.push_back(Vec3f(38.888, 0.65106, 0.93289));
	ref.push_back(Vec3f(37.7765, 0.573698, 0.840704));
	ref.push_back(Vec3f(37.4427, 0.537292, 0.71138));
	ref.push_back(Vec3f(35.6967, 0.402677, 0.545533));
	ref.push_back(Vec3f(33.1313, 0.304777, 0.468166));
	ref.push_back(Vec3f(26.2569, 0.198088, 0.413656));

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

	// Normalize target
	target.val[0] += dh;
	target.val[1] += ds;
	target.val[2] += dv;

	/*cout << "DH: " << dh << " DS: " << ds << " DV: " << dv << endl;

	cout << "NORMALIZED: ";
	print(target);
	cout << endl;*/
}