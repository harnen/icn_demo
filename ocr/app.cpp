#include <stdio.h>
#include <iostream>
#include <string>
#include <vector>

#include "Letter.h"
#include "image_util.h"
#include "processing.h"
#include "ocr.h"


//using namespace std;


/* 
 * Main entry point
 */
int main(int argc, char const *argv[]) {


    /*****************************
     * OCR
     ****************************/
    char const *image_alphabet = "./data/image_alphabet.png";
    char const *text_alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    int const alphabet_length = 26;

    
    // creatre alphabet template
    // Note: this is needed only once to generate the .sav files
    /*
    int ret = create_template(image_alphabet, text_alphabet, alphabet_length);
    if (ret != 0) {
        printf("Could not load alphabet image file: %s\n", image_alphabet);
        return -1;
    }
    */

    

    // load alphabet template
    vector<Letter> letters;
    load_template(&letters, alphabet_length);

    // load input
    char const *image_input = "./data/input_5_OK.png";
    vector< vector<int> > pixels;
    if (load_image(image_input, &pixels) != 0) {
        printf("Could not load input image: %s\n", image_input);
        return -1;
    }
    
    // convert input to C type for ECALL
    vector<int*> ptrs;
    transform(begin(pixels), end(pixels), back_inserter(ptrs), [](vector<int> &inner_vec) {
        return inner_vec.data();
    });
    int **input =  ptrs.data();
    int rows = pixels.size();
    int cols = pixels[0].size();


    
    // convert alphabet letters to C type for ECALL
    vector< vector<int> > letters_vec;
    for(int i=0; i<letters.size(); i++) {
        // export letter i
        vector< vector<int> > matrix = letters[i].getMatrix();
        int data_length = matrix.size() * matrix[0].size() + 3;
        int data[data_length];
        letters[i].exportLetter(data);
        
        // save letter
        int data_size = sizeof(data) / sizeof(data[0]);
        vector<int> tmp_vec;
        copy(&data[0], &data[data_size], back_inserter(tmp_vec));
        //vector<int> tmp_vec (data,  data + sizeof(data) / sizeof(data[0]));
        letters_vec.push_back(tmp_vec);
    }
    vector<int*> letters_ptrs;
    transform(begin(letters_vec), end(letters_vec), back_inserter(letters_ptrs), [](vector<int> &inner_vec) {
        return inner_vec.data();
    });
    int **letters_c =  letters_ptrs.data();
    int letters_rows = letters_vec.size();



    /********************** START ECALL **********************/
    // perform OCR on input
    char recognised_letters[100]; // make array big enough
    int length;
    character_recognition_wrap(input, rows, cols, letters_c, letters_rows, recognised_letters, &length);

    /*********************** END ECALL ***********************/


    // print result
    printf("\nOCR output: ");
    for (int i = 0; i < length; i++) {
        printf("%c", recognised_letters[i]);
    }
    printf("\n");


    // free mem
    pixels.clear();


    return 0;
}
