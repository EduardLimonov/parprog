package ru.spbstu.telematics.java;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;
import java.util.function.BiFunction;
import java.util.function.Function;

class Vector {
    BigDecimal[] elements;
    static int NTHREADS = 4;

    Vector(BigDecimal[] arr) {
        elements = Arrays.copyOf(arr, arr.length);
    }

    Vector(int size) {
        elements = new BigDecimal[size];
    }

    public Vector(Vector tVector) {
        elements = Arrays.copyOf(tVector.getElements(), tVector.size());
    }

    public Vector(Vector vector, Vector other) {
        elements = Arrays.copyOf(vector.getElements(), vector.size() + other.size());
        System.arraycopy(other.getElements(), 0, elements, vector.size(), other.size());
    }

    public Vector(BigDecimal x) {
        elements = new BigDecimal[1];
        elements[0] = x;
    }

    int size() {
        return elements.length;
    }

    BigDecimal[] getElements() {
        return elements;
    }

    void set(BigDecimal elem, int coord) {
        elements[coord] = elem;
    }

    BigDecimal get(int coord) {
        return elements[coord];
    }

    Vector add(Vector other) throws IndexOutOfBoundsException {
        return applyFunction(other, BigDecimal::add);
    }

    Vector add(BigDecimal val) {
        Vector res = new Vector(size());
        for (int i = 0; i < size(); i++)
            res.set(elements[i].add(val), i);

        return res;
    }

    Vector multiply(Vector other) throws IndexOutOfBoundsException {
        return applyFunction(other, BigDecimal::multiply);
    }

    Vector multiply(BigDecimal val) {
        Vector res = new Vector(size());
        for (int i = 0; i < size(); i++)
            res.set(elements[i].multiply(val), i);

        return res;
    }

    Vector applyFunction(Function<BigDecimal, BigDecimal> f) {
        Vector res = new Vector(size());
        for (int i = 0; i < size(); i++)
            res.set(f.apply(elements[i]), i);

        return res;
    }

    static Vector add(Vector first, Vector second) throws ExecutionException, InterruptedException {
        return applyFunction(first, second, BigDecimal::add);
    }

    static Vector add(Vector ... vs) {
        Vector res = new Vector(vs[0]);
        for (int i = 1; i < vs.length; i++)
            res = res.add(vs[i]);
        return res;
    }

    static Vector add(Vector v, BigDecimal val) throws ExecutionException, InterruptedException {
        Vector res = new Vector(v.size());

        ExecutorService exec = Executors.newFixedThreadPool(NTHREADS);
        Future<BigDecimal>[] futures = new Future[v.size()];

        for (int i = 0; i < v.size(); i++) {
            int finalI = i;
            futures[i] = exec.submit(() -> v.elements[finalI].add(val));
        }
        exec.shutdown();
        for (int i = 0; i < res.size(); i++) {
            res.set(futures[i].get(), i);
        }
        return res;
    }

    static Vector multiply(Vector v, BigDecimal val) throws ExecutionException, InterruptedException {
        Vector res = new Vector(v.size());
        ExecutorService exec = Executors.newFixedThreadPool(NTHREADS);
        Future<BigDecimal>[] futures = new Future[v.size()];

        for (int i = 0; i < v.size(); i++) {
            int finalI = i;
            futures[i] = exec.submit(() -> v.elements[finalI].multiply(val));
        }

        exec.shutdown();
        for (int i = 0; i < res.size(); i++) {
            res.set(futures[i].get(), i);
        }
        return res;
    }

    static Vector applyFunction(Vector first, Vector second, BiFunction<BigDecimal, BigDecimal, BigDecimal> f) throws ExecutionException, InterruptedException {
        if (first.size() != second.size())
            throw new IndexOutOfBoundsException();

        Vector res = new Vector(first.size());

        ExecutorService exec = Executors.newFixedThreadPool(NTHREADS);
        Future<BigDecimal>[] futures = new Future[first.size()];

        for (int i = 0; i < first.size(); i++) {
            int finalI = i;
            futures[i] = exec.submit(() -> f.apply(first.elements[finalI], second.elements[finalI]));
        }
        exec.shutdown();

        for (int i = 0; i < res.size(); i++) {
            res.set(futures[i].get(), i);
        }
        return res;
    }

    public static Vector applyFunctions(Vector v, Function<Vector, BigDecimal>[] fs) throws ExecutionException, InterruptedException {
        Vector res = new Vector(fs.length);
        ExecutorService exec = Executors.newFixedThreadPool(NTHREADS);
        Future<BigDecimal>[] futures = new Future[fs.length];
        for (int i = 0; i < fs.length; i++) {
            int finalI = i;
            futures[i] = exec.submit(() -> fs[finalI].apply(v));
        }
        exec.shutdown();

        for (int i = 0; i < fs.length; i++) {
            res.set(futures[i].get(), i);
        }

        return res;
    }

    static Vector merge(Vector first, Vector other) {
        return first.merge(other);
    }

    Vector applyFunction(Vector other, BiFunction<BigDecimal, BigDecimal, BigDecimal> f) {
        if (size() != other.size())
            throw new IndexOutOfBoundsException();

        Vector res = new Vector(size());
        for (int i = 0; i < size(); i++)
            res.set(f.apply(elements[i], other.get(i)), i);

        return res;
    }

    Vector applyFunctions(Vector other, BiFunction<BigDecimal, BigDecimal, BigDecimal>[] f) {
        if (size() != other.size())
            throw new IndexOutOfBoundsException();

        Vector res = new Vector(size());
        for (int i = 0; i < size(); i++)
            res.set(f[i].apply(elements[i], other.get(i)), i);

        return res;
    }

    Vector applyFunctions(Function<Vector, BigDecimal>[] fs) {
        Vector res = new Vector(fs.length);
        for (int i = 0; i < fs.length; i++)
            res.set(fs[i].apply(this), i);

        return res;
    }

    Vector merge(Vector other) {
        return new Vector(this, other);
    }

    public boolean lessThan(BigDecimal other) {

        return elements[0].compareTo(other) < 0;
    }

    public String toString() {
        return Arrays.toString(elements);
    }
}

public class Solver {
    enum ParallelMod {
        DISABLE, ENABLE
    }

    final static BigDecimal MAX_Q = new BigDecimal("0.05");
    static int SCALE = 30;

    static boolean checkH(BigDecimal k1, BigDecimal k2, BigDecimal k3, BigDecimal k4) {
        return (k4.subtract(k3).divide(k1.subtract(k2), RoundingMode.HALF_UP)).abs().compareTo(MAX_Q) < 0; // Q < MAX_Q
    }

    static boolean checkAccurancy(BigDecimal yXn, BigDecimal yn, BigDecimal yn_) {
        // yXn -- точное решение, yn, yn_ -- приближенные значения, полученные при расчете с шагом h и с шагом h/2 соответственно
        return yXn.subtract(yn_).abs().compareTo(yn.subtract(yn_).abs().multiply(BigDecimal.valueOf(1. / 15.))) < 0;
    }

    static void setSCALE(int scale) {
        SCALE = scale;
    }

    public static Map<BigDecimal, Vector> resolve(Equation equation, ParallelMod pm) {
        Function<Vector, BigDecimal>[] fs = equation.getFs();
        BigDecimal x0 = equation.getX0();
        Vector y0 = equation.getY0();
        BigDecimal h = equation.getH();
        BigDecimal maxX = equation.getMaxX();

        MathContext mc = new MathContext(SCALE);

        Vector x = new Vector(x0);
        Vector y = new Vector(y0);
        Map<BigDecimal, Vector> table = new HashMap<>();
        while (x.lessThan(maxX)) {
            if (pm == ParallelMod.DISABLE)
                y = getNext(y, x, fs, h);
            else {
                try {
                    y = getNextParallel(y, x, fs, h);
                } catch (ExecutionException | InterruptedException e) {
                    e.printStackTrace();
                }
            }
            x = x.add(h);
            table.put(x.get(0).round(mc),
                    y.applyFunction(t -> t.round(mc)
            ));
        }

        return table;
    }

    static void testH(Equation eq, Vector ys) {
        BigDecimal h = eq.getH();
        BigDecimal h2 = h.divide(new BigDecimal("2"), RoundingMode.HALF_UP);
        Equation eq_ = new Equation(eq);
        eq_.setH(h2);

        Vector yn = new Vector(resolve(eq, ParallelMod.DISABLE).get(eq.getMaxX())), yn_ = new Vector(resolve(eq_, ParallelMod.DISABLE).get(eq_.getMaxX()));

        for (int i = 0; i < yn.size(); i++)
            System.out.println("" + i + ' ' + checkAccurancy(ys.get(i), yn.get(i), yn_.get(i)));
    }

    static Vector getNextParallel(Vector vectorPrev, Vector x, final Function<Vector, BigDecimal>[] fs, BigDecimal h) throws ExecutionException, InterruptedException {
        // Вычисляет столбец y_n+1 методом Рунге-Кутты четвертого порядка
        BigDecimal bd2 = new BigDecimal(2), bd6 = new BigDecimal(6);
        BigDecimal h2 = h.divide(bd2, SCALE, RoundingMode.HALF_UP);
        Vector xh = Vector.add(x, h), xh2 = Vector.add(x, h2);

        class Computer {
            Vector compK1() {
                try {
                    return Vector.applyFunctions(Vector.merge(x, vectorPrev), fs);
                } catch (ExecutionException | InterruptedException e) {
                    e.printStackTrace();
                }
                return null;
            }

            Vector compK23(Vector kPrev) throws ExecutionException, InterruptedException {
                Vector kh23 = Vector.multiply(kPrev, h2);
                return Vector.applyFunctions(Vector.merge(xh2, Vector.add(vectorPrev, kh23)), fs);
            }

            Vector compK4(Vector k3) throws ExecutionException, InterruptedException {
                Vector k3h = Vector.multiply(k3, h);
                return Vector.applyFunctions(Vector.merge(xh, Vector.add(vectorPrev, k3h)), fs);
            }

            Vector compDy(Vector k1, Vector k2, Vector k3, Vector k4) throws ExecutionException, InterruptedException {
                return Vector.multiply(
                        Vector.add(k1, Vector.multiply(k2, BigDecimal.valueOf(2)), Vector.multiply(k3, BigDecimal.valueOf(2)), k4),
                        h.divide(bd6, SCALE, RoundingMode.HALF_UP));
            }
        }

        Computer pc = new Computer();
        Vector k1 = pc.compK1();

        Vector k2 = pc.compK23(k1);

        Vector k3 = pc.compK23(k2);

        Vector k4 = pc.compK4(k3);

        try {
            return vectorPrev.add(pc.compDy(k1, k2, k3, k4));
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }


    static Vector getNext(Vector vectorPrev, Vector x, final Function<Vector, BigDecimal>[] fs, BigDecimal h) {
        // Вычисляет столбец y_n+1 методом Рунге-Кутты четвертого порядка
        Vector k1, k2, k3, k4;
        BigDecimal bd2 = new BigDecimal(2), bd6 = new BigDecimal(6);
        BigDecimal h2 = h.divide(bd2, SCALE, RoundingMode.HALF_UP);

        k1 = x.merge(vectorPrev).applyFunctions(fs);

        k2 = x.add(h2).merge(vectorPrev.add(k1.multiply(h2))).applyFunctions(fs);

        k3 = x.add(h2).merge(vectorPrev.add(k2.multiply(h2))).applyFunctions(fs);

        k4 = x.add(h).merge(vectorPrev.add(k3.multiply(h))).applyFunctions(fs);

        Vector dy = k1.add(k2.multiply(bd2)).add(k3.multiply(bd2)).add(k4).multiply(h.divide(bd6, SCALE, RoundingMode.HALF_UP));
        return vectorPrev.add(dy);
    }

    private static void printResult(Map<BigDecimal, Vector> result) {
        ArrayList<BigDecimal> keys = new ArrayList<>(result.keySet());
        keys.sort(BigDecimal::compareTo);
        for (BigDecimal k: keys)
            System.out.println(k + " : " + result.get(k));
    }

    static Map<BigDecimal, Vector> test(Equation eq, ParallelMod pm) {
        long start = System.currentTimeMillis();

        Map<BigDecimal, Vector> result = resolve(eq, pm);

        long finish = System.currentTimeMillis();
        printResult(result);
        long elapsed = finish - start;
        System.out.println("Прошло времени, мс: " + elapsed);
        return result;
    }

    static Equation getEquation1() {
        Function<Vector, BigDecimal>[] fs = new Function[2];
        fs[0] = vector -> vector.get(1).multiply(BigDecimal.valueOf(-3)).subtract(vector.get(2));
        fs[1] = vector -> vector.get(1).subtract(vector.get(2));

        BigDecimal[] y0s = {BigDecimal.valueOf(2), BigDecimal.valueOf(-1)};

        return new Equation(fs, BigDecimal.valueOf(0), new Vector(y0s), BigDecimal.valueOf(0.1), BigDecimal.valueOf(0.5));
    }

    static Equation getEquation2() {
        Function<Vector, BigDecimal>[] fs = new Function[4];
        fs[0] = vector -> vector.get(1).multiply(BigDecimal.valueOf(-3)).subtract(vector.get(2));
        fs[1] = vector -> vector.get(1).subtract(vector.get(2));
        fs[2] = vector -> vector.get(0).multiply(vector.get(2)).multiply(vector.get(1));
        fs[3] = vector -> vector.get(1).subtract(vector.get(0)).multiply(vector.get(1));

        BigDecimal[] y0s = {BigDecimal.valueOf(2), BigDecimal.valueOf(-1), BigDecimal.valueOf(12.5), BigDecimal.valueOf(0)};

        return new Equation(fs, BigDecimal.valueOf(0), new Vector(y0s), BigDecimal.valueOf(0.01), BigDecimal.valueOf(1));
    }

    public static void main(String[] args) {
//        test1();
//        test1Parallel();
        setSCALE(400);
        Equation eq = getEquation2();
        test(eq, ParallelMod.DISABLE);
        test(eq, ParallelMod.ENABLE);
    }
}
