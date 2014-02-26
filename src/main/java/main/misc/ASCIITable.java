package main.misc;

/**
 * Author: mrzl
 * Date: 06.02.14
 * Time: 15:15
 * Project: main.GoogleAnalyticsInstallation
 */
public class ASCIITable {
    /**
     * this converts an character into the decimal representation in order to send it over the serial
     * port and being able to interpret it on the arduino source: http://www.bbdsoft.com/ascii.html
     *
     * @param decimalRepresentation character representation
     * @return
     */
    public static int getCharFromInt(char decimalRepresentation) {
        int returnValue = 0;
        switch (decimalRepresentation) {
            case '1':
                returnValue = 49;
                break;
            case '2':
                returnValue = 50;
                break;
            case '#':
                returnValue = 35;
                break;
        }
        return returnValue;
    }
}
