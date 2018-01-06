/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.andersoncarlosfs.execution;

import org.junit.Test;

/**
 *
 * @author AndersonCarlos
 */
public class MainTest {

    public MainTest() {
    }

    /**
     * Test of main method, of class Main.
     */
    @Test
    public void testMain() throws Exception {
        Runtime runtime = Runtime.getRuntime();
        runtime.gc();
        long total = runtime.totalMemory();
        long start = System.nanoTime();
        Main.main(null);
        long stop = System.nanoTime();
        long free = runtime.freeMemory();
        System.out.println("Time (MS): " + (stop - start) / 1000000.0);
        System.out.println("Memory (MB): " + (total - free) / (1024.0 * 1024.0));
    }

}
