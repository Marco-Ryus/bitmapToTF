package com.example.bitmaptotf;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.tensorflow.lite.support.image.TensorImage;


public abstract class Recognition {

    protected abstract @NonNull float[] recognize();

    protected abstract TensorImage loadImage();
}
