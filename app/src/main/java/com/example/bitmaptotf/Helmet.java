package com.example.bitmaptotf;

import android.app.Activity;

import com.example.bitmaptotf.ClassifierFloat;

import java.io.IOException;
import java.util.List;

public class Helmet extends ClassifierFloat {

    public Helmet(Activity activity, int numThreads) throws IOException {
        super(activity, numThreads);
    }

    @Override
    protected String getModelPath() {
//        return "hat.tflite";
        return "AdvancedEast3.tflite";
    }


    public static boolean haveHat(List<Float> scores, float scale) {
        int i = 0;
        for (float score: scores) {
            if(score > 0.5) {
                i += 1;
            }
        }
        return i >= scores.size() * scale;
    }

}
