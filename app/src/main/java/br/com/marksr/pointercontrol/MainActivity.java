package br.com.marksr.pointercontrol;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.CompoundButton;
import android.widget.Switch;

import com.marksr.pointercontrol.R;

public class MainActivity extends AppCompatActivity {
    private String TAG = this.getClass().getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        Switch switchCompat = (Switch) findViewById(R.id.switch_server);

        switchCompat.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                if (isChecked) {
                    Log.d(TAG, "Starting server");
                    Intent pointServer = new Intent(MainActivity.this, PointerServer.class);
                    startService(pointServer);
                } else {
                    Log.d(TAG, "Stoping server");
                    stopService(new Intent(MainActivity.this, PointerServer.class));
                }

            }
        });
    }

    public String getTAG() {
        return TAG;
    }

    public void setTAG(String TAG) {
        this.TAG = TAG;
    }
}
