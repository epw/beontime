package org.willisson.eric.BeOnTime;

import android.app.ListActivity;
import android.app.Activity;
import android.os.Bundle;
import android.app.Dialog;
import android.widget.SimpleAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.view.View;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.SortedMap;
import java.util.HashMap;
import java.util.TreeMap;
import java.io.IOException;

import com.google.api.client.googleapis.extensions.android2.auth.GoogleAccountManager;
import android.accounts.AccountManager;
import android.accounts.Account;
import android.content.Intent;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.google.api.client.googleapis.GoogleHeaders;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.HttpHeaders;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.extensions.android2.AndroidHttp;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.json.JsonHttpRequest;
import com.google.api.client.http.json.JsonHttpRequestInitializer;
import com.google.api.client.json.jackson.JacksonFactory;

import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.CalendarRequest;
import com.google.api.services.calendar.model.Events;
import com.google.api.services.calendar.model.Event;
import android.text.format.Time;
import android.text.format.DateUtils;
import com.google.ical.iter.RecurrenceIterator;
import com.google.ical.iter.RecurrenceIteratorFactory;
import com.google.ical.values.DateValue;
import com.google.ical.values.DateValueImpl;
import java.util.TimeZone;
import java.util.Collections;
import java.util.Comparator;

public class EventChooser extends ListActivity
{
	private String authToken;

	private HttpTransport transport = AndroidHttp.newCompatibleTransport ();
	private Calendar service;

	private static final String PREFS = "org.willisson.eric.BeOnTime.prefs";
	private static final String PREFS_ACCOUNT_NAME = "accountName";
	private static final String PREFS_EVENT_ID = "eventId-";
	private static final int DIALOG_ACCOUNTS = 1;
	private static final int REQUEST_AUTHENTICATE = 0;
	private static final String API_KEY = "AIzaSyC9aawe_NAXAPIrCnY0-NNRPaYd6QNqJ3c";
	private static final int GET_LOCATION_ACTIVITY = 1;
	private static final String LOGTAG = "EventChooser";
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		gotAccount (false);
	}

	@Override
	protected Dialog onCreateDialog (int id) {
		switch (id) {
		case DIALOG_ACCOUNTS:
			AlertDialog.Builder builder = new AlertDialog.Builder
				(this);
			builder.setTitle ("Select a Google account");
			final AccountManager manager = AccountManager.get(this);
			final Account[] accounts = manager.getAccountsByType
				("com.google");
			String[] names = new String[accounts.length];
			for (int i = 0; i < accounts.length; i++) {
				names[i] = accounts[i].name;
			}
			builder.setItems (names,
					  new DialogInterface.OnClickListener
					  () {
						  public void onClick
						  (DialogInterface dialog,
						   int which) {
							  chooseAccount
								  (manager,
								   accounts[which]);
						  }
					  });
			return builder.create();
		}
		return null;
	}

	@Override
	protected void onListItemClick (ListView l, View v, int position,
					long id) {
		Bundle bundle = new Bundle ();
		Map<String, String> event = (Map<String, String>)
			getListView().getItemAtPosition (position);
		for (String key : event.keySet ()) {
			bundle.putString (key, event.get(key));
		}
		Intent intent = new Intent(EventChooser.this,
					   LocationChooser.class);
		intent.putExtra ("org.willisson.eric.BeOnTime.Event", bundle);
		try {
			startActivityForResult (intent, GET_LOCATION_ACTIVITY);
		} catch (RuntimeException e) {
			e.printStackTrace ();
		}
	}

	@Override
	protected void onActivityResult (int requestCode, int resultCode,
					 Intent data) {
		if (requestCode == GET_LOCATION_ACTIVITY) {
			if (resultCode == Activity.RESULT_OK) {
				SharedPreferences settings
					= getSharedPreferences (PREFS, 0);
				SharedPreferences.Editor editor
					= settings.edit ();
				Bundle dataBundle
					= data.getExtras();
				Bundle event
					= dataBundle.getBundle ("org.willisson.eric.BeOnTime.Event");
				int lat = dataBundle.getInt
					("org.willisson.eric.BeOnTime.LAT");
				int lon = dataBundle.getInt
					("org.willisson.eric.BeOnTime.LON");
				editor.putInt (PREFS_EVENT_ID
						   + event.getString("id")
						   + "LAT", lat);
				editor.putInt (PREFS_EVENT_ID
						   + event.getString("id")
						   + "LON", lon);

				editor.commit ();


/*				new AsyncTask<Void, Void, Void>() {
					private Bundle bundle;
					
					@Override protected void onPreExecute () {
					}
					@Override protected void onPostExecute (Void result) {
					}
					@Override protected Void doInBackground
						(Void... params) {
						showCalendarEvents ();
						return null;
					}
					}.execute ();
*/
				showCalendarEvents ();
			}
		}
	}
				
/*	@Override
	public boolean onCreateOptionsMenu (Menu menu) {
		MenuInflater inflater = getMenuInflater ();
		inflater.inflate (R.menu.menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected (MenuItem item) {
		switch (item.getItemId()) {
		case R.id.event_list:
			startActivity (new Intent (MemoryFlipActivity.this,
						   EventChooser.class));
					
			return true;
		default:
			return super.onOptionsItemSelected (item);
		}
	}
*/

	private void gotAccount (boolean tokenExpired) {
		SharedPreferences settings = getSharedPreferences (PREFS, 0);
		String accountName = settings.getString (PREFS_ACCOUNT_NAME,
							 null);
		if (accountName != null) {
			AccountManager manager = AccountManager.get(this);
			Account[] accounts = manager.getAccountsByType
				("com.google");
			for (int i = 0; i < accounts.length; i++) {
				if (accountName.equals (accounts[i].name)) {
					if (tokenExpired) {
						manager.invalidateAuthToken
							("com.google",
							 this.authToken);
					}
					chooseAccount (manager, accounts[i]);
					return;
				}
			}
		}
		showDialog (DIALOG_ACCOUNTS);
	}

	private void chooseAccount (final AccountManager manager,
				    final Account account) {
		SharedPreferences settings = getSharedPreferences (PREFS, 0);
		SharedPreferences.Editor editor = settings.edit ();
		editor.putString (PREFS_ACCOUNT_NAME, account.name);
		editor.commit ();
		new AsyncTask<Void, Void, Void>() {
			private Bundle bundle;

			@Override protected void onPreExecute () {
			}
			@Override protected void onPostExecute (Void result) {
				if (bundle.containsKey
				    (AccountManager.KEY_INTENT)) {
					Intent intent
						= bundle.getParcelable
						(AccountManager.KEY_INTENT);
					int flags = intent.getFlags ();
					flags &= ~Intent.FLAG_ACTIVITY_NEW_TASK;
					intent.setFlags (flags);
					startActivityForResult
						(intent, REQUEST_AUTHENTICATE);
				} else if (bundle.containsKey
					   (AccountManager.KEY_AUTHTOKEN)) {
					loginWithAuthToken
						(bundle.getString
						 (AccountManager.KEY_AUTHTOKEN));
				}
			}
			@Override protected Void doInBackground
				(Void... params) {
				try {
					bundle = manager.getAuthToken (account,
								       "cl",
								       true,
								       null,
								       null)
						.getResult ();
				} catch (android.accounts.OperationCanceledException e) {
				} catch (java.io.IOException e) {
				} catch (android.accounts.AuthenticatorException e) {
				}
				return null;
			}
		}.execute ();
	}

	private class GoogleRequest implements HttpRequestInitializer {
		public void initialize (HttpRequest request) {
			GoogleHeaders headers = new GoogleHeaders ();
			headers.setGoogleLogin (authToken);
			request.getHeaders().putAll (headers);
		}
	}

	private class CalendarRequestInitializer
		implements JsonHttpRequestInitializer {
		public void initialize(JsonHttpRequest request) {
			CalendarRequest calendarRequest
				= (CalendarRequest) request;
			calendarRequest.setPrettyPrint(true);
			calendarRequest.setKey(API_KEY);
		}
	}

	private void loginWithAuthToken (String authToken) {
		JacksonFactory jsonFactory = new JacksonFactory ();

		this.authToken = authToken;

		service = Calendar.builder(transport, jsonFactory)
			.setApplicationName ("BeOnTime-EventChooser/1.0")
			.setHttpRequestInitializer (new GoogleRequest ())
			.setJsonHttpRequestInitializer (new CalendarRequestInitializer ())
			.build ();


		showCalendarEvents ();
	}

	private void getNextRecurrence (Time tTime, List<String> rec,
					Time nowTime) {
		StringBuffer rdata = new StringBuffer ();
		for (int i = 0; i < rec.size(); i++) {
			rdata.append (rec.get(i));
			rdata.append ('\n');
		}
		try {
			RecurrenceIterator recIter = RecurrenceIteratorFactory
				.createRecurrenceIterator
				(rdata.toString(),
				 new DateValueImpl (tTime.year,
						    tTime.month,
						    tTime.monthDay),
				 TimeZone.getDefault(),
				 true);
			recIter.advanceTo (new DateValueImpl(nowTime.year,
							     nowTime.month,
							     nowTime.monthDay));
			if (recIter.hasNext ()) {
				DateValue next = recIter.next();
				Time tTimeRec = new Time ();
				tTime.set (tTime.second, tTime.minute,
					   tTime.hour,
					   next.day (), next.month (),
					   next.year ());
			}
		} catch (java.text.ParseException e) {
		}
	}

	private class CompareEvents implements Comparator {
		public int compare (Object o1, Object o2) {
			Map<String, String> e1 = (Map<String, String>) o1;
			Map<String, String> e2 = (Map<String, String>) o2;

			if (Long.valueOf (e1.get("seconds"))
			    < Long.valueOf (e2.get("seconds"))) {
				return -1;
			}
			if (equals (o1, o2)) {
				return 0;
			}
			return 1;
		}
		public boolean equals (Object o1, Object o2) {
			Map<String, String> e1 = (Map<String, String>) o1;
			Map<String, String> e2 = (Map<String, String>) o2;

			return (Long.valueOf (e1.get("seconds"))
				== Long.valueOf (e2.get("seconds")));
		}
	}
			

	private void showCalendarEvents () {
		int count;
		Time nowTime = new Time();
		long now;
		Events events;
		Calendar.Events.List calEventsList;
		SharedPreferences settings = getSharedPreferences (PREFS, 0);

		try {
			calEventsList = service.events().list("epwtest@gmail.com");
			Log.d (LOGTAG, calEventsList.toString ());
			events = calEventsList.execute ();
		} catch (IOException e) {
			Log.d (LOGTAG, "IOException", e);
			return;
		}

		SortedMap<Map<String,String>, Object> eventTree
			= new TreeMap<Map<String,String>, Object> (new CompareEvents());

		nowTime.setToNow ();
		now = nowTime.toMillis (false);

		count = 0;
		while (true) {
			if (count++ > 10) {
				break;
			}
			for (Event event : events.getItems ()) {
				Map<String, String> eventMap
					= new HashMap<String, String> ();

				String tStr = event.getStart().getDateTime()
					.toString ();
				Time tTime = new Time ();
				long t;

				tTime.parse3339 (tStr);

				List<String> rec = event.getRecurrence ();
				if (rec != null) {
					getNextRecurrence (tTime, rec, nowTime);
				}

				t = tTime.toMillis (false);

				if (now > t) {
					continue;
				}
				if (t - now <= 1000 * 60 * getResources()
				    .getInteger(R.integer.early_minutes)) {
					Intent intent
						= new Intent (EventChooser.this,
							      MemoryFlipActivity.class);
					intent.putExtra ("org.willisson.eric.BeOnTime.TimeRemaining",
							 t - now);
					startActivity (intent);
				}
				
				eventMap.put ("name", event.getSummary());
				eventMap.put ("time",
					      DateUtils.formatDateTime
					      (this, t,
					       DateUtils.FORMAT_SHOW_TIME |
					       DateUtils.FORMAT_NO_NOON |
					       DateUtils.FORMAT_NO_MIDNIGHT |
					       DateUtils.FORMAT_SHOW_DATE |
					       DateUtils.FORMAT_NUMERIC_DATE));
				eventMap.put ("seconds", Long.toString(t));
				eventMap.put ("id", event.getId());

				int lat = settings.getInt (PREFS_EVENT_ID
							+ event.getId()
							+ "LAT", 0);
				if (lat != 0) {
					int lon = settings.getInt (PREFS_EVENT_ID
								+ event.getId()
								+ "LON", 0);
					eventMap.put ("at",
						      "At "
						      + Integer.toString (lat)
						      + ", "
						      + Integer.toString (lon));
				}
									
				eventTree.put (eventMap, null);
			}
			String pageToken = events.getNextPageToken ();
			if (pageToken != null && !pageToken.isEmpty()) {
				try {
					events = service.events()
						.list("primary")
						.setPageToken(pageToken)
						.execute ();
				} catch (IOException e) {
					break;
				}
			} else {
				break;
			}
		}

		List<Map<String, String>> eventList
			= new ArrayList (eventTree.keySet());

		ListAdapter adapter = new SimpleAdapter (
			this,
			eventList,
			R.layout.events,
			new String[] {"name", "time", "at"},
			new int[] {R.id.text1, R.id.text2, R.id.text3});

		setListAdapter (adapter);
	}
}
