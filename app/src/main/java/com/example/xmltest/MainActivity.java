package com.example.xmltest;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.Random;

public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_RECORD_AUDIO = 13;
    private boolean running = true;
    private Handler h1 = new Handler();
    //initiating the audio listener
    private AudioListener listener = new AudioListener();
    int listenerRefreshCounter;
    int averageSound;

    //Integer value of relative sound
    //0 is negligible sound (<50)
    //1 is moderate sound (50<x<100)
    //2 is major sound (>100)
    int excitementLevel;
    int lastExcitementLevel;
    //State refers to what gif is currently playing
    int currentState;
    int lastState;
    long timeAtGifBeginning;
    long gifOneTime;
    long gifTwoTime;


    private Runnable runnableCode = new Runnable() {
        @Override
        public void run(){
            final TextView t1 = findViewById(R.id.textView2);
            String sound = listener.getAverageBytes() +"";
            int value = Integer.parseInt(sound);
            if(listenerRefreshCounter<=10){
                averageSound+=value;
                listenerRefreshCounter++;
            }else{
                //t1.setText(String.valueOf(averageSound/20));
                if(averageSound/10<=20){
                    //not enough sound
                    excitementLevel = 0;
                }else if((10<(averageSound/20)) && (averageSound/10 <60)){
                    //sound enough to start upsetting the motes
                    excitementLevel = 1;
                }else if(averageSound/10 >=60){
                    //set new state and animation
                    excitementLevel = 2;
                }
                gifControl();
                averageSound=0;
                listenerRefreshCounter=0;
            }
            h1.postDelayed(this,100);
        }
    };

    public ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);

        imageView = (ImageView) findViewById(R.id.bgd);

        Random randomGen = new Random();
        if(randomGen.nextBoolean()){
            imageView.setImageResource(R.drawable.journeybgd);
        }

        //Listener Functionality Begins
        getPermission();
        listener.startRecording();
        h1.post(runnableCode);
        listenerRefreshCounter=0;
        averageSound=0;
        excitementLevel = 0;
        lastExcitementLevel=0;
        currentState = 0;
        lastState = 0;
        /////////////////////////////////////////////////////////////

        gifTwoTime = 4000000000L;
        gifOneTime = 4000000000L;

        imageView = (ImageView) findViewById(R.id.gif);
        Glide.with(this).load(R.raw.dandelionfull).into(imageView);
    }



    @Override
    public void onStop(){
        super.onStop();
        h1.removeCallbacks(runnableCode);
        listener.stopPlayback();
        finish();
    }

    public void setGif(){
        if(lastState != currentState) {
            if (currentState <= 2) {
                if (currentState == 0) {
                    Glide.with(MainActivity.this).load(R.raw.dandelionfull).into(imageView);
                    lastState = 0;
                } else if (currentState == 1) {
                    Glide.with(MainActivity.this).load(R.raw.dandelionhalf).into(imageView);
                    lastState = 1;
                } else if (currentState == 2) {
                    Glide.with(MainActivity.this).load(R.raw.dandelionempty).into(imageView);
                    lastState = 2;
                    Runnable r = new Runnable() {
                        @Override
                        public void run() {
                            finish();
                            System.exit(0);
                        }
                    };
                    h1.postDelayed(r,1000);
                }
            } else {
                timeAtGifBeginning = System.nanoTime();
                if (currentState == 3) {
                    Glide.with(MainActivity.this).load(R.raw.dandelionhalfblow).into(imageView);
                    lastState = 3;
                } else if (currentState == 4) {
                    Glide.with(MainActivity.this).load(R.raw.dandelionfinalblow).into(imageView);
                    lastState = 4;
                }
            }
        }
    }

    public void gifControl(){
        //check to see if current state is still a running gif animation
        if(currentState == 3){
            long currentTime = System.nanoTime();
            long elapsedTime = currentTime-timeAtGifBeginning;
            if(elapsedTime<gifOneTime){
                return;
            }else{
                //stop gif One from playing. Make sure to pad out the end of a gif with normal waving, and time it to the end of the proper animation
                //start regular idle gif playing with setGif()
                currentState =1;
                setGif();
            }

        }else if(currentState == 4){
            long currentTime = System.nanoTime();
            long elapsedTime = currentTime-timeAtGifBeginning;
            if(elapsedTime<gifTwoTime){
                return;
            }else{
                //stop gif One from playing. Make sure to pad out the end of a gif with normal waving, and time it to the end of the proper animation
                //start regular idle gif playing with setGif()
                currentState =2;
                setGif();
            }
        }else if(currentState == 0){
            if(excitementLevel == 2){
                currentState=3;
                setGif();
            }
        }else if(currentState == 1){
            if(excitementLevel == 2){
                currentState=4;
                setGif();
            }
        }
    }

    public void getPermission(){
        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.READ_CONTACTS)
                != PackageManager.PERMISSION_GRANTED) {

            // Permission is not granted
            if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,
                    Manifest.permission.READ_CONTACTS)) {
            } else {
                // No explanation needed; request the permission
                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{android.Manifest.permission.RECORD_AUDIO},
                        REQUEST_RECORD_AUDIO);

            }
        } else {
            // Permission has already been granted
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case REQUEST_RECORD_AUDIO: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request.
        }
    }

    /*
        Runnable r = new Runnable(){
            @Override
            public void run(){
                Glide.with(MainActivity.this).clear(imageView);
            }
        };

        Handler h = new Handler();
        h.postDelayed(r,5000);
     */
}
