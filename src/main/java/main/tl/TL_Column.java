package main.tl;

/**
 * Author: mrzl
 * Date: 06.02.14
 * Time: 15:16
 * Project: main.GoogleAnalyticsInstallation
 */
public class TL_Column {

    int nCells;
    int[] rotations;
    int[] targetRotations;

    int CW = -1;
    int CCW = 1;

    int targetDirection = CW; // start clockwise on start

    TL_Column( int nCells ) {
        this.nCells = nCells;
        rotations = new int[ nCells ];
        targetRotations = new int[ nCells ];
        // show black as default
        for ( int i = 0; i < nCells; i++ ) {
            rotations[ i ] = 180;
            targetRotations[ i ] = 180;
        }
    }

    void set( int[] arrayToSet ) {
        targetRotations = arrayToSet;
        calculateBestDirection();
    }

    void set( boolean[] arrayToSet ) {

        for ( int i = 0; i < arrayToSet.length; i++ ) {
            if ( arrayToSet[ i ] == false ) {
                targetRotations[ i ] = 180;
            } else {
                targetRotations[ i ] = 360;
            }
        }

        calculateBestDirection();

    }

    void set( boolean state, int cell ) {
        if ( state == false ) {
            targetRotations[ cell ] = 180;
        } else {
            targetRotations[ cell ] = 360;
        }
    }

    void calculateBestDirection() {
        // loop backwards to find first difference
        // the idea is to find the quickest way to rotate
        // TODO
        // it can be that it is not locked
        // in that case we should look at the rotation
        // for example if one is rotated at 39 degrees
        // and the next one at 340
        // and the targer is ... then what is the quickest way?
        for ( int i = nCells - 1; i >= 1; i-- ) {
            if ( rotations[ i ] != targetRotations[ i ] ) {

                if ( isLockedByParentCW( i ) ) {
                    targetDirection = CCW;
                } else {
                    targetDirection = CW;
                }
                break;
            }
        }
    }

    boolean update() {
        // we go backwards cause the cell
        // that is the farest away from the
        // engine should be set in position first
        for ( int i = nCells - 1; i >= 0; i-- ) {
            if ( targetRotations[ i ] == rotations[ i ] ) {
                if ( i == 0 ) {
                    // when all is ready we return true
                    return true;
                }
                continue;
            }
            if ( targetDirection == CW ) {
                // oneStepCW();
                rotateCW();
            } else {
                // oneStepCCW();
                rotateCCW();
            }
            // check if it's in position now
            if ( targetRotations[ i ] == rotations[ i ] ) {
                if ( targetDirection == CW ) {
                    targetDirection = CCW;
                } else {
                    targetDirection = CW;
                }
            }
            // we break since we rotated a step
            // the next step is for the next update
            break;
        }
        return false;
    }

    void rotateCW() {
        rotateCW( 0 );
    }

    void rotateCW( int index ) {
        if ( hasChild( index ) && hasChildLockedCW( index ) ) {
            rotateCW( index + 1 );
        }
        rotations[ index ] += TL_Field.rotationStep;
        if ( rotations[ index ] > 360 ) {
            // System.out.println("rotateCW: "+rotations[index]);
            rotations[ index ] -= 360;
            // System.out.println("now: rotateCW: "+rotations[index]);

        }
    }

    void rotateCCW() {
        rotateCCW( 0 );
    }

    void rotateCCW( int index ) {
        if ( hasChild( index ) && hasChildLockedCCW( index ) ) {
            rotateCCW( index + 1 );
        }
        rotations[ index ] -= TL_Field.rotationStep;
        if ( rotations[ index ] < 1 ) {
            // System.out.println("rotateCCW: "+rotations[index]);
            rotations[ index ] += 360;
            // System.out.println("now: rotateCCW: "+rotations[index]);

        }
    }

    boolean hasChild( int index ) {
        if ( index >= nCells - 1 ) {
            return false;
        }
        return true;
    }

    boolean hasParent( int index ) {
        if ( index == 0 ) {
            return false;
        }
        return true;
    }

    boolean hasChildLockedCW( int index ) {
        if ( index == nCells ) {
            System.out.println( "ERROR: do not call hasChildLockedCW(int index) on the latest cell" );
        }
        if ( rotations[ index ] == rotations[ index + 1 ] )
            return true;
        return false;
    }

    boolean hasChildLockedCCW( int index ) {
        if ( index == nCells ) {
            System.out.println( "ERROR: do not call hasChildLockedCCW(int index) on the latest cell" );
        }
        if ( Math.abs( rotations[ index ] - rotations[ index + 1 ] ) == 180 )
            return true;
        return false;
    }

    boolean isLockedByParentCW( int index ) {
        if ( index == 0 ) {
            System.out.println( "ERROR: do not call isLockedByParentCW(int index) on the first cell" );
        }
        if ( rotations[ index ] == rotations[ index - 1 ] )
            return true;
        return false;
    }

    boolean isLockedByParentCCW( int index ) {
        if ( index == 0 ) {
            System.out.println( "ERROR: do not call isLockedByParentCCW(int index) on the first cell" );
        }
        if ( Math.abs( rotations[ index ] - rotations[ index - 1 ] ) == 180 )
            return true;
        return false;
    }

    // it will invert the targetRotation, not the current
    void invert() {
        for ( int i = 0; i < nCells; i++ ) {
            targetRotations[ i ] += 180;
            if ( targetRotations[ i ] > 360 ) {
                targetRotations[ i ] -= 360;
            }
        }
    }
}
