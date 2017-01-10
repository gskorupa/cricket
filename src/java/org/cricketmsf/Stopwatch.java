/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cricketmsf;

import java.util.concurrent.TimeUnit;

/**
 *
 * @author greg
 */
public class Stopwatch {

    public long start = System.nanoTime();

    public long time() {
        return System.nanoTime() - start;
    }

    public long time(TimeUnit unit) {
        return unit.convert(time(), TimeUnit.NANOSECONDS);
    }

}
