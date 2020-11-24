package com.example.bitmaptotf;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.tensorflow.lite.DataType;

import java.io.IOException;
import java.util.Arrays;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Bitmap bitmap = BitmapFactory.decodeResource(MainActivity.this.getResources(), R.mipmap.test);
        //下面调用的方法就是传入bitmap后获取返回数据，如果模型输出float数组就用float去接，
        // 也要调整recognize方法里最后的get方法换成模型相应的输出数据结构
        //在Helmat中修改模型名称路径，模型要放在asset里面
        try {
            float[] type = new RecognitionHelmet(new Helmet(MainActivity.this,4),bitmap).recognize();
//            @NonNull int[] ints = new RecognitionHelmet(new Helmet(MainActivity.this, 4), bitmap).recognize();
            float[][][] res = arrayProc(type);
            Log.d(TAG, "res的shape：" + res[0].length);
            Log.d(TAG,"数据类型是"+ Arrays.toString(res[0][0]));
            procesData(res);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void procesData(float[][][] res) {
        //暂时没有写py中的sigmoid函数
        
    }

    //用于处理返回的数组
    private float[][][] arrayProc(float[] type) {
        int width = 184;
        int height = 184;
        int len = 7;
        int cnt=0;
        float[][][] ans = new float[width][height][len];
        for(int i=0;i<width;i++){
            for(int j=0;j<height;j++){
                for(int k=0;k<len;k++){
                    ans[i][j][k] = type[cnt];
                    cnt++;
                }
            }
        }
        return ans;
    }



}