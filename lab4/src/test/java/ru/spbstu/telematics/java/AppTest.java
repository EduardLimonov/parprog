package ru.spbstu.telematics.java;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.Arrays;
import java.util.Comparator;

public class AppTest 
    extends TestCase
{
    static int SCALE = 2;

    public AppTest( String testName )
    {
        super( testName );
    }

    public static Test suite()
    {
        return new TestSuite( AppTest.class );
    }

    public static boolean equals(BigDecimal[] results, BigDecimal[] etalon) {
        return Arrays.equals(results, etalon, (o1, o2) -> {

            MathContext mc = new MathContext(SCALE);
            return o1.round(mc).compareTo(o2.round(mc));
        });
    }

    public void check1() {
        Equation eq = Solver.getEquation1();
        BigDecimal[] lastY = {
                BigDecimal.valueOf(0.552), BigDecimal.valueOf(-0.184)
        };
        assertTrue(equals(
                lastY, Solver.resolve(eq, Solver.ParallelMod.DISABLE).get(BigDecimal.valueOf(0.5)).getElements()
        ));
    }

    public void check2() {
        Equation eq = Solver.getEquation2();
        BigDecimal[] lastY = {
                BigDecimal.valueOf(0.1353), BigDecimal.valueOf(-9.2046e-10), BigDecimal.valueOf(12.4445), BigDecimal.valueOf(0.5622)
        };
        assertTrue(equals(
                lastY, Solver.resolve(eq, Solver.ParallelMod.DISABLE).get(new BigDecimal("1.00")).getElements()
        ));
    }

    public void testApp()
    {
        check1();
        check2();
    }
}
