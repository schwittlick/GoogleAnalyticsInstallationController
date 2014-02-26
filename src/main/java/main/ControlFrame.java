package main;

import controlP5.ControlEvent;
import controlP5.ControlP5;
import controlP5.Matrix;
import main.misc.GlobalParameters;
import processing.core.PApplet;

import java.awt.*;

/**
 * Author: mrzl
 * Date: 12.02.14
 * Time: 10:57
 * Project: GoogleAnalyticsInstallation
 */
public class ControlFrame extends PApplet {

    // the parent class.
    private GoogleAnalyticsInstallation parent;

    private int width, height;
    private Matrix matrix;


    /**
     * The constructor of the ControlFrame
     *
     * @param parent the PApplet, which creates this Applet
     * @param width  the width of the Frame
     * @param height the height of the Frame
     */
    public ControlFrame( PApplet parent, int width, int height ) {
        // casted, in order to call methods created in the PastaPeelPApplet class
        this.parent = ( GoogleAnalyticsInstallation ) parent;

        // setting with and height
        this.width = width;
        this.height = height;

    }

    /**
     * Being called, since this is a PApplet
     */
    public void setup() {
        ControlP5 cp = new ControlP5( this );
        cp.setBroadcast( false );

        cp.addBang( "randomValues" ).setPosition( 10, 90 ).setSize( 50, 30 );
        cp.addBang( "randomValuesM" ).setPosition( 100, 90 ).setSize( 50, 30 );
        cp.addBang( "clearBlack" ).setPosition( 190, 90 ).setSize( 50, 30 );
        cp.addSlider( "motorSpeed" ).setPosition( 10, 150 ).setSize( 300, 20 ).setRange( 0, 9600 )
                .setValue( 2000 ).setUpdate( false );
        cp.addSlider( "rotationDegree" ).setPosition( 10, 180 ).setSize( 300, 20 ).setRange( 50, 900 )
                .setValue( 689 ).setUpdate( false );
        cp.addSlider( "acceleration" ).setPosition( 10, 210 ).setSize( 300, 20 ).setRange( 10, 2000 )
                .setValue( 1800 ).setUpdate( false );

        // small adjust buttons for each tube
        for ( int i = 0; i < GlobalParameters.TUBE_COUNT; i++ ) {
            cp.addBang( "a" + i ).setPosition( 20 + i * 20, 300 ).setSize( 10, 10 );
        }

        matrix = cp.addMatrix( "display" ).setPosition( 450, 10 )
                .setSize( GlobalParameters.TUBE_COUNT * 15, GlobalParameters.CELL_COUNT * 15 )
                .setGrid( GlobalParameters.TUBE_COUNT, GlobalParameters.CELL_COUNT ).setGap( 5, 5 )
                .setMode( ControlP5.MULTIPLES )
                .setColorBackground( color( 120 ) )
                .setBackground( color( 0 ) )
                .stop();

        cp.addBang( "do_it" ).setPosition( 450, 300 ).setSize( 30, 15 );

        cp.setBroadcast( true );
    }

    /**
     * Draws the Applet
     */
    public void draw() {
        background( 0 );
        updateStatus();
    }

    private void updateStatus() {
        if ( parent.getArduino().isBusy() ) {
            fill( 255, 0, 0 );
        } else if ( parent.getArduino().isIdling() ) {
            fill( 0, 255, 0 );
        } else if ( parent.getArduino().isSleeping() ) {
            fill( 0, 0, 255 );
        }
        rect( width - 100, height - 50, 100, 50 );
    }

    /**
     * This is where all ControlEvents are being interpreted
     *
     * @param e the ControlEvent containing all information about the event
     */
    @SuppressWarnings( "unused" )
    public void controlEvent( ControlEvent e ) {
        System.out.println( e );
        switch ( e.getName() ) {
            case "do_it":
                do_it();
                break;
            case "randomValuesM":
                randomValuesM();
                break;
            case "randomValues":
                randomValues();
                break;
            case "motorSpeed":
                motorSpeed( ( int ) e.getValue() );
                break;
            case "rotationDegree":
                rotationDegree( ( int ) e.getValue() );
                break;
            case "acceleration":
                acceleration( ( int ) e.getValue() );
                break;
            case "clearBlack":
                clearBlack();
                break;
        }
    }

    public void do_it() {
        System.out.println( "im DOING it" );
        boolean[][] randomStates = new boolean[ GlobalParameters.TUBE_COUNT ][ GlobalParameters.CELL_COUNT ];
        for ( int i = 0; i < GlobalParameters.TUBE_COUNT; i++ ) {
            for ( int j = 0; j < GlobalParameters.CELL_COUNT; j++ ) {
                randomStates[ i ][ j ] = matrix.get( i, GlobalParameters.CELL_COUNT - j - 1 );
            }
        }

        String rots = parent.getArduino().rotateToState( randomStates );
        if ( rots.isEmpty() ) {
            Logger.log( "tried to add rotation, but busy" );
        } else {
            Logger.log( "random values rotated " + rots );
        }
    }

    public void randomValues() {
        boolean[] randomStates = new boolean[ parent.getArduino().getColumnHeight() ];
        for ( int i = 0; i < parent.getArduino().getColumnHeight(); i++ ) {
            if ( random( 1 ) > 0.5f ) {
                randomStates[ i ] = GlobalParameters.BLACK;
            } else {
                randomStates[ i ] = GlobalParameters.WHITE;
            }
        }
        String rots = parent.getArduino().rotateToState( randomStates );
        if ( rots.isEmpty() ) {
            Logger.log( "tried to add rotation, but busy" );
        } else {
            Logger.log( "random values rotated " + rots );
        }
    }

    void randomValuesM() {
        boolean[][] randomStates = new boolean[ GlobalParameters.TUBE_COUNT ][ GlobalParameters.CELL_COUNT ];
        for ( int i = 0; i < GlobalParameters.TUBE_COUNT; i++ ) {
            for ( int j = 0; j < GlobalParameters.CELL_COUNT; j++ ) {
                if ( random( 1 ) > 0.5f ) {
                    randomStates[ i ][ j ] = GlobalParameters.BLACK;
                } else {
                    randomStates[ i ][ j ] = GlobalParameters.WHITE;
                }
                matrix.set( i, GlobalParameters.CELL_COUNT - j - 1, randomStates[ i ][ j ] );
            }
        }

        String rots = parent.getArduino().rotateToState( randomStates );
        if ( rots.isEmpty() ) {
            Logger.log( "tried to add rotation, but busy" );
        } else {
            Logger.log( "random values rotated " + rots );
        }
    }


    public void clearBlack() {
        String toRotate = "";

        for ( int i = 0; i < parent.getArduino().getColumnCount(); i++ ) {
            toRotate += "r";
            for ( int j = 0; j < parent.getArduino().getColumnHeight(); j++ ) {
                toRotate += "2";
            }
        }

        parent.getArduino().sendStringToArduino( toRotate );
    }

    public void motorSpeed( int val ) {
        parent.getArduino().setMotorspeed( val );
    }

    public void rotationDegree( int val ) {
        parent.getArduino().setRotationDegree( val );
    }

    public void acceleration( int val ) {
        parent.getArduino().setAcceleration( val );
    }

    public void toggleTwitterUpdates() {
        // i know this is missguiding, just leave it like that
        if ( !parent.getArduino().twitterController.running ) {
            Logger.log( "Twitter enabled" );
        } else {
            Logger.log( "Twitter disabled" );
        }
        parent.getArduino().twitterController.running = !parent.getArduino().twitterController.running;
    }

    public void toggleGoogleUpdates() {
        if ( !parent.getArduino().googleAnalyticsController.running ) {
            Logger.log( "Google enabled" );
        } else {
            Logger.log( "Google disabled" );
        }
        parent.getArduino().googleAnalyticsController.running = !parent.getArduino().googleAnalyticsController.running;
    }


    public void cellCount( int val ) {
        parent.getArduino().setInstallationResolution( parent.getArduino().getColumnCount(), val );
    }

    /**
     * Returns the width of the frame
     *
     * @return width the width of this frame window
     */
    public int getWidth() {
        return this.width;
    }

    /**
     * returns the height of the frame
     *
     * @return height the height of this frame window
     */
    public int getHeight() {
        return this.height;
    }


    /**
     * static method to create a ControlFrame -> factory?
     *
     * @param _p     the parent PApplet
     * @param name   the name of the frame
     * @param width  the width of the frame
     * @param height the height of the frame
     */
    static ControlFrame createControlFrame( PApplet _p, String name, int width, int height ) {
        // creates a new frame with the passed frame name
        Frame f = new Frame( name );
        ControlFrame p = new ControlFrame( _p, width, height );
        f.add( p );
        p.init();
        f.setTitle( name );
        f.setSize( p.getWidth(), p.getHeight() );
        f.setResizable( false );
        f.setVisible( true );

        return p;
    }
}