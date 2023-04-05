package net.nussi;

import com.fazecast.jSerialComm.SerialPort;

import java.beans.Encoder;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class ComTest {

    public static void main(String[] args) throws IOException, InterruptedException {

        SerialPort serialPort = Main.getSerialPort();


        serialPort.setComPortTimeouts(SerialPort.TIMEOUT_WRITE_BLOCKING, 100, 100);
        serialPort.setBaudRate(9600);
        serialPort.setNumDataBits(8);
        serialPort.setNumStopBits(1);
        serialPort.setParity(SerialPort.NO_PARITY);


        serialPort.openPort();


        Thread.sleep(1000);

        int numWritten = serialPort.writeBytes(new byte[]{0x10}, 1);
        System.out.println("Wrote " + numWritten + " bytes to serial port");


        serialPort.closePort();
    }

}
