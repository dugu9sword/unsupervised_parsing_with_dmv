package dugu9sword;

import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.util.ArrayUtil;

import java.lang.reflect.Array;
import java.util.Arrays;

public class FuckKotlin {
    public static void main(String[] args) {


        double[][][] x = new double[2][2][2];
//        ArrayUtil.
        for (int i = 0; i < 2; i++)
            for (int j = 0; j < 2; j++)
                Arrays.fill(x[i][j], 0.0);
        double[] flat = ArrayUtil.flattenDoubleArray(x);
        int[] shape = {2, 2, 2};    //Array shape here
        INDArray myArr = Nd4j.create(flat, shape, 'c');
        System.out.println(myArr);
    }

    public double[][][] da(){
        return new double[2][2][2];
    }
}