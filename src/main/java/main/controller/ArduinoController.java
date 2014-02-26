package main.controller;

import main.misc.GlobalParameters;
import main.tl.TL_Field;
import processing.core.PApplet;
import processing.serial.Serial;

import java.util.Scanner;

/**
 * Author: mrzl
 * Date: 06.02.14
 * Time: 15:13
 * Project: main.GoogleAnalyticsInstallation
 */
public class ArduinoController {
    public TwitterController twitterController = null;
    public GoogleAnalyticsController googleAnalyticsController = null;

    public Serial serialPort;
    public TL_Field tl;

    private PApplet p;

    private Status status;

    private int columnCount, columnHeight;

    public ArduinoController( PApplet parent, int columnCount, int columnHeight ) {
        this.p = parent;
        this.columnCount = columnCount;
        this.columnHeight = columnHeight;

        this.twitterController = new TwitterController( p );
        // this.googleAnalyticsController = new main.controller.GoogleAnalyticsController(p, 24);

        //googleAnalyticsController.start();

        initArduino();
    }

    private void initArduino() {
        if ( serialPort != null ) {
            serialPort.clear();
            serialPort.dispose();
        }

        System.err.println( "Initializing main.controller.ArduinoController Interface." );
        System.err.println( "Listing available serialconnections:" );
        String[] serialConnections = Serial.list();
        for ( int i = 0; i < serialConnections.length; i++ ) {
            System.out.print( i + " : " );
            System.out.println( serialConnections[ i ] );
        }

        int chosenDevice;
        Scanner scanner = new Scanner( System.in );
        chosenDevice = scanner.nextInt();
        System.out.println( "chosen: " + chosenDevice );
        scanner.close();

        serialPort = new Serial( p, Serial.list()[ chosenDevice ], 9600 );
        serialPort.bufferUntil( '\n' );

        tl = new TL_Field( columnCount, columnHeight );
        //tl.clearBlack();
        tl.rotationStep( 20 );

    }

    public void update() {

        if ( twitterController.running ) {
            //twitterController.update();

            if ( twitterController.wasSentToSleep() ) {
                setSleeping();
            } else {
                setIdling();
            }
        }

        tl.update();
    }

    public void rotateAccordingToAnalytics() {
        float maximumVisitsPerDayLast3Months = 400;
        float percentToday =
                googleAnalyticsController.getVisitorStatistics().get( 0 ) / maximumVisitsPerDayLast3Months;
        // float percentToday = mouseX/maximumVisitsPerDayLast3Months;
        // println(percentToday);
        int mappedVisitorsToHeightOfColumn = ( int ) ( PApplet.map( percentToday, 0, 1, 0, columnHeight ) );
        boolean[] instructionsToPerform = new boolean[ columnHeight ];
        System.out.println( mappedVisitorsToHeightOfColumn );
        for ( int i = 0; i < columnHeight; i++ ) {
            if ( i < mappedVisitorsToHeightOfColumn ) {
                instructionsToPerform[ i ] = GlobalParameters.WHITE;
            } else {
                instructionsToPerform[ i ] = GlobalParameters.BLACK;
            }
        }
        rotateToState( instructionsToPerform );
    }

    /**
     * core element of this class. this transforms the states noted in a boolean array into a way the
     * arduino motors can understand it. and it sends the date to the arduino at the same time.
     *
     * @param states boolean array containing the states
     */
    public String rotateToState( boolean[] states ) {
        String sentRots = "";
        int[] ro;
        if ( isSleeping() || isBusy() ) {
            System.out.println( "Don't interrupt me. I'm either sleeping or busy." );
        } else {
            setBusy();

            int[] rotationToPerform = getRotationsForState( states );

            // testing
            String sendToArduino = "r";
            ro = new int[ rotationToPerform.length ];
            for ( int i = 0; i < rotationToPerform.length; i++ ) {
                System.out.println( "sent rotations: " + rotationToPerform[ i ] / 180 );
                sentRots += ( rotationToPerform[ i ] / 180 ) + " ";
                ro[ i ] = ( rotationToPerform[ i ] / 180 );
                // this transforms rotations like "-4 half rotations" into "4 * -1 half rotations"
                int halfRotationCount = PApplet.abs( ( rotationToPerform[ i ] / 180 ) );
                for ( int j = 0; j < halfRotationCount; j++ ) {
                    if ( rotationToPerform[ i ] > 0 ) {
                        // sendRotationToArduino(main.misc.ASCIITable.getCharFromInt('2'));
                        sendToArduino += "2";
                    } else {
                        // sendRotationToArduino(main.misc.ASCIITable.getCharFromInt('1'));
                        sendToArduino += "1";
                    }
                }
            }
            // # = indicating that the instructions are done
            // sendRotationToArduino(main.misc.ASCIITable.getCharFromInt('#'));

            // testing TODO: wieder einkommentieren, wenn es gehen soll - 03.07.
            sendStringToArduino( sendToArduino );

        }

        return sentRots;
    }

    public String rotateToState( boolean[][] states ) {
        String sentRots = "";
        if ( isSleeping() || isBusy() ) {
            System.out.println( "Don't interrupt me. I'm either sleeping or busy." );
        } else {
            setBusy();
            String sendToArduino = "";
            String finishedRotations = "";
            int[][] rotationToPerform = getRotationsForState( states );

            for ( int i = 0; i < rotationToPerform.length; i++ ) {
                sendToArduino += "r";
                finishedRotations += "r";
                for ( int j = 0; j < rotationToPerform[ i ].length; j++ ) {
                    //System.out.print("(debug: "+rotationToPerform[i][j]+")");
                    sentRots += ( rotationToPerform[ i ][ j ] / 180 ) + " ";
                    int rot = rotationToPerform[ i ][ j ] / 180;
                    finishedRotations += rot + "";
                    int halfRotationCount = PApplet.abs( rot );
                    for ( int k = 0; k < halfRotationCount; k++ ) {
                        if ( rot > 0 ) {
                            // sendRotationToArduino(main.misc.ASCIITable.getCharFromInt('2'));
                            sendToArduino += "2";
                        } else {
                            // sendRotationToArduino(main.misc.ASCIITable.getCharFromInt('1'));
                            sendToArduino += "1";
                        }
                    }
                }
                sentRots += "\n";

            }
            sendToArduino += "q";
            finishedRotations += "q";
            sendStringToArduino( sendToArduino );
        }
        return sentRots;
    }

    public void setInstallationResolution( int w, int h ) {
        this.columnCount = w;
        this.columnHeight = h;
        initArduino();
    }

    public void rotateHalfRoundCW() {
        String rotate = "r1";
        sendStringToArduino( rotate );
    }

    public void rotateHalfRoundCCW() {
        String rotate = "r2";
        sendStringToArduino( rotate );
    }

    public void adjust() {
        String adjustRot = "o1";
        sendStringToArduino( adjustRot );
    }

    public void adjust( int columnNr ) {
        String adjust = "s";
        adjust += columnNr;
        System.out.println( "Sending " + adjust + " to arduino." );
        sendStringToArduino( adjust );
    }

    /**
     * sends the motorspeed as a string. the string is parsed into an int in the arduino code
     *
     * @param motorspeed
     */
    public void setMotorspeed( int motorspeed ) {
        String speed = "m" + motorspeed + "\n";
        //serialPort.write(speed);
        System.out.println( "Changed motor speed to " + speed + "." );
    }

    public void setRotationDegree( int deg ) {
        String degree = "d" + deg + "\n";
        serialPort.write( degree );
        System.out.println( "Changed rotation degree to " + degree + "." );
    }

    public void setAcceleration( int acc ) {
        String acceleration = "a" + acc + "\n";
        //serialPort.write(acceleration);
        System.out.println( "Changed acceleration to " + acc + "." );
    }

    public void sendStringToArduino( String input ) {
        input += "\n";
        System.out.println( "I sent the following command to the arduino: " + input );
        serialPort.write( input );
    }


    /**
     * this calculates the neccessary rotations needed to put the installation into the right
     * constellation
     *
     * @param states boolean array indicating the states of the individual cells
     * @return int[] array containing a list of rotations e.g. -2 = 2 full rotations counterclockwise;
     * 5 = 5 full rotations clockwise
     */
    public int[] getRotationsForState( boolean[] states ) {

        tl.set( states, 0 );
        return tl.instructionCalculator.calculateInstructions( 0 );
    }

    public int[][] getRotationsForState( boolean[][] states ) {
        tl.set( states );
        int[][] returnStates = new int[ states.length ][ states[ 0 ].length ];
        for ( int i = 0; i < states.length; i++ ) {
            returnStates[ i ] = tl.instructionCalculator.calculateInstructions( i );
            System.out.println( "i=" + i + " length=" + returnStates[ i ].length );
            for ( int j = 0; j < returnStates[ i ].length; j++ ) {
                System.out.print( returnStates[ i ][ j ] / 180 + " " );
            }
            System.out.println();
        }
        return returnStates;
    }

    /**
     * this actually sends the date over the serialport to the arduino
     *
     * @param direction integer indicating the direction in which the lowest element should be rotated
     */
    public void sendRotationToArduino( int direction ) {
        serialPort.write( direction );
    }


    /**
     * getter to check if the arduino is in sleeping mode
     *
     * @return boolean indicating if sleeping or not
     */
    public boolean isSleeping() {
        return this.status == Status.SLEEPING;
    }

    /**
     * setter to either set the arduino to sleep or wake it up
     */
    public void setSleeping() {
        this.status = Status.SLEEPING;
    }

    /**
     * getter to check if the arduino is currently busy performing rotations
     *
     * @return bool indicating if the installation is currently rotating
     */
    public boolean isBusy() {
        return this.status == Status.BUSY;
    }

    /**
     * sets the installation to being busy or not
     */
    public void setBusy() {
        this.status = Status.BUSY;
    }

    public boolean isIdling() {
        return this.status == Status.IDLING;
    }

    public void setIdling() {
        this.status = Status.IDLING;
    }

    public int getColumnCount() {
        return columnCount;
    }

    public int getColumnHeight() {
        return columnHeight;
    }
}
