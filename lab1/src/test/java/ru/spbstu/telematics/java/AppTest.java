package ru.spbstu.telematics.java;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import static ru.spbstu.telematics.java.Vectors.getScalarProduct;


/**
 * Unit test for simple App.
 */
public class AppTest 
    extends TestCase
{
    static double[][] v1Examples = {
            {4, -5, 8.13, -100.0005},
            {-28.848, -18383.3888, 2389.4949},
            {-568758.79, 272.932, 918999.99, 28838.23, 29029.086},
            {109090.987, 819287, 8912.903, 8989.908},
            {378.893, 8926, 7873.03, 1, 773.2}
    };
    static double[][] v2Examples = {
            {1, -2, 3, 4},
            {989.21, -84982.03, 2837},
            {172973.919, -89219.94, -2888.2102, 83, 892839.747},
            {10204.902, 28908.03, 89278, 1740},
            {191, 737.47, 77817.48, 8983.38, 7827.9}
    };
    static double[] expected = {-361.612, 1569008158.8044841, -75138337620.60875, 25608604599.982273, 6.253758960073999E8};

    public void testApp()
    {
        for (int i = 0; i < v1Examples.length; i++) {
            double [] v1 = v1Examples[i];
            double [] v2 = v2Examples[i];
            double res = expected[i], myRes = -1;
            try {
                myRes = getScalarProduct(v1, v2);
            }
            catch (ArrayIndexOutOfBoundsException e) {

            }
            assertEquals(res, myRes);
        }
    }
}
