package uk.ac.ucl.ndnocr.ui.splash;

/**
 * Created by srenevic on 08/09/16.
 */
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import uk.ac.ucl.ndnocr.MainActivity;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        Bundle bundle = intent.getExtras();
        if (bundle != null) {
            Log.d("Splashactivity", "Extras");
            for (String key : bundle.keySet()) {
                Object value = bundle.get(key);
                Log.d("Splashactivity", String.format("%s %s (%s)", key,
                        value.toString(), value.getClass().getName()));
            }
        }        finish();
    }
}