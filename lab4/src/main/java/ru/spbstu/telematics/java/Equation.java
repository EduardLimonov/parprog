package ru.spbstu.telematics.java;

import java.math.BigDecimal;
import java.util.function.Function;

public class Equation {
    final Function<Vector, BigDecimal>[] fs;
    BigDecimal x0;
    Vector y0;
    BigDecimal h;
    BigDecimal maxX;

    Equation(final Function<Vector, BigDecimal>[] fs, BigDecimal x0, Vector y0, BigDecimal h, BigDecimal maxX) {
        this.fs = fs;
        this.x0 = x0;
        this.y0 = y0;
        this.h = h;
        this.maxX = maxX;
    }

    public Equation(Equation eq) {
        fs = eq.getFs();
        x0 = eq.getX0();
        y0 = eq.getY0();
        h = eq.getH();
        maxX = eq.getMaxX();
    }

    void setH(BigDecimal newH) {
        h = newH;
    }

    Function<Vector, BigDecimal>[] getFs() {
        return fs;
    }

    BigDecimal getX0() {
        return x0;
    }

    Vector getY0() {
        return y0;
    }

    BigDecimal getH() {
        return h;
    }

    BigDecimal getMaxX() {
        return maxX;
    }
}
