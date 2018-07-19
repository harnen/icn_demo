// ============================================================================
// Letter.h
// Represents a matrix that is an image of a letter.
//
// ============================================================================
#ifndef LETTER_H_
#define LETTER_H_

#include <vector>

using namespace std;


/**
 * The Letter class represents a matrix that is an image of a letter.
 * It store the location where it was found and the matrix of the letter
 * as well as the char value of the letter.
 *
 * the location and char value are not always known, so x and y default to 0
 * while letter defaults to *.
 */
class Letter {
private:
	/**
	 * The char ascii value of the character represented by this letter class.  Defaults to * if
	 * the character is unknown (for instance, in the case of unscanned letters)
	 */
	char letter;

	/**
	 * A matrix containing
	 */
	vector< vector<int> > matrix;

	/**
	 * The x and y coordinates in the parent matrix of where the matrix was found
	 */
	int x;
	int y;

public:
	//Initialize a new letter from a matrix
	Letter(vector< vector<int> > matrix);

	//Getters and setters
	char getLetter() const;
	void setLetter(char letter);
	const vector<vector<int> >& getMatrix() const;
	void setMatrix(const vector<vector<int> >& matrix);

	int getX() const;
	void setX(int x);
	int getY() const;
	void setY(int y);

	void exportLetter(int data[]);
	static Letter importLetter(const int data[], const int rows, const int cols);
};

#endif /* LETTER_H_ */
