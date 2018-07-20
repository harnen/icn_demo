// ============================================================================
// processing.cpp
// Image processing / OCR functions.
//
// ============================================================================
#include "processing.h"


/*
 * character_recognition
 * perform OCR on an input image.
 * Note: the image is input as a int** double pointer for edger8r compilation.
 */
void character_recognition(int** input, int rows, int cols, int** letters_c, int letters_rows, int letters_cols, 
	char *output_letters, int *length) {

	// convert input to vector
	vector< vector<int> > input_image;
	input_image.resize(rows, vector<int>(cols, 0));
	for (int i = 0; i < rows; ++i) {
    	for (int j = 0; j < cols; ++j) {
        	input_image[i][j] = input[i][j];
        }
    }

    // convert letters C-type to object
    vector< vector<int> > letters_vec;
	letters_vec.resize(letters_rows, vector<int>(letters_cols, 0));
	for (int i = 0; i < letters_rows; ++i) {
    	for (int j = 0; j < letters_cols; ++j) {
        	letters_vec[i][j] = letters_c[i][j];
        }
    }
    vector<Letter> letters;
    for(int i=0; i<letters_rows; i++) {
		// export letter i
		int *data = letters_vec[i].data();
		Letter tmp = Letter::importLetter(data, sizes[i][0], sizes[i][1]);
		letters.push_back(tmp);
	}

	// find letters in image
	vector<Letter> possible_letters = find_letters(127, input_image);

	// set number of recognised letters
	*length = possible_letters.size();

	// find the best match in the alphabet for each letter
	for (int i = 0; i < possible_letters.size(); i++) {
		Letter match = letters.at(0); 
		match_letter(possible_letters.at(i), letters, &match);
		output_letters[i] = match.getLetter();
	}
}


/*
 * find_letters
 * find all letters in a 2d vector image.
 */
vector<Letter> find_letters(const int& threshold, const vector< vector<int> > matrix) {
	vector<Letter> letters;

	// initialize impossible base values
	int leftEdge = -1;
	int topEdge = -1;
	int bottomEdge = -1;

	for (unsigned int x = 0; x < matrix.size(); x++) {
		// determin whether a pixel was found in this column
		bool found = false;

		for (unsigned int y = 0; y < matrix.at(0).size(); y++) {
			// look for the top and bottom edges of the shape
			if (matrix.at(x).at(y) < threshold) {
				if (topEdge < 0 || y < topEdge) {topEdge = y;}
				found = true;
			} 
			else {
				if (y>0&&matrix.at(x).at(y-1)<threshold&&(bottomEdge < 0 || y > bottomEdge)) {
					bottomEdge = y;
				}
			}
		}

		// look for the left edge
		if (found) {
			if (leftEdge < 0) {leftEdge = x;}
		} 
		else {
			if (leftEdge > 0) {
				// we've found the right edge, so we found a shape
				vector< vector<int> > subMatrix;

				// copy the shape to a new matrix
				for (int subx = leftEdge; subx < x; subx++) {
					vector<int> column;
					for (int suby = topEdge; suby < bottomEdge; suby++) {
						column.push_back(matrix.at(subx).at(suby));
					}
					subMatrix.push_back(column);
				}

				// initialize a letter of the matrix
				Letter letter(subMatrix);
				letter.setX(leftEdge);
				letter.setY(topEdge);
				letters.push_back(letter);

				// set values back to original state to search for more shapes
				leftEdge = -1;
				topEdge = -1;
				bottomEdge = -1;
			}
		}
	}
	return letters;
}


/*
 * match_letter
 * search for the better matching letter.
 */
void match_letter(const Letter to_match, const vector<Letter> letters, Letter *best_match) {
	// init the lowest known difference in letters
	double lowest_difference = 256;
	int lowest_difference_index = -1;

	// loop over each letter
	for (int j = 0; j < letters.size(); j++) {
		// scale the template letter to match the input letter then compare the matrices
		double difference = compare_matrices(
			to_match.getMatrix(),
			scale_Matrix_to(
				to_match.getMatrix().size(), 
				to_match.getMatrix().at(0).size(), 
				letters.at(j).getMatrix()
			)
		);

		// update lowest difference value and the index
		if (difference < lowest_difference) {
			lowest_difference = difference;
			lowest_difference_index = j;
		}
	}

	// get the letter with the lowest difference value
	*best_match = letters.at(lowest_difference_index);
}


/*
 * scale_Matrix_to
 * scale a 2d vector matrix A to match the size of a matrix B.
 */
vector< vector<int> > scale_Matrix_to(int width, int height, vector< vector<int> > oldMatrix) {
	// init a matrix
	vector< vector<int> > matrix;

	// compute scale factors
	double wScaler = (double) oldMatrix.size() / (double) width;
	double hScaler = (double) oldMatrix.at(0).size() / (double) height;

	// fill matrix
	for (double x = 0; x < oldMatrix.size(); x+=wScaler) {
		vector<int> column;
		for (double y = 0; y < oldMatrix.at(0).size(); y+=hScaler) {
			column.push_back(oldMatrix.at(x).at(y));
		}
		matrix.push_back(column);
	}
	return matrix;
}


/*
 * compare_matrices
 * return the distance between two matrices A and B.
 */
double compare_matrices(vector< vector<int> > matrixA, vector< vector<int> > matrixB) {
	int differenceSum = 0;

	// compute differences betwieen matrices
	for (int x = 0; x < matrixA.size(); x++) {
		for (int y = 0; y < matrixA.at(0).size(); y++) {
			if (matrixA.at(x).at(y)-matrixB.at(x).at(y) > 0) {
				differenceSum += matrixA.at(x).at(y) - matrixB.at(x).at(y);
			}
			else {
				differenceSum += matrixB.at(x).at(y) - matrixA.at(x).at(y);
			}
		}
	}

	// return the average difference between matrices A and B
	return (double) differenceSum / (double) (matrixA.size()*matrixA.at(0).size());
}







