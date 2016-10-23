package matthewkleinsmith.jetsonremotecontrol;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;

import java.io.IOException;
import java.lang.*;

public class GetDataActivity extends Activity {
    private final int throttleREVERSE = 58;
    private final int throttleNEUTRAL = 61;
    private final int throttleFORWARD = 65;
    private final int steeringMIN = 25;
    private final int steeringNEUTRAL = 50;
    private final int steeringMAX = 85;

    private SeekBar mThrottleBar;
    private SeekBar mSteeringBar;
    private ImageButton mPauseButton;
    private ImageButton mPlayButton;
    private TextView mThrottleText;
    private TextView mSteeringText;
    private ImageButton mForwardButton;
    private ImageButton mReverseButton;

    private int mThrottle = throttleNEUTRAL;

    private Boolean mLoopCommands;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_get_data);

        mThrottleBar = (SeekBar) findViewById(R.id.throttleBar);
        mSteeringBar = (SeekBar) findViewById(R.id.steeringBar);
        mPauseButton = (ImageButton) findViewById(R.id.pauseButton);
        mPlayButton = (ImageButton) findViewById(R.id.playButton);
        mThrottleText = (TextView) findViewById(R.id.throttleText);
        mSteeringText = (TextView) findViewById(R.id.steeringText);
        mForwardButton = (ImageButton) findViewById(R.id.forwardButton);
        mReverseButton = (ImageButton) findViewById(R.id.reverseButton);

        mThrottleBar.setProgress(throttleNEUTRAL);
        mThrottleText.setText(Integer.toString(mThrottleBar.getProgress()));
        mSteeringBar.setProgress(steeringNEUTRAL - steeringMIN);
        mSteeringText.setText(Integer.toString(mSteeringBar.getProgress() + steeringMIN));

        mLoopCommands = true;

        loopCommands();

        mForwardButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    mForwardButton.setImageResource(R.drawable.forward_arrow_filled_red);
                    mThrottle = throttleNEUTRAL;
                    return true;
                }
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    mForwardButton.setImageResource(R.drawable.forward_arrow_filled_green);
                    mThrottle = throttleFORWARD;
                }
                return false;
            }
        });

        mReverseButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    mReverseButton.setImageResource(R.drawable.forward_arrow_filled_red);
                    mThrottle = throttleNEUTRAL;
                    return true;
                }
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    mReverseButton.setImageResource(R.drawable.forward_arrow_filled_green);
                    mThrottle = throttleREVERSE;
                }
                return false;
            }
        });

        mSteeringBar.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    mSteeringBar.setThumb(getResources().getDrawable(R.drawable.thumb_red));
                    mSteeringBar.setProgress(steeringNEUTRAL - steeringMIN);
                    mSteeringText.setText(Integer.toString(steeringNEUTRAL));
                    return true;
                }
                if (event.getAction() == MotionEvent.ACTION_MOVE) {
                    mSteeringBar.setThumb(getResources().getDrawable(R.drawable.thumb_green));
                    mSteeringText.setText(Integer.toString(mSteeringBar.getProgress() + steeringMIN));
                }
                return false;
            }
        });


        // In unlimited speed mode:
        mThrottleBar.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    mThrottleBar.setProgress(throttleNEUTRAL);
                    mThrottleText.setText(Integer.toString(throttleNEUTRAL));
                    return true;
                }
                if (event.getAction() == MotionEvent.ACTION_MOVE) {
                    mThrottleText.setText(Integer.toString(mThrottleBar.getProgress()));
                }
                return false;
            }
        });
    }

    @Override
    protected void onDestroy() {
        resetCommands();
        sendCommands();
        super.onDestroy();
    }

    private void sendCommands() {
        String time = Long.toString(SystemClock.elapsedRealtime());
        String throttle = Integer.toString(mThrottle);
        String steering = Integer.toString(mSteeringBar.getProgress() + steeringMIN);
        String delim1 = ",";
        String delim2 = "\n";
        byte[] bytes = (time + delim1 + throttle + delim1 + steering + delim2).getBytes();
        try {((cBaseApplication)this.getApplicationContext()).theBluetoothServer.send(bytes);}
        catch (BluetoothServer.BluetoothServerException | IOException e) {e.printStackTrace();}
    }

    private void loopCommands() {
        mLoopCommands = true;
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                while (mLoopCommands) {
                    sendCommands();
                    SystemClock.sleep(1);
                }
            }
        });
    }

    private void resetCommands() {
        mThrottleBar.setProgress(throttleNEUTRAL);
        mSteeringBar.setProgress(steeringNEUTRAL - steeringMIN);
    }

    public void onPauseClick(View view) {
        resetCommands();
        sendCommands();
        mLoopCommands = false;
        mPauseButton.setVisibility(View.GONE);
        mPlayButton.setVisibility(View.VISIBLE);
    }

    public void onPlayClick(View view) {
        resetCommands();
        loopCommands();
        mPlayButton.setVisibility(View.GONE);
        mPauseButton.setVisibility(View.VISIBLE);
    }
}
