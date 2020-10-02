# Shared Clipboard
Share your clipboard between your Android smartphone and any computer.

## Project structure
This project consists of two applications:
1. **server**  web client and backend
2. **app** android app as smartphone client


## Requirements:
- java11
- redis 3.2+ (key value in memory database)
- Android SDK API 29

## Setup
1. start redis via command line `redis-server`
2. in server project folder start the spring boot application by typing `gradlew bootRun`into the command line
3. Deploy the app to your smarthpone via Android Studio

## How it works
The server provides a web interface with a QR code. With the smartphone app you can scan the QR code to connect with your computer (or any device from which you are accessing the website).
Once connected you can copy text in your smartpho and the clipboard is displayed in the web interface. The website will copy it to your computers clipboard automatically.
1. open `localhost:8090`
2. open smartphone app
3. in the app click on the capture button (right lower corner)
4. Take a picture of the QR code and click ok
5. The website redirects you to your clipboard page. You can now start copying on your smartphone
6. Once you copied a text, you can see it on the webpage inside the box.
7. To copy images, use the share function on your smartphone. The image will appear for download on the webpage.
