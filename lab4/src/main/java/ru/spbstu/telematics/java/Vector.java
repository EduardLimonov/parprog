package ru.spbstu.telematics.java;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
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

    static Vector add(Vector... vs) {
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
