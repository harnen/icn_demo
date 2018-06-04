// ============================================================================
// processing.h
// Image processing / OCR functions.
//
// ============================================================================
#ifndef PROCESSING_H_
#define PROCESSING_H_

#include <vector>
#include "Letter.h"

using namespace std;


// store letters matrix sizes
// ugly -- should stay in a file create by 'create_template'
// also used by image_util.h
const int sizes[26][2] = {
	{62, 79}, {48, 79}, {51, 81}, {49, 79}, {41, 79}, {42, 79}, {52, 81}, {45, 79}, {36, 79},
	{39, 80}, {55, 79}, {43, 79}, {50, 79}, {47, 79}, {54, 81}, {45, 79}, {56, 94}, {53, 79}, 
	{44, 81}, {57, 79}, {45, 80}, {62, 79}, {63, 79}, {60, 79}, {58, 79}, {48, 79}
};



/*
 * character_recognition
 * perform OCR on an input image.
 * Note: the image is input as a int** double pointer for edger8r compilation.
 */
void character_recognition(int** input, int rows, int cols, int** letters_c, int letters_rows, int letters_col, 
	char *output_letters, int *length);

/*
 * find_letters
 * find all letters in a 2d vector image.
 */
vector<Letter> find_letters(const int& threshold, const vector< vector<int> > matrix);

/*
 * match_letter
 * search for the better matching letter.
 */
void match_letter(const Letter to_match, const vector<Letter> letters, Letter *best_match);

/*
 * scale_Matrix_to
 * scale a 2d vector matrix A to match the size of a matrix B.
 */
vector< vector<int> > scale_Matrix_to(int width, int height, vector< vector<int> > oldMatrix);

/*
 * compare_matrices
 * return the distance between two matrices A and B.
 */
double compare_matrices(vector< vector<int> > matrixA, vector< vector<int> > matrixB);



#endif // PROCESSING_H_