package main.tl;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * Author: mrzl
 * Date: 06.02.14
 * Time: 15:16
 * Project: main.GoogleAnalyticsInstallation
 */
public class TL_Field {

    private static int CW = -1;
    private static int CCW = 1;

    private int nColumns;
    private int nCells;
    private TL_Column[] columns;

    protected static int rotationStep = 1;

    public InstructionCalculator instructionCalculator;

    public TL_Field( int nColumns, int nCells ) {
        this.nColumns = nColumns;
        this.nCells = nCells;
        columns = new TL_Column[ nColumns ];

        instructionCalculator = new InstructionCalculator();

        for ( int i = 0; i < nColumns; i++ ) {
            columns[ i ] = new TL_Column( nCells );
        }
    }

    /**
     * Rotate the amount that is set by rotationStep.
     */
    public boolean update() {

        int readyCount = 0;

        for ( TL_Column tlc : columns ) {
            if ( tlc.update() ) {
                readyCount++;
            }
        }

        return readyCount == nColumns ? true : false;

    }

    /**
     * Set the angle for the column where the index matches.
     */
    public void setAngles( int[] angles ) {
        for ( int col = 0; col < angles.length; col++ ) {
            setAngle( angles[ col ], col );
        }
    }

    /**
     * Set the specified column to match those angles.</br> WARNING! This can be tricky, if you don't
     * understand why </br> then don't use it.
     */
    public void setAngle( int[] angle, int column ) {
        // ready = false;
        TL_Column tlc = columns[ column ];
        tlc.set( angle );
    }

    /**
     * Set all columns to match this angle.
     */
    public void setAngles( int angle ) {
        for ( int i = 0; i < nColumns; i++ ) {
            setAngle( angle, i );
        }
    }

    /**
     * Set all cells of the specified column to the specified angle.
     */
    public void setAngle( int angle, int column ) {
        // ready = false;
        for ( int i = 0; i < nCells; i++ ) {
            columns[ column ].targetRotations[ i ] = angle;
        }
        // finish it fast
        targetDirectionCW( column );
    }

    /**
     *
     */
    public void invert() {
        // ready = false;
        for ( TL_Column tlc : columns ) {
            tlc.invert();
        }
        calculateBestRotations();
    }

    /**
     * @return
     */
    public int[][] getCurrentStates() {

        int[][] ret = new int[ nColumns ][ nCells ];

        for ( int i = 0; i < nColumns; i++ ) {
            // main.tl.TL_Column tlc = columns[i];
            // ret[i] = tlc.rotations;
            ret[ i ] = currentState( i );
        }

        return ret;
    }

    /**
     * @param column
     * @return
     */
    public int[] currentState( int column ) {

        int[] ret = new int[ nCells ];
        TL_Column tlc = columns[ column ];
        ret = tlc.rotations;
        return ret;
    }

    /**
     * @param column
     * @param cell
     * @return
     */
    public int currentState( int column, int cell ) {
        TL_Column tlc = columns[ column ];
        return tlc.rotations[ cell ];
    }

    /**
     * For number of cells returns true if locked clock wise by parent, else false.</br> First one [0]
     * will always be false since this one is connected to the engine</br> and there fore has no
     * parent.
     */
    public boolean[] currentStateLockedByParentCW( int column ) {
        int[] rotations = currentState( column );
        boolean[] ret = new boolean[ nCells ];
        // first one has no parent
        ret[ 0 ] = false;
        for ( int i = 1; i < nCells; i++ ) {
            if ( rotations[ i ] == rotations[ i - 1 ] ) {
                ret[ i ] = true;
            }
        }
        return ret;
    }

    /**
     * For number of cells returns true if locked counter clock wise by parent, else false.</br> First
     * one [0] will always be false since this one is connected to the engine</br> and there fore has
     * no parent.
     */
    public boolean[] currentStateLockedByParentCCW( int column ) {
        int[] rotations = currentState( column );
        boolean[] ret = new boolean[ nCells ];
        // first one has no parent
        ret[ 0 ] = false;
        for ( int i = 1; i < nCells; i++ ) {
            if ( Math.abs( rotations[ i ] - rotations[ i - 1 ] ) == 180 ) {
                ret[ i ] = true;
            }
        }
        return ret;
    }

    /**
     * For number of cells returns true if it has it's child locked clock wise, else false.</br> Last
     * one [nCells-1] will always be false since this one has no child.
     */
    public boolean[] currentStateChildLockedCW( int column ) {
        int[] rotations = currentState( column );
        boolean[] ret = new boolean[ nCells ];
        for ( int i = 0; i < nCells - 1; i++ ) {
            if ( rotations[ i ] == rotations[ i + 1 ] ) {
                ret[ i ] = true;
            }
        }
        return ret;
    }

    /**
     * For number of cells returns true if it has it's child locked counter clock wise, else
     * false.</br> Last one [nCells-1] will always be false since this one has no child.
     */
    public boolean[] currentStateChildLockedCCW( int column ) {
        int[] rotations = currentState( column );
        boolean[] ret = new boolean[ nCells ];
        for ( int i = 0; i < nCells - 1; i++ ) {
            if ( Math.abs( rotations[ i ] - rotations[ i + 1 ] ) == 180 ) {
                ret[ i ] = true;
            }
        }
        return ret;
    }

    /**
     * @return
     */
    public int[][] targetRotations() {

        int[][] ret = new int[ nColumns ][ nCells ];

        for ( int i = 0; i < nColumns; i++ ) {
            // main.tl.TL_Column tlc = columns[i];
            // ret[i] = tlc.rotations;
            ret[ i ] = targetRotations( i );
        }

        return ret;
    }

    /**
     * @param column
     * @return
     */
    public int[] targetRotations( int column ) {

        int[] ret = new int[ nCells ];
        TL_Column tlc = columns[ column ];
        ret = tlc.targetRotations;
        return ret;
    }

    /**
     *
     */
    public void clearBlack() {
        set( false );
        for ( int i = 0; i < nColumns; i++ ) {
            TL_Column tlc = columns[ i ];
            tlc.targetDirection = CW;
        }
    }

    /**
     *
     */
    public void clearWhite() {
        set( true );
        for ( int i = 0; i < nColumns; i++ ) {
            TL_Column tlc = columns[ i ];
            tlc.targetDirection = CW;
        }
    }

    /**
     * @param states
     */
    public void set( boolean[][] states ) {
        for ( int col = 0; col < states.length; col++ ) {
            set( states[ col ], col );
        }
    }

    /**
     * @param states
     * @param column
     */
    public void set( boolean[] states, int column ) {
        // ready = false;
        TL_Column tlc = columns[ column ];
        tlc.set( states );
        calculateBestRotation( column );
    }

    /**
     * @param state
     * @param column
     * @param cell
     */
    public void set( boolean state, int column, int cell ) {
        // ready = false;
        TL_Column tlc = columns[ column ];
        tlc.set( state, cell );
        calculateBestRotation( column );
    }

    /**
     *
     */
    private void calculateBestRotations() {
        for ( int col = 0; col < nColumns; col++ ) {
            calculateBestRotation( col );
        }
    }

    /**
     * @param column
     */
    private void calculateBestRotation( int column ) {
        // if this method was perfect written you should be able
        // to call it on every update and it would still
        // finish, but Albert Einstein is dead.
        int[] targetRotations = columns[ column ].targetRotations;
        // WOULD BE BETTER (+ COMPLEXER)
        // IF IT WOULD ALSO CHECK HOW MANY
        // ARE IN POSITION YET

        // if the amount on top that has the same target orientation
        // is higher then the amount of cells locked CCW
        // EXCLUSIVE THE TOP ONES then set the target
        // direction to CW to increase speed
        // -----
        // 1) first count how many top ones have the same orientation
        int nTopWithSameOrientation = 0;
        int i = nCells - 1;
        while ( i > 0 ) {
            if ( targetRotations[ i ] == targetRotations[ i - 1 ] ) {
                nTopWithSameOrientation++;
                i--;
            } else {
                break;
            }
        }
        // 2) now count how many or locked CCW exclusive the top ones
        int nLockedCCW = 0;
        boolean[] lockedCCW = currentStateChildLockedCCW( column );
        for ( i = 0; i < nCells - nTopWithSameOrientation; i++ ) {
            if ( lockedCCW[ i ] ) nLockedCCW++;
        }
        // 3) set targetRotation to CW if match condition
        if ( nTopWithSameOrientation > nLockedCCW ) {
            targetDirectionCW( column );
        } else {
            targetDirectionCCW( column );
        }
    }

    /**
     * @param state
     */
    public void set( boolean state ) {
        for ( int i = 0; i < nColumns; i++ ) {
            set( state, i );
        }
    }

    /**
     * @param state
     * @param column
     */
    public void set( boolean state, int column ) {
        // ready = false;
        for ( int i = 0; i < nCells; i++ ) {
            if ( state == false )
                columns[ column ].targetRotations[ i ] = 180;
            else
                columns[ column ].targetRotations[ i ] = 360;
        }
        // speed increase
        targetDirectionCW( column );
    }

    /**
     * @param column
     */
    public void targetDirectionCW( int column ) {
        columns[ column ].targetDirection = CW;
    }

    /**
     * @param column
     */
    public void targetDirectionCCW( int column ) {
        columns[ column ].targetDirection = CCW;
    }

    /**
     * @param s
     */
    public void rotationStep( int s ) {
        this.rotationStep = s;
    }

    /**
     * @return
     */
    public int rotationStep() {
        return rotationStep;
    }

    /**
     * @author marcels
     */
    public class InstructionCalculator {


        int[] currentRotations;

        private InstructionCalculator() {

        }

        /*
        *
        */
        public int[] calculateInstructions( int column ) {
            calculateBestRotation( column );

            ArrayList<Integer> instructions = new ArrayList<Integer>();
            int instructionRotationCount = 0;

            // no copy needed since we don't change the target rotation
            int[] targetRotations = columns[ column ].targetRotations;
            // copy needed since a reference would mess the original up
            currentRotations = new int[ columns[ column ].rotations.length ];
            for ( int i = 0; i < currentRotations.length; i++ ) {
                currentRotations[ i ] = columns[ column ].rotations[ i ];
            }

            int targetDirection = columns[ column ].targetDirection;


            boolean ready = false;
            // update method from main.tl.TL_Column
            // we go backwards cause the cell
            // that is the farest away from the
            // engine should be set in position first
            while ( ready == false ) {
                for ( int i = nCells - 1; i >= 0; i-- ) {
                    if ( targetRotations[ i ] == currentRotations[ i ] ) {
                        if ( i == 0 ) {
                            // when all is ready
                            ready = true;
                            break;
                        }
                        continue;
                    }
                    if ( targetDirection == CW ) {
                        // oneStepCW();
                        rotateCW();
                        instructionRotationCount += rotationStep;
                    } else {
                        // oneStepCCW();
                        rotateCCW();
                        instructionRotationCount -= rotationStep;
                    }
                    // check if it's in position now
                    if ( targetRotations[ i ] == currentRotations[ i ] ) {
                        if ( targetDirection == CW ) {
                            targetDirection = CCW;
                        } else {
                            targetDirection = CW;
                        }

                        instructions.add( instructionRotationCount );
                        instructionRotationCount = 0;
                    }
                    // we break since we rotated a step
                    // the next step is for the next update
                    break;
                }
            }
            int[] ret = new int[ instructions.size() ];
            Iterator<Integer> it = instructions.iterator();
            for ( int i = 0; i < ret.length; i++ ) {
                ret[ i ] = it.next().intValue();
            }

            return ret;
        }


        /**
         *
         */
        void rotateCW() {
            rotateCW( 0 );
        }

        /**
         * @param index
         */
        void rotateCW( int index ) {
            if ( hasChild( index ) && hasChildLockedCW( index ) ) {
                rotateCW( index + 1 );
            }
            currentRotations[ index ] += rotationStep;
            if ( currentRotations[ index ] > 360 ) {
                // System.out.println("rotateCW: "+rotations[index]);
                currentRotations[ index ] -= 360;
                // System.out.println("now: rotateCW: "+rotations[index]);

            }
        }

        /**
         *
         */
        void rotateCCW() {
            rotateCCW( 0 );
        }

        /**
         * @param index
         */
        void rotateCCW( int index ) {
            if ( hasChild( index ) && hasChildLockedCCW( index ) ) {
                rotateCCW( index + 1 );
            }
            currentRotations[ index ] -= rotationStep;
            if ( currentRotations[ index ] < 1 ) {
                // System.out.println("rotateCCW: "+rotations[index]);
                currentRotations[ index ] += 360;
                // System.out.println("now: rotateCCW: "+rotations[index]);

            }
        }

        /**
         *
         */
        boolean hasChildLockedCW( int index ) {
            if ( index == nCells ) {
                System.out.println( "ERROR: do not call hasChildLockedCW(int index) on the latest cell" );
            }
            if ( currentRotations[ index ] == currentRotations[ index + 1 ] ) return true;
            return false;
        }

        /**
         * @param index
         * @return
         */
        boolean hasChildLockedCCW( int index ) {
            if ( index == nCells ) {
                System.out.println( "ERROR: do not call hasChildLockedCCW(int index) on the latest cell" );
            }
            if ( Math.abs( currentRotations[ index ] - currentRotations[ index + 1 ] ) == 180 ) return true;
            return false;
        }

        /**
         * @param index
         * @return
         */
        boolean hasChild( int index ) {
            if ( index >= nCells - 1 ) return false;
            return true;
        }
    }
}
