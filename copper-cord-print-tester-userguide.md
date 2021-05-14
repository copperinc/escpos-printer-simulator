# Copper Cord Print Tester User Guide


## Introduction

The Copper Cord Print Tester is an application that emulates a printer in
order to test the Copper Cord device.  The app can be used with Serial Cord
devices with USB-Serial adapters, as well as USB Cord devices that use
usb-serial drivers.

## Serial Setup

Plug the USB-Serial Adapter into the computer. Later the Cord device may be
plugged in and removed separately from the USB-Serial adapter.

Start the application.  

    java -jar copper-cord-print-tester-x.y.jar

The top pull-down list should show a listing of serial devices detected on the
computer. If none are detected, the top button will be labeled "Scan Ports".
Check that the USB-serial adapter is plugged in and has been recognized by the
operating system. If it is recognized, click "Scan Ports" and the device should
appear in the port list.

Once the serial adapter has been recognized, select the port and the baud rate
to test at, and click "Open". The display area should show 
"== Port <port> opened at <baud>".

With the Cord device unpowered, plug in the device into the serial adapter,
then power the Cord, holding down the button to enter the Cord provisioning
mode. If the Cord device is blinking pink the device has successfully entered
provisioning mode.

In provisioning mode, if the application is able to recognize Cord print
commands at the selected speed it will run a series of four tests. The
provisioning introduction receipt showing the serial number and QR code for
the device should be printed to the display. All four tests should pass if the
Cord is operating normally.

Once the device has generated output, the Clear button can be clicked to ready
the display and application for the next device - the port does not need to be
closed and reopened.


## Troubleshooting

Some unprintable box characters may be sent to the disply during serial port
baud negotiation.  If the baud was negotiated successfully, as long as the
Cord provisioning receipt is shown afterward and all tests pass, this is
expected.

If the application is only showing unprintable characters, you may want to
close the serial port and reopen it at a different speed to assess if the
problem associated with only some baud rates.



