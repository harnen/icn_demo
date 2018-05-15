# icn_demo
The repository contains an edge computing demo for ICN'18

# OCR
We implement and evaluate a simple Optical Character Recognition (OCR) algorithm to illustrate the execution of pure programs. Our simple algorithm is initialized in the enclave with a model of each letter of the alphabet; it can then be fed with input images to detect the list of embedded letters. We implemented this example to show how our system can be used to outsource execution of pure
programs.

## Compilation

```
  cd ocr
  make
  ./app
```
