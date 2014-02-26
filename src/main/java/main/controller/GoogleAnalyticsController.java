package main.controller;

import com.google.gdata.client.analytics.AnalyticsService;
import com.google.gdata.client.analytics.DataQuery;
import com.google.gdata.data.analytics.DataEntry;
import com.google.gdata.data.analytics.DataFeed;
import com.google.gdata.util.AuthenticationException;
import com.google.gdata.util.ServiceException;
import processing.core.PApplet;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * Author: mrzl
 * Date: 06.02.14
 * Time: 15:14
 * Project: main.GoogleAnalyticsInstallation
 */
public class GoogleAnalyticsController extends Thread {
    private PApplet p;
    private AnalyticsService analyticsService;
    private DataQuery query;
    private DataFeed feed;
    private static final String CLIENT_USERNAME = "marzzzel@gmail.com";
    private static final String CLIENT_PASS = "***";
    private static final String TABLE_ID = "ga:32503195";
    private SimpleDateFormat dateFormat;
    private GregorianCalendar calendar;

    private int numberOfDaysOfStatistics;
    private ArrayList<Integer> visitorStatistics;

    public boolean running = false;
    int updateIntervallMillis = 6000;
    int millisLastUpdate;

    public GoogleAnalyticsController( PApplet parent, int daysOfStatistics ) {
        this.p = parent;
        initAnalyticsService();
        dateFormat = new SimpleDateFormat( "yyyy-MM-dd" );
        calendar = new GregorianCalendar();

        numberOfDaysOfStatistics = daysOfStatistics;
        // visitorStatistics = getVisitorsForLastDays(numberOfDaysOfStatistics);

    }

    public void start() {
        running = true;
        System.out.println( "started googleanalytics controller" );
        super.start();
    }

    public void run() {
        while ( running ) {
            update();
            try {
                sleep( 300000 ); // 5 min
            } catch ( Exception e ) {
                System.out.println( "Googleanalytics thread excepotion" );
            }
        }
    }

    private void initAnalyticsService() {
        analyticsService = new AnalyticsService( "analyticsService" );
        try {
            analyticsService.setUserCredentials( CLIENT_USERNAME, CLIENT_PASS );
        } catch ( AuthenticationException e ) {
            e.printStackTrace();
        }
    }

    public void update() {
        System.out.println( "main.controller.GoogleAnalyticsController statistics updated." );
        System.out.println( "Framecount: " + p.frameCount );
        visitorStatistics = getVisitorsForLastDays( numberOfDaysOfStatistics );
        for ( Integer i : visitorStatistics ) {
            System.out.println( i );
        }
    }

    public void setNumberOfDaysStatistic( int nr ) {
        this.numberOfDaysOfStatistics = nr;
    }

    public ArrayList<Integer> getVisitorStatistics() {
        return visitorStatistics;
    }

    private int getVisitors( Date date ) {
        int visits = 0;
        try {
            query = new DataQuery( new URL( "https://www.google.com/analytics/feeds/data" ) );
        } catch ( MalformedURLException e ) {
            e.printStackTrace();
        }
        query.setStartDate( dateFormat.format( date ).toString() );
        query.setEndDate( dateFormat.format( date ).toString() );
        query.setDimensions( "ga:visitCount" );
        query.setMetrics( "ga:visitors" );
        // query.setSort("");
        query.setIds( TABLE_ID );
        try {
            feed = analyticsService.getFeed( query.getUrl(), DataFeed.class );
        } catch ( IOException e ) {
            e.printStackTrace();
        } catch ( ServiceException e ) {
            e.printStackTrace();
        }
        for ( DataEntry entry : feed.getEntries() ) {
            visits += Integer.parseInt( entry.stringValueOf( "ga:visitors" ) );
        }
        return visits;
    }

    /**
     * This returns an arraylist containing integers which represent the visitors of each day The size
     * of the arraylist depends on the numberOfDays specified and its sorted so: .get(0) -> newest
     * date specified .get(list.size()) -> oldest date specified
     */
    private ArrayList<Integer> getVisitorsForLastDays( int numberOfDays ) {
        ArrayList<Integer> visitorsLastDays = new ArrayList<Integer>();
        for ( int i = 0; i < numberOfDays; i++ ) {
            int visitsPerDay;
            try {
                visitsPerDay = getVisitors( getDateXDaysFromNow( -i ) );
            } catch ( NullPointerException e ) {
                System.out.println( e );
                System.out.println( "Goole Exception- the limit might be exceeded" );
                visitsPerDay = 0;
            }
            visitorsLastDays.add( new Integer( visitsPerDay ) );
        }
        return visitorsLastDays;
    }

    /**
     * this method is needed because the analytics api needs proper Date datatypes in order to get
     * data and we need to know the dates by how many days they are away from today.
     */
    private Date getDateXDaysFromNow( int daysFromNow ) {
        calendar.add( Calendar.DAY_OF_MONTH, daysFromNow ); // add x days
        Date date = calendar.getTime();
        calendar.add( Calendar.DAY_OF_MONTH, -daysFromNow ); // reset calender
        return date;
    }
}
