package com.example.giorgi.movierecognition.controller;

import java.util.List;

public interface FramesListener {

    void sendFrameHashes(List<Long> hashes);
}
