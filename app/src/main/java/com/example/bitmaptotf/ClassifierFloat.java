package com.example.bitmaptotf;

import android.app.Activity;

import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.support.common.TensorOperator;
import org.tensorflow.lite.support.common.TensorProcessor;
import org.tensorflow.lite.support.common.ops.NormalizeOp;
import org.tensorflow.lite.support.image.TensorImage;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.io.IOException;


public class ClassifierFloat extends Classifier{

    private static final float IMAGE_MEAN = 127.5f;
    private static final float IMAGE_STD = 127.5f;

    private static final float PROBABILITY_MEAN = 0.0f;
    private static final float PROBABILITY_STD = 1.0f;

    public ClassifierFloat(Activity activity, int numThreads) throws IOException {
        super(activity, numThreads);
    }

    @Override
    protected String getModelPath() {
        return null;
    }


    @Override
    protected TensorOperator getPostprocessNormalizeOp() {
        return new NormalizeOp(PROBABILITY_MEAN, PROBABILITY_STD);
    }

    @Override
    public TensorOperator getPreprocessNormalizeOp() {
        return new NormalizeOp(IMAGE_MEAN, IMAGE_STD);
    }

    @Override
    public int getImageSizeX() {
        return super.imageSizeX;
    }

    @Override
    public int getImageSizeY() {
        return super.imageSizeY;
    }

    @Override
    public Interpreter getTflite() {
        return super.tflite;
    }

    @Override
    public TensorBuffer getOutputProbabilityBuffer() {
        return super.outputProbabilityBuffer;
    }

    @Override
    public TensorProcessor getProbabilityProcessor() {
        return super.probabilityProcessor;
    }

    @Override
    public TensorImage getInputImageBuffer() {
        return super.inputImageBuffer;
    }

}
