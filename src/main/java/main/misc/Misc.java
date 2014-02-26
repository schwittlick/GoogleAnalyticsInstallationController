package main.misc;

import processing.core.PApplet;
import processing.core.PConstants;

/**
 * Author: mrzl
 * Date: 12.02.14
 * Time: 11:11
 * Project: GoogleAnalyticsInstallation
 */
public class Misc {
    public static void drawCylinder( PApplet p, float w, float h, int sides ) {
        float angle;
        float[] x = new float[ sides + 1 ];
        float[] z = new float[ sides + 1 ];

        // get the x and z position on a circle for all the sides
        for ( int i = 0; i < x.length; i++ ) {
            angle = PConstants.TWO_PI / ( sides ) * i;
            x[ i ] = PApplet.sin( angle ) * w;
            z[ i ] = PApplet.cos( angle ) * w;
        }

        // draw the top of the drawCylinder
        p.beginShape( PApplet.TRIANGLE_FAN );

        p.vertex( 0, -h / 2, 0 );

        for ( int i = 0; i < x.length; i++ ) {
            p.vertex( x[ i ], -h / 2, z[ i ] );
        }

        p.endShape();

        // draw the center of the drawCylinder
        p.beginShape( PApplet.QUAD_STRIP );

        for ( int i = 0; i < x.length; i++ ) {
            p.vertex( x[ i ], -h / 2, z[ i ] );
            p.vertex( x[ i ], h / 2, z[ i ] );
        }

        p.endShape();

        // draw the bottom of the drawCylinder
        p.beginShape( PApplet.TRIANGLE_FAN );

        p.vertex( 0, h / 2, 0 );

        for ( int i = 0; i < x.length; i++ ) {
            p.vertex( x[ i ], h / 2, z[ i ] );
        }

        p.endShape();
    }
}
