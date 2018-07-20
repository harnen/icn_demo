#include <tesseract/baseapi.h>
#include <leptonica/allheaders.h>
#include <string>
#include <iostream>

#include "tess.h"
#include "string.h"

std::string run_tess(char const* image_input)
{
    char *outText;

    std::string response ("");

    tesseract::TessBaseAPI *api = new tesseract::TessBaseAPI();
    // Initialize tesseract-ocr with English, without specifying tessdata path
    if (api->Init(NULL, "eng")) {
        fprintf(stderr, "Could not initialize tesseract.\n");
        exit(1);
    }

    // Open input image with leptonica library
    Pix *image = pixRead(image_input);
    api->SetImage(image);
    // Get OCR result
    outText = api->GetUTF8Text();
    printf("OCR output:\n%s", outText);

    // Destroy used object and release memory
    api->End();
    //delete [] outText;
    

    // print result
    response.append("OCR output: ");
    response = response + outText;
    
    printf("\n");
    std::cout << response << std::endl;

    // free mem
    pixDestroy(&image);

    return response;
}
