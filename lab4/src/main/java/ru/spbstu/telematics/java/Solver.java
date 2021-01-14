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
