// ============================================================================
// Letter.h
// Represents a matrix that is an image of a letter.
//
// ============================================================================
#include "Letter.h"
#include <stdio.h>


namespace std {
	void __throw_length_error(char const*) {}
	void __throw_bad_alloc() {}
}


// getters and setters
char Letter::getLetter() const {
	return letter;
}
void Letter::setLetter(char letter) {
	this->letter = letter;
}
int Letter::getX() const {
	return x;
}
void Letter::setX(int x) {
	this->x = x;
}
int Letter::getY() const {
	return y;
}
void Letter::setY(int y) {
	this->y = y;
}
const vector< vector<int> >& Letter::getMatrix() const {
	return matrix;
}
void Letter::setMatrix(const vector< vector<int> >& matrix) {
	this->matrix = matrix;
}

// initialize a new letter object
Letter::Letter(vector< vector<int> > matrix) {
	this->matrix=matrix;
	this->x=0;
	this->y=0;
	this->letter='*';
}

// import / export
void Letter::exportLetter(int data[]) {
	// allocate array
	//data[this->matrix.size() + this->matrix[0].size() + 3];

	// export matrix
	int rows = matrix.size();
	int cols = matrix[0].size();
	for (int i=0; i < rows; i++) {
		for (int j=0; j < cols; j++) {
			data[cols*i+j] = matrix[i][j];
		}
	}

	// export x,y, char
	data[matrix.size() * matrix[0].size() + 0] = x;
	data[matrix.size() * matrix[0].size() + 1] = y;
	data[matrix.size() * matrix[0].size() + 2] = (int) letter;
}

Letter Letter::importLetter(const int data[], const int rows, const int cols) {
	// set matrix
	vector< vector<int> > new_matrix;
	new_matrix.resize(rows, vector<int>(cols, 0));
	for (int i=0; i < rows; i++) {
		for (int j=0; j < cols; j++) {
			new_matrix[i][j] = data[cols*i+j];
		}
	}
	Letter letter(new_matrix);

	// import x,y, char
	letter.setX(data[rows * cols + 0]);
	letter.setY(data[rows * cols + 1]);
	letter.setLetter((char) data[rows * cols + 2]);

	return letter;
}




