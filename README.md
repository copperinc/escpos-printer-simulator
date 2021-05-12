# Copper Cord device tester

Build a new main class, updated the parser to be more robust for
framing commands from a live host device connected to a serial port.

## Version Notes

### V0.2.3
* Rescan serial ports on certain gui events to account for plugging/unplugging 
  of devices
* Parser fix to improve handling of rejected chars

Note: At higher serial port speeds, the simulator may receive some garbage
characters at the start of the speed handshake as the fw tries commands at 
lower serial speeds, those can still be read with inconsistent bytes. In 
testing so far, the parser has always recovered.

In general, 9600 baud, the first handshake speed the firmware attempts shows
a clean "provisioning receipt"




### V0.2.2
* Parser fixes to recognize two byte cut commands
* Added test for cut command on provision
* Added test for unintended echo replies

### V0.2.1
Parser fixes for unprintable characters

### V0.2
Added a new gui panel with controls changes for testing.

## Run notes

The build will create a jar file: ./build/libs/copper-cord-print-tester-x.y.jar

The program should be runnable via java -jar copper-cord-print-tester-x.y.jar


## Build notes
The Gui panels and as well as the new Copper specific
dialog was laid out w/ the netbeans IDE

A gradle build config was added, allowing command line builds

gradle build : will build the jar file
gradle run   : will build and run the tester




# Previous escpos-printer-simulator notes

## escpos-printer-simulator
ESC/POS Printer simulator written in Java.  
This provide Swing GUI to display receipts and also write content to a file - access through web url.  
The idea comes from JavaPos printer simulator and from [escpos-tools](https://github.com/receipt-print-hq/escpos-tools), a PHP library.  
<img src="./docs/escpos-printer-simulator.png" width="400">   

Web access:
<img src="./docs/escpos-web.png" width="800">   
## Usage
```
java -jar ESCPosPrinterSimulator.jar [port] [filepath] [filesize]

Example:
java -jar ESCPosPrinterSimulator.jar
java -jar ESCPosPrinterSimulator.jar 9100
java -jar ESCPosPrinterSimulator.jar 9100 receipts.txt 10

run as linux / ubuntu background:
java -jar ESCPosPrinterSimulator.jar 9100 receipts.txt 200 & exit
```
Arguments:
- port: optional - default 9100.  
- filepath: path to the written receipt - if available, program will run without GUI.  
- filesize: in KB - default 200. If reach the size, file will be deleted then recreated.  

Deploy on linux Apache & PHP 
```
java -jar ESCPosPrinterSimulator.jar 9100 /var/www/html/escpos-printer/tmp/9100.txt & exit  

access through url:  
http://{ip}/escpos-printer/printer-page.php?files=9100.txt  
```