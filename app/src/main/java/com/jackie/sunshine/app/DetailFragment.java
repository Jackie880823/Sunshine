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
 *  Copyright (C) 2014 The Android Open Source Project
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


import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.jackie.sunshine.app.data.WeatherContract;


/**
 * A simple {@link Fragment} subclass.
 */
public class DetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final String TAG = "DetailFragment";
    public static final String DETAIL_URI = "URI";

    private static final String FORECAST_SHARE_HASHTAG = " #SunshineApp";
    public static final int DETAIL_LOAD_ID = 0x3e9;


    private static final String[] DETAIL_COLUMS = {
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
            WeatherContract.WeatherEntry.COLUMN_HUMIDITY,
            WeatherContract.WeatherEntry.COLUMN_PRESSURE,
            WeatherContract.WeatherEntry.COLUMN_WIND_SPEED,
            WeatherContract.WeatherEntry.COLUMN_DEGREES,
            WeatherContract.WeatherEntry.COLUMN_WEATHER_ID,
            // This works because the WeatherProvider returns location data joined with
            // weather data, even though they're stored in two different tables.
            WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING
    };

    public static final int COL_WEATHER_ID = 0;
    public static final int COL_WEATHER_DATE = 1;
    public static final int COL_WEATHER_DESC = 2;
    public static final int COL_WEATHER_MAX_TEMP = 3;
    public static final int COL_WEATHER_MIN_TEMP = 4;
    public static final int COL_WEATHER_HUMIDITY = 5;
    public static final int COL_WEATHER_PRESSURE = 6;
    public static final int COL_WEATHER_WIND_SPEED = 7;
    public static final int COL_WEATHER_DEGREES = 8;
    public static final int COL_WEATHER_CONDITION_ID = 9;

    private ImageView mIconView;
    private TextView mTvFriendlyDate;
    private TextView mTvDate;
    private TextView mTvDescription;
    private TextView mTvHighTemp;
    private TextView mTvLowTemp;
    private TextView mTvHumidityView;
    private TextView mTvWindView;
    private TextView mTvPressure;

    private String mForecastStr;
    private ShareActionProvider mShareAction;
    private Uri mUri;

    public DetailFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);
    }

    public void onLocationChanged(String newLocation) {

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle
            savedInstanceState) {
        Bundle arguments = getArguments();
        if (arguments != null) {
            mUri = arguments.getParcelable(DetailFragment.DETAIL_URI);
        }

        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);

        mIconView = (ImageView) rootView.findViewById(R.id.detail_icon);
        mTvDate = (TextView) rootView.findViewById(R.id.detail_date_text);
        mTvFriendlyDate = (TextView) rootView.findViewById(R.id.detail_day_text);
        mTvDescription = (TextView) rootView.findViewById(R.id.detail_forecast_text);
        mTvHighTemp = (TextView) rootView.findViewById(R.id.detail_high_text);
        mTvLowTemp = (TextView) rootView.findViewById(R.id.detail_low_text);
        mTvHumidityView = (TextView) rootView.findViewById(R.id.detail_humidity_text);
        mTvWindView = (TextView) rootView.findViewById(R.id.detail_wind_text);
        mTvPressure = (TextView) rootView.findViewById(R.id.detail_pressure_text);
        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        getLoaderManager().initLoader(DETAIL_LOAD_ID, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.detail_fragment, menu);

        MenuItem shareMenu = menu.findItem(R.id.action_share);

        mShareAction = (ShareActionProvider) MenuItemCompat.getActionProvider(shareMenu);

        if (!TextUtils.isEmpty(mForecastStr)) {
            mShareAction.setShareIntent(createShareIntent());
        }
    }

    private Intent createShareIntent() {
        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        shareIntent.putExtra(Intent.EXTRA_TEXT, mForecastStr + FORECAST_SHARE_HASHTAG);
        shareIntent.setType("text/plain");
        return shareIntent;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if (mUri != null) {
            return new CursorLoader(getContext(), mUri, DETAIL_COLUMS, null, null, null);
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        Log.d(TAG, "onLoadFinished: ");
        if (data == null || !data.moveToFirst()) return;

        Context context = getContext();

        int weatherId = data.getInt(COL_WEATHER_CONDITION_ID);
        mIconView.setImageResource(Utility.getArtResourceForWeatherCondition(weatherId));

        // Read date from cursor and update views for day of week and date
        long date = data.getLong(COL_WEATHER_DATE);
        String friendlyDateStr = Utility.getDayName(context, date);
        String dateStr = Utility.getFormattedMonthDay(context, date);
        mTvDate.setText(dateStr);
        mTvFriendlyDate.setText(friendlyDateStr);

        // Read description from cursor and update view
        String weatherDescription = data.getString(COL_WEATHER_DESC);
        mTvDescription.setText(weatherDescription);
        mIconView.setContentDescription(weatherDescription);

        boolean isMetric = Utility.isMetric(context);

        // Read high temperature from cursor and update view
        String high = Utility.formatTemperature(context, data.getDouble(COL_WEATHER_MAX_TEMP));
        mTvHighTemp.setText(high);

        // Read low temperature from cursor and update view
        String low = Utility.formatTemperature(context, data.getDouble(COL_WEATHER_MIN_TEMP));
        mTvLowTemp.setText(low);

        // Read humidity from cursor and update view
        float humidity = data.getFloat(COL_WEATHER_HUMIDITY);
        mTvHumidityView.setText(getActivity().getString(R.string.format_humidity, humidity));

        // Read wind speed and direction from cursor and update view
        float windSpeedStr = data.getFloat(COL_WEATHER_WIND_SPEED);
        float windDirStr = data.getFloat(COL_WEATHER_DEGREES);
        mTvWindView.setText(Utility.getFormattedWind(getActivity(), windSpeedStr, windDirStr));


        // Read pressure from cursor and update view
        float pressure = data.getFloat(COL_WEATHER_PRESSURE);
        mTvPressure.setText(getString(R.string.format_pressure, pressure));

        mForecastStr = String.format("%s - %s - %s/%s", dateStr, weatherDescription, high, low);

        if (mShareAction != null) {
            mShareAction.setShareIntent(createShareIntent());
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) { }
}
