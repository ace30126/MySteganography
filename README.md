# MyStegLSB

A simple LSB image steganography tool for CTF.

## Overview

MyStegLSB is a lightweight desktop application designed for encoding and decoding messages using the Least Significant Bit (LSB) steganography technique in images. This tool was created to quickly address LSB-based image steganography challenges often found in Capture The Flag (CTF) competitions.

**Key Features:**

* Simple GUI for easy use.
* **LSB Encoding:** Embeds text messages within images using the Least Significant Bit method.
* **LSB Decoding:** Extracts hidden text messages from images encoded using the LSB method.

## Installation

As this is a standalone executable (`MyStegLSB.exe`), no formal installation is required. Simply download the file and run it.

## Usage

1.  **Launch the Application:** Double-click `MyStegLSB.exe` to open the graphical interface.
2.  **Encoding:**
    * Select an image file you want to encode the message into.
    * Enter the secret message you wish to embed.
    * Specify the output file name and location for the encoded image.
    * Click the "Encode" button.
3.  **Decoding:**
    * Select the image file that you believe contains a hidden message.
    * Click the "Decode" button.
    * The extracted message will be displayed in the application.

## Built With

* [JAVA](https://www.java.com/) - The programming language used.
* [Swing](https://docs.oracle.com/javase/8/docs/api/javax/swing/package-summary.html) - The GUI toolkit for Java.

## Author

* REDUCTO (https://tutoreducto.tistory.com/663)

## Creation Date

* April 28, 2025 (1 day of development)

## Version

* v1.0

## Acknowledgements

* This program was created with the assistance of Gemini.
* Inspired by the frequent occurrence of LSB steganography challenges in CTF competitions.

---
