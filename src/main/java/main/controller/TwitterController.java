package main.controller;

import processing.core.PApplet;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.conf.ConfigurationBuilder;

import java.util.List;

/**
 * Author: mrzl
 * Date: 06.02.14
 * Time: 15:14
 * Project: main.GoogleAnalyticsInstallation
 */
public class TwitterController extends Thread {
    private PApplet p;
    private Twitter twitter;

    private String wakeupCall = "wake up";
    private String goToSleep = "sleep";
    private String supevisorTwitterAccount = "MarselC";

    private int updateIntervallInMilliseconds;
    private boolean isSleeping;

    public boolean running = false;

    public TwitterController( PApplet parent ) {
        this.p = parent;

        ConfigurationBuilder cb = new ConfigurationBuilder();
        cb.setOAuthConsumerKey( "awsO0TpzLAVsdYzlm1UKOg" );
        cb.setOAuthConsumerSecret( "XWsf0CyjGSRveIlniJn9CEEjUcV8E7RcxSTupbDmdqE" );
        cb.setOAuthAccessToken( "1010943918-YsQZPiHyEt3QhdNoYVAR6AElUzOkv1hsjr0Od5u" );
        cb.setOAuthAccessTokenSecret( "gqXtz3AHEZ4UywHBSZyMH6PqYYPcylhOStFg5cFbeI" );
        twitter = new TwitterFactory( cb.build() ).getInstance();

        isSleeping = false;
        // 5 * 60 * 1000
        updateIntervallInMilliseconds = 300000;

    }

    public void start() {
        running = true;
        super.start();
        System.out.println( "started twittercontroller" );
    }

    public void run() {
        System.out.println( "running: " + p.frameCount );
        while ( running ) {
            update();
            try {
                sleep( updateIntervallInMilliseconds );
            } catch ( Exception e ) {
                System.out.println( "whut" );
            }
        }
    }

    public void update() {
        System.out.println( "Twitter update loaded. At frameCount: " + p.frameCount );
        loadInstructionsFromLastTwitterMention();
    }

    public void loadInstructionsFromLastTwitterMention() {
        List<Status> mentions = null;
        try {
            mentions = twitter.getMentionsTimeline();
        } catch ( TwitterException e ) {
            e.printStackTrace();
        }
        try {
            // mentions.get(0) is the most recent mention
            if ( mentions.get( 0 ).getUser().getScreenName().equals( supevisorTwitterAccount ) ) {
                String fullTwitterMessage = mentions.get( 0 ).getText();
                if ( fullTwitterMessage.contains( goToSleep )
                        || fullTwitterMessage.contains( goToSleep.toUpperCase() ) ) {
                    setWasSentToSleep( true );
                    System.out.println( getName() + " sent the arduino to sleep." );
                } else if ( fullTwitterMessage.contains( wakeupCall )
                        || fullTwitterMessage.contains( wakeupCall.toUpperCase() ) ) {
                    setWasSentToSleep( false );
                    System.out.println( getName() + " woke the arduino up." );
                } else {
                    // dont do anything- the twitter message was either misstyped or nonsense. keeping the
                    // installation running
                }
            }
        } catch ( NullPointerException e ) {
            System.out.println( "limit exceeded" );
        }
    }

    /**
     * this changes the twitter account to which the installation is listening
     */
    public void changeSupervisorTwitterName( String twitterName ) {
        this.supevisorTwitterAccount = twitterName;
    }

    private void setWasSentToSleep( boolean bool ) {
        this.isSleeping = bool;
    }

    public boolean wasSentToSleep() {
        return isSleeping;
    }
}
