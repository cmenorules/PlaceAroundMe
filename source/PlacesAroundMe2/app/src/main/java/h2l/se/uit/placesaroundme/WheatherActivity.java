package h2l.se.uit.placesaroundme;

import android.annotation.SuppressLint;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class WheatherActivity extends AppCompatActivity {
    /**
     * Whether or not the system UI should be auto-hidden after
     * {@link #AUTO_HIDE_DELAY_MILLIS} milliseconds.
     */
    private static final boolean AUTO_HIDE = true;

    /**
     * If {@link #AUTO_HIDE} is set, the number of milliseconds to wait after
     * user interaction before hiding the system UI.
     */


    /**
     * Some older devices needs a small delay between UI widget updates
     * and a change of the status and navigation bar.
     * <p>
     * /**
     * Touch listener to use for in-layout UI controls to delay hiding the
     * system UI. This is to prevent the jarring behavior of controls going away
     * while interacting with activity UI.
     */

    private ImageView img_back = null;
    private TextView txt_temp = null;
    private TextView txt_min = null;
    private TextView txt_max = null;
    private TextView txt_wind = null;
    private TextView txt_tip = null;
    private TextView txt_humatily = null;
    private TextView txt_description = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_wheather);

        img_back = (ImageView) findViewById(R.id.img_back);
        img_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });


        txt_temp = (TextView) findViewById(R.id.txt_temperture);
        txt_min = (TextView) findViewById(R.id.txt_temperture_min);
        txt_max = (TextView) findViewById(R.id.txt_temperture_max);
        txt_wind = (TextView) findViewById(R.id.txt_wind);
        txt_tip = (TextView) findViewById(R.id.txt_advice);
        txt_humatily = (TextView) findViewById(R.id.txt_humidity);
        txt_description = (TextView) findViewById(R.id.txt_description);

        try {
            double _long = getIntent().getDoubleExtra("LONG", 0);
            double _lat = getIntent().getDoubleExtra("LAT", 0);
            GetFullWeatherData task = new GetFullWeatherData(_lat, _long);
            task.execute().get();
            WeatherDTO dto = task.getwData();


            //android:text="Humidity:"

            //android:text="Wind:"
            //android:text="Description:"

            //android:text="Tips: Take your umbrella"


            txt_temp.setText("Temperature: " + dto.getTemp());
            txt_min.setText("Min temp :" + dto.getMin());
            txt_max.setText("Max temp :"+ dto.getMax());
            txt_wind.setText("Wind: " + dto.getWind());
            txt_tip.setText("Tips: " + dto.getTip());
            txt_humatily.setText("Humidity: " + dto.getHumidity());
            txt_description.setText("Description: " +dto.getDescripton());


        } catch (Exception ex) {

        }

        // Set up the user interaction to manually show or hide the system UI.
        //mContentView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                toggle();
//            }
//        });

        // Upon interacting with UI controls, delay any scheduled hide()
        // operations to prevent the jarring behavior of controls going away
        // while interacting with the UI.
        ///findViewById(R.id.dummy_button).setOnTouchListener(mDelayHideTouchListener);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
    }


}

