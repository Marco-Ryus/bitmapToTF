package com.example.bitmaptotf;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;

import java.io.IOException;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private int width = 184;
    private int height = 184;
    private int len = 7;

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
            float[][][] res = arrayProc(type);  //tflite只能获取一维数组，在此进行处理获得三维数组
            boolean[][] cond = procesData(res);     //相当于py中的greater_equal操作
            ArrayList<int[]> pixels = where(cond);//相当于where操作
            nms(res, pixels);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void nms(float[][][] predict, ArrayList<int[]> pixels) {
        double threshold = 0.9;
        ArrayList<ArrayList<int[]>> region = new ArrayList<>();
        for(int i=0;i<pixels.size();i++){
            boolean merge = false;
            int[] cur = pixels.get(i);
            for (int j = 0; j < region.size(); j++) {
                if(shouldMerge(region.get(j),cur)){    //判断region中是否与cur存在交集
                    region.get(j).add(cur);
                    merge = true;
                }
            }
            if(!merge){
                ArrayList<int[]> ans = new ArrayList<>();
                ans.add(cur);
                region.add(ans);
            }
        }
    }

    private boolean shouldMerge(ArrayList<int[]> region, int[] cur) {
        for (int i = 0; i < cur.length; i++) {
            int ans = cur[i];
            for (int j = 0; i < region.size(); i++) {
                if(region.get(i)[0]==ans||region.get(i)[1]==ans){
                    return true;
                }
            }
        }
        return false;

    }


    private ArrayList<int[]> where(boolean[][] cond) {
        ArrayList<int[]> res = new ArrayList<>();
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                if(cond[i][j]){
                    res.add(new int[]{i,j});
                }
            }
        }
        return res;
    }



    private boolean[][] procesData(float[][][] res) {
        //暂时没有写py中的sigmoid函数
        double threshold = 0.9;
        boolean[][] ans = new boolean[width][height];
        for(int i=0;i<width;i++){
            for(int j=0;j<height;j++){
                    if(res[i][j][0]>threshold){
                        ans[i][j] = true;
                    }else{
                        ans[i][j] = false;
                    }
            }
        }
        return ans;
    }

    //用于处理返回的数组
    private float[][][] arrayProc(float[] type) {
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