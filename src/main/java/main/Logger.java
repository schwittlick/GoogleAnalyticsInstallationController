package main;

import java.util.ArrayList;

/**
 * Author: mrzl
 * Date: 12.02.14
 * Time: 10:45
 * Project: GoogleAnalyticsInstallation
 */
public class Logger {
    private static ArrayList<String> logger = new ArrayList<>();

    public static ArrayList<String> getLogger() {
        return Logger.logger;
    }

    public static void log( String message ) {
        logger.add( message );

        if ( logger.size() > 20 ) {
            logger.remove( 0 );
        }
    }
}