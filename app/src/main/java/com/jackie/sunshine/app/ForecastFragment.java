/*
 *             $$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$
 *             $                                                   $
 *             $                       _oo0oo_                     $
 *             $                      o8888888o                    $
 *             $                      88" . "88                    $
 *             $                      (| -_- |)                    $
 *             $                      0\  =  /0                    $
 *             $                    ___/`-_-'\___                  $
 *             $                  .' \\|     |$ '.                 $
 *             $                 / \\|||  :  |||$ \                $
 *             $                / _||||| -:- |||||- \              $
 *             $               |   | \\\  -  $/ |   |              $
 *             $               | \_|  ''\- -/''  |_/ |             $
 *             $               \  .-\__  '-'  ___/-. /             $
 *             $             ___'. .'  /-_._-\  `. .'___           $
 *             $          ."" '<  `.___\_<|>_/___.' >' "".         $
 *             $         | | :  `- \`.;`\ _ /`;.`/ - ` : | |       $
 *             $         \  \ `_.   \_ __\ /__ _/   .-` /  /       $
 *             $     =====`-.____`.___ \_____/___.-`___.-'=====    $
 *             $                       `=-_-='                     $
 *             $     ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~   $
 *             $                                                   $
 *             $          Buddha Bless         Never Bug           $
 *             $                                                   $
 *             $$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$
 *
 *  Copyright (C) 2016 The Android Open Source Project
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.jackie.sunshine.app;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.jackie.sunshine.app.data.WeatherContract;

/**
 * Created 16/11/21.
 *
 * @author Jackie
 * @version 1.0
 */

public class ForecastFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String TAG = "ForecastFragment";

    private static final int FORECAST_LOADER_ID = 0x3e8;

    private static final String[] FORECAST_COLUMNS = {
            //In this case the id needs to be fully qualified with a table name, since
            //the content provider joins the location & weather tables in the background
            // (both have an _id column)
            // On one hand, that's annoying.  On the other, You can search the weather table
            // using the location set by the user, which is only in the Location table.
            // So the convenience is worth it.
            WeatherContract.WeatherEntry.TABLE_NAME + "." + WeatherContract.WeatherEntry._ID,
            WeatherContract.WeatherEntry.COLUMN_DATE,
            WeatherContract.WeatherEntry.COLUMN_SHORT_DESC,
            WeatherContract.WeatherEntry.COLUMN_MAX_TEMP,
            WeatherContract.WeatherEntry.COLUMN_MIN_TEMP,
            WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING,
            WeatherContract.WeatherEntry.COLUMN_WEATHER_ID,
            WeatherContract.LocationEntry.COLUMN_COORD_LAT,
            WeatherContract.LocationEntry.COLUMN_COORD_LONG
    };

    // These indices are tied to FORECAST_COLUMNS.
    //If FORECAST_COLUMNS changes, these must change.
    static final int COL_WEATHER_ID             = 0;
    static final int COL_WEATHER_DATE           = 1;
    static final int COL_WEATHER_DESC           = 2;
    static final int COL_WEATHER_MAX_TEMP       = 3;
    static final int COL_WEATHER_MIN_TEMP       = 4;
    static final int COL_LOCATION_SETTING       = 5;
    static final int COL_WEATHER_CONDITION_ID   = 6;
    static final int COL_COORD_LAT              = 7;
    static final int COL_COORD_LONG             = 8;
    public static final String EXTRA_POSITION   = "position";

    private ForecastAdapter mForecastAdapter;
    private ListView mListView;

    private int mPosition = ListView.INVALID_POSITION;

    private Callback callback;

    public ForecastFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Add this line in order for this fragment to handle menu events.
        setHasOptionsMenu(true);
    }

    /**
     * @param inflater
     * @param container
     * @param savedInstanceState
     * @return
     */
    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle
            savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        // Get a reference to the ListView, and attach this adapter to it.
        mListView = (ListView) rootView.findViewById(R.id.list_forecast);
        mForecastAdapter = new ForecastAdapter(getActivity(), null, 0);

        mListView.setAdapter(mForecastAdapter);

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Cursor cursor = (Cursor) parent.getItemAtPosition(position);

                if (cursor != null) {
                    String locationSetting = Utility.getPreferredLocation(getContext());
                    Uri data = WeatherContract.WeatherEntry.buildWeatherLocationWithDate
                            (locationSetting, cursor.getLong(COL_WEATHER_DATE));

                    callback.onItemSelected(data);

                    Intent intent = new Intent(getActivity(), DetailActivity.class);
                    intent.setData(data);
                    startActivity(intent);
                }
                mPosition = position;

            }
        });

        if (savedInstanceState != null && savedInstanceState.containsKey(EXTRA_POSITION)) {
            mPosition = savedInstanceState.getInt(EXTRA_POSITION);
        }

        return rootView;
    }

    public void setCallback(Callback callback) {
        this.callback = callback;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        getLoaderManager().initLoader(FORECAST_LOADER_ID, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onStart() {
        super.onStart();
        updateWeather();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        if (mPosition != ListView.INVALID_POSITION) {
            outState.putInt(EXTRA_POSITION, mPosition);
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.forecast_fragment, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_refresh:
                Log.d(TAG, "onOptionsItemSelected: selected action is refresh");
                updateWeather();
                return true;

            default:
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private void updateWeather() {
        // SunshineSyncAdapter.syncImmediately(getContext());
        // String location = Utility.getPreferredLocation(getActivity());

        // Intent intent = new Intent(getContext(), SunshineService.AlarmReceiver.class);
        // intent.putExtra(SunshineService.LOCATION_QUERY_EXTRA, location);
        // PendingIntent pendingIntent = PendingIntent.getBroadcast(getContext(), 0, intent,
        //         PendingIntent.FLAG_ONE_SHOT);

        // AlarmManager am = (AlarmManager) getActivity().getSystemService(Context.ALARM_SERVICE);
        // am.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 5000, pendingIntent);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        String locationSetting = Utility.getPreferredLocation(getActivity());

        // Sort order: Ascending, by date
        String sortOrder = WeatherContract.WeatherEntry.COLUMN_DATE + " ASC";
        Uri weatherForLocationUri = WeatherContract.WeatherEntry
                .buildWeatherLocationWithStartDate(locationSetting, System.currentTimeMillis());
        return new CursorLoader(getContext(), weatherForLocationUri, FORECAST_COLUMNS, null,
                null, sortOrder);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mForecastAdapter.swapCursor(data);
        if (mPosition != ListView.INVALID_POSITION) {
            mListView.smoothScrollToPosition(mPosition);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mForecastAdapter.swapCursor(null);
    }

    public void onLocationChange() {
        updateWeather();
        getLoaderManager().restartLoader(FORECAST_LOADER_ID, null, this);
    }

    public void setUseTodayLayout(boolean useTodayLayout) {
        mForecastAdapter.setUserTodayLayout(useTodayLayout);
    }

    /**
     * A callback interface that all activities containing this fragment must
     * implement. This mechanism allows activities to be notified of item
     * selections.
     */
    public interface Callback {
        /**
         * DetailFragmentCallback for when an item has been selected.
         */
        void onItemSelected(Uri dateUri);
    }
}
