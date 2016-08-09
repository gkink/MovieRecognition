package com.example.giorgi.movierecognition.controller;

import org.opencv.core.Mat;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;

public class ComputeHashThread extends Thread{

    private BlockingQueue<Mat> frames;
    private static final int NUMBER_OF_HEALTHY_FRAMES = 100;
    private List<Long> frameHashes;
    private FramesListener observer;
    private static final String COMPUTE_HASH_LIBRARY_NAME = "compute_hash_library";

    public ComputeHashThread(BlockingQueue<Mat> frames, FramesListener observer) {
        this.frames = frames;
        this.frameHashes = new ArrayList<>();
        this.observer = observer;
    }

    @Override
    public void run() {
        while (true){
            try {
                Mat frame = frames.take();
                long hash = getFrameHash(frame.getNativeObjAddr());
                if (hash != 0) {
                    frameHashes.add(hash);
                    if (frameHashes.size() == NUMBER_OF_HEALTHY_FRAMES) {
                        observer.sendFrameHashes(frameHashes);
                        frameHashes.clear();
                    }
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public native long getFrameHash(long matAddress);

    static {
        System.loadLibrary(COMPUTE_HASH_LIBRARY_NAME);
    }
}
