package main;

import controlP5.ControlEvent;
import main.controller.ArduinoController;
import main.misc.ASCIITable;
import main.misc.GlobalParameters;
import main.misc.Misc;
import peasy.PeasyCam;
import processing.core.PApplet;
import processing.serial.Serial;

import javax.swing.*;

/**
 * Author: mrzl
 * Date: 06.02.14
 * Time: 15:05
 * Project: main.GoogleAnalyticsInstallation
 */
public class GoogleAnalyticsInstallation extends PApplet {

    private ArduinoController arduino;
    private PeasyCam cam;
    private CellShape cell;

    public void init() {
        frame.setIconImage( new ImageIcon( sketchPath( "image/onf.jpg" ) ).getImage() );
        frame.setTitle( "Simulator" );
        super.init();
    }

    public void setup() {
        size( 630, 350, P3D );
        textFont( createFont( "Replica-Light", 40 ) );

        initComponents();
    }

    private void initComponents() {
        cam = new PeasyCam( this, 800 );
        cam.setActive( false );

        cell = new CellShape( this, "image/cell_simple2.stl" );

        arduino = new ArduinoController( this, GlobalParameters.TUBE_COUNT, GlobalParameters.CELL_COUNT );

        ControlFrame.createControlFrame( this, "Controls", 800, 400 );
    }

    public void draw() {
        background( 0 );

        scale( 0.5f );
        arduino.update();
        translate( -570, -38 );
        for ( int i = 0; i < GlobalParameters.TUBE_COUNT; i++ ) {
            drawSimulator( i, i * 60 );
        }

        cam.beginHUD();
        pushStyle();
        fill( 0, 150 );

        rect( 0, 0, width, height );
        popStyle();
        cam.endHUD();
    }

    void drawSimulator( int tubeNr, float _x ) {
        ortho();
        lights();
        fill( 255 );
        noStroke();
        pushMatrix();
        translate( _x, 350 );

        for ( int i = 0; i < arduino.getColumnHeight(); i++ ) {
            pushMatrix();
            translate( 0, -i * 34 );
            rotateY( radians( arduino.tl.getCurrentStates()[ tubeNr ][ i ] ) );
            fill( 90 );
            cell.render();
            popMatrix();
        }

        noLights();
        pushStyle();
        fill( 255 );
        Misc.drawCylinder( this, 15, 2000, 10 );
        popStyle();
        popMatrix();
    }


    @SuppressWarnings( "unused" )
    public void controlEvent( ControlEvent theEvent ) {
        System.out.println( "Received a ControlEvent from " );
        System.out.println( theEvent.getLabel() );

        for ( int i = 0; i < GlobalParameters.TUBE_COUNT; i++ ) {
            if ( theEvent.getLabel().equals( "a" + i ) ) {
                arduino.adjust( i );
            }
        }
    }

    @SuppressWarnings( "unused" )
    public void serialEvent( Serial p ) {
        String message = p.readStringUntil( '\n' );


        if ( message.startsWith( "debug" ) ) {
            System.out.println( message );
            //
            message = message.substring( 5 );
            // trim off any whitespace:
            message = trim( message );
            // convert to an int and map to the screen height:
            float inByte = Float.parseFloat( message );
            System.out.println( "debug message: " + message );
        } else {
            System.out.println( "main.controller.ArduinoController incoming message: " + message );
        }

        // this string is important and has to fit the string the arduino sketch.
        // it sets the arduino class free and after this new instructions can be sent over to
        // the arduino itself.
        if ( message.equals( "done processing instructions\n" ) ) {
            arduino.setIdling();
        }
    }

    public ArduinoController getArduino() {
        return this.arduino;
    }

    public void keyPressed() {
        if ( key == 'r' ) {
            // rotates the arduino to random positions
            boolean[] randomStates = new boolean[ arduino.getColumnHeight() ];
            for ( int i = 0; i < arduino.getColumnHeight(); i++ ) {
                if ( random( 1 ) > 0.5 ) {
                    randomStates[ i ] = GlobalParameters.BLACK;
                } else {
                    randomStates[ i ] = GlobalParameters.WHITE;
                }
            }
            arduino.rotateToState( randomStates );
        }
        if ( key == 'b' ) {
            // clears the column to black
            boolean[] blackStates = new boolean[ arduino.getColumnHeight() ];
            for ( int i = 0; i < arduino.getColumnHeight(); i++ ) {
                blackStates[ i ] = GlobalParameters.BLACK;
            }
            arduino.rotateToState( blackStates );
        }

        if ( key == 'o' ) {
            // rotates the lowest element by 180° in order to test how accurate the motor is rotating
            arduino.sendRotationToArduino( ASCIITable.getCharFromInt( '1' ) );
        }

        if ( key == 'a' ) {
            arduino.rotateAccordingToAnalytics();
        }
    }

    /**
     * This is where it all begins.-
     *
     * @param args command line stuff
     */
    public static void main( String[] args ) {
        PApplet.main( new String[]{ "--hide-stop", "main.GoogleAnalyticsInstallation" } );
    }
}
