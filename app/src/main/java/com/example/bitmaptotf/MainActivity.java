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
            float[] type = new RecognitionHelmet(new Helmet(MainActivity.this, 4), bitmap).recognize();
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
        for (int i = 0; i < pixels.size(); i++) {
            boolean merge = false;
            int[] cur = pixels.get(i);
            for (int j = 0; j < region.size(); j++) {
                if (shouldMerge(region.get(j), cur)) {    //判断region中是否与cur存在交集,该部分无法确定是否与py一致，运行出错再回顾
                    region.get(j).add(cur);
                    merge = true;
                }
            }
            if (!merge) {
                ArrayList<int[]> ans = new ArrayList<>();
                ans.add(cur);
                region.add(ans);
            }
        }
        ArrayList<ArrayList<Integer>> D = regionGroup(region);
        int len = D.size();
        int[][][] quadList = new int[len][4][2];
        int[][] scoreList = new int[len][4];
        for (int gTh = 0; gTh < len; gTh++) {
            int[][] totalScore = new int[4][2];
            for (int row = 0; row < D.get(gTh).size(); row++) {
                for (int k = 0; k < region.get(row).size(); k++) {
                    int[] ij = region.get(row).get(k);
                    float score = predict[ij[0]][ij[1]][1];
                }
            }
        }
    }

    private ArrayList<ArrayList<Integer>> regionGroup(ArrayList<ArrayList<int[]>> region) {
        ArrayList<Integer> S = new ArrayList<>();
        for (int i = 0; i < region.size(); i++) {
            S.add(i);
        }
        ArrayList<ArrayList<Integer>> D = new ArrayList<>();
        while(S.size()>0){
            int m = S.get(0);
            S.remove(0);
            if(region.size()==0){
                ArrayList<Integer> ans = new ArrayList<>();
                ans.add(m);
                D.add(ans);
            }else{
                D.add(recRegionMerge(region, m, S));
            }
        }
        return D;
    }

    private ArrayList<Integer> recRegionMerge(ArrayList<ArrayList<int[]>> region, int m, ArrayList<Integer> s) {
        ArrayList<Integer> tmp = new ArrayList<>();
        ArrayList<Integer> rows = new ArrayList<>();

        for (int n = 0; n < s.size(); n++) {
            if(!regionNeighbor(region.get(m)).contains(region.get(n))
                    || !regionNeighbor(region.get(n)).contains(region.get(m))){
                tmp.add(n);
            }
        }
        for (int i = 0; i < tmp.size(); i++) {
            s.remove(tmp.get(i));
        }
        for (int j = 0; j < tmp.size(); j++) {
            ArrayList<Integer> integers = recRegionMerge(region, tmp.get(j), s);
            for (int k = 0; k < integers.size(); k++) {
                rows.add(integers.get(k));
            }
        }
        return rows;
    }

    private ArrayList regionNeighbor(ArrayList<int[]> region) {
        int jMin, jMax = 0,max = region.get(0)[0], iMin = region.get(0)[0],indexMax = 0;
        ArrayList<int[]> neighbor = new ArrayList<>();
        for (int i = 1; i < region.size(); i++) {
            if(region.get(i)[0]<iMin){
                iMin = region.get(i)[0];
                indexMax = i;
            }
            if(region.get(i)[0]>max){
                max = region.get(i)[0];
                jMax = region.get(i)[1];
            }
            neighbor.add(new int[]{region.get(i)[0]+1, region.get(i)[1]});
        }
        jMax++;
        iMin++;
        jMin = region.get(indexMax)[1]-1;
        neighbor.add(new int[]{iMin, jMin});
        neighbor.add(new int[]{iMin, jMax});
        return neighbor;
    }

    private boolean shouldMerge(ArrayList<int[]> region, int[] cur) {
        for (int i = 0; i < cur.length; i++) {
            int ans = cur[i];
            for (int j = 0; i < region.size(); i++) {
                if (region.get(i)[0] == ans || region.get(i)[1] == ans) {
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
                if (cond[i][j]) {
                    res.add(new int[]{i, j});
                }
            }
        }
        return res;
    }


    private boolean[][] procesData(float[][][] res) {
        //暂时没有写py中的sigmoid函数
        double threshold = 0.9;
        boolean[][] ans = new boolean[width][height];
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                if (res[i][j][0] > threshold) {
                    ans[i][j] = true;
                } else {
                    ans[i][j] = false;
                }
            }
        }
        return ans;
    }

    //用于处理返回的数组
    private float[][][] arrayProc(float[] type) {
        int cnt = 0;
        float[][][] ans = new float[width][height][len];
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                for (int k = 0; k < len; k++) {
                    ans[i][j][k] = type[cnt];
                    cnt++;
                }
            }
        }
        return ans;
    }


}