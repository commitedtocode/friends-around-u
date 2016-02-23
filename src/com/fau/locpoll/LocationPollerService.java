package com.fau.locpoll;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;

/**
 * Service providing the guts of the location polling
 * engine. Uses a WakeLock to ensure the CPU stays on while
 * the location lookup is going on. Handles both successful
 * and timeout conditions.
 * 
 * Those wishing to leverage this service should do so via
 * the LocationPoller class.
 */
public class LocationPollerService extends Service {
  private static final String LOCK_NAME_STATIC = "com.commonsware.cwac.locpoll.LocationPoller";
  private static volatile PowerManager.WakeLock lockStatic;
  private LocationManager locationManager;


  //private Notification notification;
  //private NotificationManager nm;

  /**
   * Lazy-initializes the WakeLock when we first use it. We
   * use a partial WakeLock since we only need the CPU on,
   * not the screen.
   */
	synchronized private static PowerManager.WakeLock getLock(Context context) {
		if (lockStatic == null) {
			PowerManager mgr = (PowerManager) context.getApplicationContext().getSystemService(Context.POWER_SERVICE);
			lockStatic = mgr.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,LOCK_NAME_STATIC);
			lockStatic.setReferenceCounted(true);
		}
		return (lockStatic);
	}

  /**
   * Called by LocationPoller to trigger a poll for the
   * location. Acquires the WakeLock, then starts the
   * service using the supplied Intent (setting the
   * component so routing always goes to the service).
	 */
	public static void requestLocation(Context context, Intent intent) throws InvalidParameterException {

		assertValidParameters(intent);
		getLock(context).acquire();
		intent.setClass(context, LocationPollerService.class);
		context.startService(intent);
	}

	public static void assertValidParameters(Intent intent)throws InvalidParameterException {
		LocationPollerParameter parameters = getParametersFromIntent(intent);
		if (parameters.getProviders() == null || parameters.getProviders().length == 0) {
			throw new InvalidParameterException("at least one provider must be set");
		}
	}
    
	public static LocationPollerParameter getParametersFromIntent(Intent intent)throws InvalidParameterException {
		LocationPollerParameter parameter = new LocationPollerParameter(intent.getExtras());
		return parameter;
	}

  /**
   * Obtain the LocationManager on startup
   */
  @Override
  public void onCreate() {
    locationManager=(LocationManager)getSystemService(LOCATION_SERVICE);
    //nm=(NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
    //showNotification();
    //android.os.Debug.waitForDebugger();
  }
   @Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		Log.d("location service","stopped");
		//nm.cancel(R.string.service_started);
	}
  /*private void showNotification() {
		// TODO Auto-generated method stub
		CharSequence text="synchronization in process";
		notification=new Notification(android.R.drawable.ic_menu_compass, text,System.currentTimeMillis());
		Intent main=new Intent(this, Main.class);
		main.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
		PendingIntent contentIntent = PendingIntent.getActivity(this,0,main, PendingIntent.FLAG_UPDATE_CURRENT);
		notification.setLatestEventInfo(this, "Location Synch", text, contentIntent);
		notification.flags|= Notification.FLAG_ONGOING_EVENT | Notification.FLAG_FOREGROUND_SERVICE | Notification.FLAG_NO_CLEAR;
		startForeground(R.string.service_started, notification);
	}*/
  /**
   * No-op implementation as required by superclass
   */
  @Override
  public IBinder onBind(Intent i) {
    return(null);
  }
  
  /**
   * Validates the required extras (EXTRA_PROVIDER and
   * EXTRA_INTENT). If valid, updates the Intent to be
   * broadcast with the application's own package (required
   * to keep the broadcast within this application, so we do
   * not leak security information). Then, forks a
   * PollerThread to do the actual location lookup.
   * 
   * @return START_REDELIVER_INTENT to ensure we get the
   *         last request again
   */
  @Override
  public int onStartCommand(Intent intent, int flags, int startId) {
		LocationPollerParameter parameters = getParametersFromIntent(intent);
		PollerThread pollerThread = new PollerThread(getLock(this), locationManager, parameters);
		pollerThread.start();
		return (START_REDELIVER_INTENT);
	}

	/**
	 * A WakefulThread subclass that knows how to look up the current location,
	 * plus handle the timeout scenario.
	 */
	private class PollerThread extends WakefulThread {
		private LocationManager locationManager;
		private Handler handler = new Handler();
		private LocationPollerParameter locationPollerParameter;
		private int currentLocationProviderIndex;
		
		private Runnable onTimeout = new Runnable() {
			public void run() {
				locationManager.removeUpdates(mListener);
				if (isTriedAllProviders()) {
					broadCastFailureMessage();
					quit();
				} else {
					currentLocationProviderIndex++;
					tryNextProvider();
				}
			}
		};

    private LocationListener mListener = new LocationListener() {
      /**
       * If we get a fix, get rid of the timeout condition,
       * then attach the location as an extra
       * (EXTRA_LOCATION) on the Intent, broadcast it, then
       * exit the polling loop so the thread terminates.
       */
      public void onLocationChanged(Location location) {
        handler.removeCallbacks(onTimeout);
        Intent toBroadcast = createIntentToBroadcastOnCompletion();
        toBroadcast.putExtra(LocationPollerResult.LOCATION_KEY, location);
        sendBroadcast(toBroadcast);
        quit();
      }

      public void onProviderDisabled(String provider) {
        // required for interface, not used
      }

      public void onProviderEnabled(String provider) {
        // required for interface, not used
      }

      public void onStatusChanged(String provider, int status,
                                  Bundle extras) {
        // required for interface, not used
      }
    };
  
    /**
     * Constructor.
     * 
     * @param lock
     *          Already-locked WakeLock
     * @param locationManager
     *          LocationManager for doing the location
     *          lookup
     * @param provider
     *          name of the location provider to use
     * @param intentTemplate
     *          Intent to be broadcast when location found
     *          or timeout occurs
     */
		PollerThread(PowerManager.WakeLock lock,
				LocationManager locationManager,
				LocationPollerParameter locationPollerParameter) {
			super(lock, "LocationPoller-PollerThread");

			this.locationManager = locationManager;
			this.locationPollerParameter = locationPollerParameter;
    }

    /**
     * Called before the Handler loop begins. Registers a
     * timeout, so we do not wait forever for a location.
     * When a timeout occurs, broadcast an Intent containing
     * an error extra, then terminate the thread. Also,
     * requests a location update from the LocationManager.
     */
    @Override
    protected void onPreExecute() {
			tryNextProvider();
		}

		private void tryNextProvider() {
			handler.postDelayed(onTimeout, locationPollerParameter.getTimeout());
			locationManager.requestLocationUpdates(getCurrentProvider(), 0, 0, mListener);
		}

		private void broadCastFailureMessage() {
			Intent toBroadcast = createIntentToBroadcastOnCompletion();
			toBroadcast.putExtra(LocationPollerResult.ERROR_KEY, "Timeout!");
			Location location = locationManager.getLastKnownLocation(getCurrentProvider());
			toBroadcast.putExtra(LocationPollerResult.LASTKNOWN_LOCATION_KEY, location);
			sendBroadcast(toBroadcast);
		}

		private String getCurrentProvider() {
			String currentProvider = locationPollerParameter.getProviders()[currentLocationProviderIndex];
			return currentProvider;
		}

		private boolean isTriedAllProviders() {
			return currentLocationProviderIndex == locationPollerParameter
					.getProviders().length - 1;
		}
		
		private Intent createIntentToBroadcastOnCompletion() {
	        Intent toBroadcast = new Intent(locationPollerParameter.getIntentToBroadcastOnCompletion());
	        toBroadcast.setPackage(getPackageName());
	        return toBroadcast;
		}

    /**
     * Called when the Handler loop ends. Removes the
     * location listener.
     */
    @Override
    protected void onPostExecute() {
      locationManager.removeUpdates(mListener);
      super.onPostExecute();
    }

    /**
     * Called when the WakeLock is completely unlocked.
     * Stops the service, so everything shuts down.
     */
    @Override
    protected void onUnlocked() {
      locationManager=null;
      stopSelf();
    }
  }
}
