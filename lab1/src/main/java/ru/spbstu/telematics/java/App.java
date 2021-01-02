package ru.spbstu.telematics.java;

class Vectors {
    static double[] getFirstVector() {
        return new double[]{2.4, 5.1, 12., 15.5};
    }

    static double[] getSecondVector() {
        return new double[]{11.1, -12.21, 13.31, 14};
    }

    static double getScalarProduct(double[] v1, double[] v2) throws ArrayIndexOutOfBoundsException {
        if (v1.length != v2.length)
            throw new ArrayIndexOutOfBoundsException("Размерности векторов не совпадают!");

        double res = 0;
        for (int i = 0; i < v1.length; i++) {
            res += v1[i] * v2[i];
        }
        return res;
    }

    static String getStrVector(double[] v) {
        StringBuilder sb = new StringBuilder();
        for (double d: v) {
            sb.append(Double.toString(d));
            sb.append(", ");
        }
        sb.deleteCharAt(sb.length()-1);
        sb.deleteCharAt(sb.length()-1);
        return sb.toString();
    }

    static void runLab() {
        double[] v1 = getFirstVector();
        double[] v2 = getSecondVector();
        System.out.println("v1 = " + getStrVector(v1));
        System.out.println("v2 = " + getStrVector(v2));

        double result = getScalarProduct(v1, v2);

        System.out.println("Скалярное произведение векторов v1 и v2: SP = " + result);
    }
}

public class App {
    public static void main(String[] args) {
        Vectors.runLab();
    }
}