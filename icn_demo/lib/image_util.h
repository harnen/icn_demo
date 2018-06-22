// ============================================================================
// image_util.h
// Image utils relying on the SFML graphic library.
//
// ============================================================================
#ifndef IMAPGEUTIL_H_
#define IMAPGEUTIL_H_

#include <vector>
#include <fstream>
#include <iostream>
#include <SFML/Graphics.hpp>
#include "processing.h"
#include "Letter.h"

using namespace std;
using namespace sf;


// ============================================================================
// constants
// ============================================================================
const int THRESHOLD = 90;
const double RED_WEIGHT = 0.299;
const double GREEN_WEIGHT = 0.587;
const double BLUE_WEIGHT = 0.114;



/*
 * load_template
 * load letter representations (template) from file; these are created by 'create_template'
 */
void load_template(vector<Letter> *letters, const int alphabet_length);

/*
 * create_template
 * load an image-database file and a text file containing all letters in the alphabet;
 * and save a letter representation in a file (for each letter)
 */
int create_template(const char *image_alphabet, const char *text_alphabet, const int alphabet_length);

/*
 * load_alphabet
 * load an image-database file and a text file containing all letters in the alphabet.
 */
int load_alphabet(const char *image_alphabet, const char *text_alphabet, const int alphabet_length, vector<Letter> *letters);

/*
 * load_image
 * load an image into a 2d vector.
 */
int load_image(const char *image_file, vector< vector<int> > *pixels);

/*
 * get_grayscale_pixels
 * convert an image into a grayscale 2d vector.
 */
vector< vector<int> > get_grayscale_pixels(const Image& img);

/*
 * apply_threshold
 * apply a threshold to a 2d vector representing a grayscale image.
 */
vector< vector<int> > apply_threshold(const int& threshold, const vector< vector<int> > oldMatrix, int high, int low);



#endif // IMAPGEUTIL_H_

