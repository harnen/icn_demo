// ============================================================================
// image_util.cpp
// Image utils relying on the SFML graphic library.
//
// ============================================================================
#include "image_util.h"



/*
 * load_template
 * load letter representations (template) from file; these are created by 'create_template'
 */
void load_template(vector<Letter> *letters, const int alphabet_length) {
	// read template from file
	char filename[6];
	for(int i=0; i<alphabet_length; i++) {
		sprintf(filename, "%d.sav", i);
		ifstream fin(filename);
			
		// init buffers
		int const rows = sizes[i][0];
		int const cols = sizes[i][1];
		int data[rows * cols + 3];

		// read file
		int j=0, x;
		while (fin >> x) {
			data[j++] = x;
		}

		// import letter
		Letter tmp = Letter::importLetter(data, rows, cols);
		(*letters).push_back(tmp);
		fin.close();
	}
}


/*
 * create_template
 * load an image-database file and a text file containing all letters in the alphabet;
 * and save a letter representation in a file (for each letter)
 * 
 * Note: the .sav files created should be encrypted
 */
int create_template(const char *image_alphabet, const char *text_alphabet, const int alphabet_length) {
	// load alphabet
	vector<Letter> letters;
	int ret = load_alphabet(image_alphabet, text_alphabet, alphabet_length, &letters);
	if (ret != 0) {return 1;}

	// save letters to file
	ofstream file;
	char filename[6];
	for(int i=0; i<letters.size(); i++) {
		// open file
		sprintf(filename, "%d.sav", i);
		file.open(filename);

		// export letter
		vector< vector<int> > matrix = letters[i].getMatrix();
		int data_length = matrix.size() * matrix[0].size() + 3;
		int data[data_length];
		letters[i].exportLetter(data);
		
		// save letter
		for(int i=0; i<data_length; i++) {
			file << (int)data[i] << " ";
		}
		file.close();
	}

	return 0;
}


/*
 * load_alphabet
 * load an image-database file and a text file containing all letters in the alphabet.
 */
int load_alphabet(const char *image_alphabet, const char *text_alphabet, const int alphabet_length, vector<Letter> *letters) {
	// load the databse image file - image file containing evey letters
	vector< vector<int> > pixels;
	if (load_image(image_alphabet, &pixels) != 0) {return 1;}

	// locate all the 'letters' in the image file
	*letters = find_letters(127, pixels);

	// read text alphabet
	for(int i=0; i<alphabet_length; i++) {
		char c = text_alphabet[i];
		(*letters).at(i).setLetter(c);
	}
	return 0;
}


/*
 * load_image
 * load an image into a 2d vector.
 */
int load_image(const char *image_file, vector< vector<int> > *pixels) {
	// load image
	Image input_image;
	if (!input_image.loadFromFile(image_file)) {return 1;}

	// apply the threshold to the input text pixels
	*pixels = apply_threshold(THRESHOLD, get_grayscale_pixels(input_image), 255, 0);
	return 0;
}


/*
 * get_grayscale_pixels
 * convert an image into a grayscale 2d vector.
 */
vector< vector<int> > get_grayscale_pixels(const Image& img) {
	vector< vector<int> > matrix;

	for (int x = 0; x < img.getSize().x; x++) {
		vector<int> column;
		for (int y = 0; y < img.getSize().y; y++) {
			Color c = img.getPixel(x,y);
			unsigned int shade = RED_WEIGHT * c.r + GREEN_WEIGHT * c.g + BLUE_WEIGHT * c.b;
			column.push_back(shade);
		}
		matrix.push_back(column);
	}
	return matrix;
}


/*
 * apply_threshold
 * apply a threshold to a 2d vector representing a grayscale image.
 */
vector< vector<int> > apply_threshold(const int& threshold, const vector< vector<int> > oldMatrix, int high, int low) {
	vector< vector<int> > matrix;

	// apply threshold at every point
	for (int x = 0; x < oldMatrix.size(); x++) {
		vector<int> column;
		for (int y = 0; y < oldMatrix.at(0).size(); y++) {
			unsigned int shade = oldMatrix.at(x).at(y) > threshold ? high : low;
			column.push_back(shade);
		}
		matrix.push_back(column);
	}
	return matrix;
}
