package com.example.giorgi.movierecognition.view;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;

import com.example.giorgi.movierecognition.R;
import com.example.giorgi.movierecognition.controller.ComputeHashThread;
import com.example.giorgi.movierecognition.controller.FramesListener;
import com.example.giorgi.movierecognition.controller.JSONObjectDownloadedListener;
import com.example.giorgi.movierecognition.model.VolleySingleton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;

import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;

public class MainActivity extends Activity  implements View.OnTouchListener,
        CameraBridgeViewBase.CvCameraViewListener2, FramesListener, JSONObjectDownloadedListener {

    private BlockingQueue<Mat> framesToBeHashed;
    private Mat mRgba;
    private CameraBridgeViewBase mCameraBridgeView;
    private boolean isClicked = false;
    private FloatingActionButton button;
    private ComputeHashThread computeHashThread;

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status){
                case LoaderCallbackInterface.SUCCESS:
                    mCameraBridgeView.enableView();
                    mCameraBridgeView.setOnTouchListener(MainActivity.this);
                    break;
                default:
                    super.onManagerConnected(status);
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        mCameraBridgeView = (CameraBridgeViewBase)findViewById(R.id.camera_view);
        mCameraBridgeView.setVisibility(SurfaceView.VISIBLE);
        mCameraBridgeView.setCvCameraViewListener(this);
        framesToBeHashed = new LinkedBlockingDeque<>();
        computeHashThread = new ComputeHashThread(framesToBeHashed, this);
        computeHashThread.start();

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (OpenCVLoader.initDebug()){
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_1_0, this, mLoaderCallback);
        }else{
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
        button = (FloatingActionButton)findViewById(R.id.fab);
        button.setImageResource(R.drawable.ic_videocam);
        isClicked = false;
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mCameraBridgeView != null){
            mCameraBridgeView.disableView();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mCameraBridgeView != null){
            mCameraBridgeView.disableView();
        }
    }

    @Override
    public void onCameraViewStarted(int width, int height) {
        mRgba = new Mat();
    }

    @Override
    public void onCameraViewStopped() {
        mRgba.release();
    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        mRgba = inputFrame.rgba();
        if (isClicked) {
            try {
                framesToBeHashed.put(mRgba);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return mRgba;
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        return false;
    }

    public void startRecording(View view){
        isClicked = !isClicked;
        if (isClicked){
            button.setImageResource(R.drawable.ic_stop);
        }else{
            button.setImageResource(R.drawable.ic_videocam);
        }
    }

    private void startNewIntent(String filmId){
        Intent intent = new Intent(this, DisplayMovieActivity.class);
        intent.putExtra(getResources().getString(R.string.movie_id_intent_key), filmId);
        startActivity(intent);
    }

    private void sendHashes(List<Long> hashes){
        String url = getResources().getString(R.string.server_url);
        VolleySingleton.getInstance(this).downloadJSONObject(url, this, generateRequest(hashes));
    }

    private JSONObject generateRequest(List<Long> hashes){
        JSONObject request = new JSONObject();
        JSONArray jsonArray = new JSONArray(hashes);
        try {
            request.put(getResources().getString(R.string.frame_hashes_post_key), jsonArray);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return request;
    }

    @Override
    public void sendFrameHashes(List<Long> hashes) {
        if (isClicked) {
            isClicked = false;
            sendHashes(hashes);
        }
    }

    @Override
    public void jsonObjectDownloaded(JSONObject response) {
        try {
            startNewIntent(response.getString(getResources().getString(R.string.movie_id_intent_key)));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
