package main;

import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PShape;
import toxi.geom.mesh.Face;
import toxi.geom.mesh.STLReader;
import toxi.geom.mesh.TriangleMesh;
import toxi.geom.mesh.WETriangleMesh;

/**
 * Author: mrzl
 * Date: 12.02.14
 * Time: 10:32
 * Project: GoogleAnalyticsInstallation
 */
public class CellShape {
    private PApplet parent;
    private PShape cellShape;

    public CellShape( PApplet p, String fileName ) {
        this.parent = p;

        TriangleMesh mesh =
                ( TriangleMesh ) new STLReader().loadBinary(
                        p.sketchPath( fileName ),
                        STLReader.TRIANGLEMESH );

        WETriangleMesh cell = new WETriangleMesh();
        cell.addMesh( mesh );
        cell.rotateX( PApplet.radians( 90 ) );
        cell.scale( 1.1f );

        cellShape = meshToPShape( cell );
    }

    public void render() {
        parent.shape( cellShape, 0, 0 );
    }

    private PShape meshToPShape( WETriangleMesh m ) {
        PShape shp = parent.createShape();
        shp.beginShape( PConstants.TRIANGLE );
        shp.fill( 90 );
        shp.noStroke();
        for ( Face f : m.faces ) {
            shp.vertex( f.a.x, f.a.y, f.a.z );
            shp.vertex( f.b.x, f.b.y, f.b.z );
            shp.vertex( f.c.x, f.c.y, f.c.z );

        }
        shp.endShape();
        return shp;
    }
}
