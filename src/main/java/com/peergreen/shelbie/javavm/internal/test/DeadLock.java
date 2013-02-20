package com.peergreen.shelbie.javavm.internal.test;


import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Validate;

/**
 * User: guillaume
 * Date: 20/02/13
 * Time: 13:45
 */
@Component
@Instantiate
public class DeadLock implements Runnable {

    @Validate
    public void start() {
        new Thread(this, "DeadLocking").start();
    }


    @Override
    public void run() {
        final Object resource2 = "resource2";
        final Object resource1 = "resource1";
        // t1 tries to lock resource1 then resource2
        Thread t1 = new Thread("One") {
            public void run() {
                // Lock resource 1
                synchronized (resource1) {
                    System.out.println("Thread 1: locked resource 1");

                    try {
                        Thread.sleep(50);
                    } catch (InterruptedException e) {
                    }

                    synchronized (resource2) {
                        System.out.println("Thread 1: locked resource 2");
                    }
                }
            }
        };

        // t2 tries to lock resource2 then resource1
        Thread t2 = new Thread("Two") {
            public void run() {
                synchronized (resource2) {
                    System.out.println("Thread 2: locked resource 2");

                    try {
                        Thread.sleep(50);
                    } catch (InterruptedException e) {
                    }

                    synchronized (resource1) {
                        System.out.println("Thread 2: locked resource 1");
                    }
                }
            }
        };

        // If all goes as planned, deadlock will occur,
        // and the program will never exit.
        t1.start();
        t2.start();
    }
}