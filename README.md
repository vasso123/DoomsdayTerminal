# DoomsdayTerminal
 Graphical CLI app for testing TCP and serial(yet to be implemented) communication. Still in early stages, but works with some quirks.
 App uses the awesome [**Lanterna**](https://github.com/mabe02/lanterna) library as the GUI toolkit.

![Doomsday Terminal screenshot](screenshot.png)

## Features
* 3 types of connections: TCP client, TCP server and serial,
* display received and sent data either in raw or in hex representation,
* send data either in raw or in hex representation,
* where possible, clipboard support.

## Features yet to be implemented
* log received and sent data to a chosen file,
* send the contents of a chosen file,
* join two connections so they talk through the app, as the exchanged data is displayed/logged to a file
* use the Java 9 modularization to build a minimal, zero dependency native app for all platforms.
