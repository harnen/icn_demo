#ifndef OCR_H
#define OCR_H

void character_recognition_wrap(int** input, int rows, int cols, int** letters_c, int letters_rows,
    char *output_letters, int *length);

std::string run_ocr(char const* image_input);
#endif /* OCR_H */
