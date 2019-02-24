package com.example.xmltest;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.util.Log;

public class AudioListener {
    private static final int SAMPLE_RATE = 44100;
    private static final String LOG_TAG = "Recorder";
    //private AudioDataReceivedListener mListener;
    private Thread mThread;
    private int avg_short = 0;
    private boolean keepRecording = false;

    public void startRecording() {
        if (mThread != null)
            return;

        keepRecording = true;
        mThread = new Thread(new Runnable() {
            @Override
            public void run() {
                record();
            }
        });
        mThread.start();
    }

    public void stopPlayback() {
        if (mThread == null)
            return;
        keepRecording = false;
        mThread = null;
        Log.v(LOG_TAG, "Recording Stopped!");
    }

    private void setAverageBytes(int buffer_size, short[] buffer)
    {
        int average = 0;
        for(int i = 0; i < (buffer_size/2) - 1; i++)
        {
            average = average + buffer[i];
        }
        avg_short =  average/buffer_size;
    }

    public int getAverageBytes()
    {
        return(Math.abs(avg_short));
    }

    private void record() {
        Log.v(LOG_TAG, "Recording Started!");
        android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_AUDIO);

        // buffer size in bytes
        int bufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT);

        if (bufferSize == AudioRecord.ERROR || bufferSize == AudioRecord.ERROR_BAD_VALUE) {
            bufferSize = SAMPLE_RATE * 2;
        }

        short[] audioBuffer = new short[bufferSize / 2];

        AudioRecord record = new AudioRecord(MediaRecorder.AudioSource.DEFAULT,
                SAMPLE_RATE,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                bufferSize);

        if (record.getState() != AudioRecord.STATE_INITIALIZED) {
            //Log.e(LOG_TAG, "Audio Record can't initialize!");
            return;
        }
        record.startRecording();
        while (keepRecording) {
            record.read(audioBuffer, 0, audioBuffer.length);
            // Notify waveform
            //mListener.onAudioDataReceived(audioBuffer);
            setAverageBytes(bufferSize, audioBuffer);

        }

        record.stop();
        record.release();

        //Log.v(LOG_TAG, String.format("Recording stopped. Samples read: %d", shortsRead));
    }
}
