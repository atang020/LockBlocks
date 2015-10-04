package org.dev.atang020.lockblock;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.hardware.SensorListener;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import android.hardware.SensorEventListener;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorManager;
import android.widget.TextView;
import android.widget.RelativeLayout;
import android.os.Vibrator;
import android.util.Log;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;

import java.io.IOException;

public class MainActivity extends AppCompatActivity implements SensorEventListener {
    MediaPlayer siren;
    private SensorManager mSensorManager;
    private Sensor mSensor;
    private Camera camera;
    private Boolean isOn;
    protected Vibrator vibe;
    TextView tv, tv1, tv2;
    Parameters params;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE); //sensor objects
        mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY); //sensor objects
        Button b1 = (Button) findViewById(R.id.button);
        //get textviews
        tv = (TextView) findViewById(R.id.xval);       //displays the values on screen
        tv1 = (TextView) findViewById(R.id.yval);
        tv2=(TextView)findViewById(R.id.zval);
        vibe = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        b1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleAirplaneMode();
            }
        });
        siren = MediaPlayer.create(this, R.raw.siren);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void toggleAirplaneMode(){

        AlertDialog.Builder db = new AlertDialog.Builder(this);
        db.setTitle("Sorry!");
        db.setMessage("We recommend turning on Airplane mode.\n"
                + "Please enable Airplane mode.\n");

        db.setPositiveButton("Open Settings", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                startActivity(new Intent(Settings.ACTION_AIRPLANE_MODE_SETTINGS));
            }
        });
        db.setNegativeButton("Close app", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        })
                .show();
    }

    //detects sensor change
    @Override
    public void onSensorChanged (SensorEvent event){
        float z = event.values[2]; //get z value
        float x = event.values[0]; //get x value
        float y = event.values[1]; //get y value
        tv2.setText("Z axis" + "\t\t" + z); //print z value
        tv.setText("x axis" + "\t\t" + x); //print x value
        tv1.setText("y axis" + "\t\t" + y); //print y value
        if(z < 9 || y > .4 || x > .2) { //sets off alarm if phone is moved
            vibe.vibrate(500); //vibrates when moved (works)
            getCamera();    //this and next line tries making the flashlight blink
            blinkFlash(true);
            siren.setLooping(true);
            siren.start();
        } else{
            vibe.cancel(); //cancels vibration when phone is on table
            getCamera();  //supposed to turn off the flashlight when phone is on table(not working)
            blinkFlash(false);
            if(siren.isPlaying())
                siren.pause();
        }


    }


    @Override
    public void onDestroy(){
        super.onDestroy();
        siren.release();
        siren = null;
    }

    //sensor stuff
    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
        //If accuracy changed do something
    }

    @Override
    protected void onResume() {
        super.onResume();
        mSensorManager.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_NORMAL);
    }
    @Override
    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this);
    }

    //gets camera parameters
    private void getCamera(){
        if(camera == null){
            try{
                camera = Camera.open();
                params = camera.getParameters();
            } catch(RuntimeException e){
                Log.e("Camera failed to open", e.getMessage());
            }
        }
    }

    //makes flash blink on and off
    private void blinkFlash(boolean isOn){
        if(isOn) {
            turnOnFlash();
            turnOffFlash();
        }
        else
            turnOffFlash();
    }

    //turns on flash
    private void turnOnFlash(){
        if(camera == null || params == null)
            return;
        params = camera.getParameters();
        params.setFlashMode(Parameters.FLASH_MODE_TORCH);
        camera.setParameters(params);
        camera.startPreview();
    }

    //turns off flash
    private void turnOffFlash(){
        if(camera == null || params == null)
            return;
        params = camera.getParameters();
        params.setFlashMode(Parameters.FLASH_MODE_OFF);
        camera.setParameters(params);
        camera.stopPreview();
    }
    //releases control of camera
    @Override
    protected void onStop() {
        super.onStop();

        // on stop release the camera
        if (camera != null) {
            camera.release();
            camera = null;
        }
    }
}
