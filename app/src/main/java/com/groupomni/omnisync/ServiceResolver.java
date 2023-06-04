package com.groupomni.omnisync;

import android.net.nsd.NsdServiceInfo;

import java.util.ArrayList;
import java.util.List;

public class ServiceResolver {
    private List<NsdServiceInfo> discoveredServices;
    private List<NsdServiceInfo> resolvedServices;

    public ServiceResolver() {
        discoveredServices = new ArrayList<>();
        resolvedServices = new ArrayList<>();
    }

    public void addService(NsdServiceInfo service) {
        synchronized (discoveredServices) {
            discoveredServices.add(service);
            discoveredServices.notifyAll();  // Notify the consumer thread
        }
    }

    public void resolveServices() {
        Thread producerThread = new Thread(new Producer());
        Thread consumerThread = new Thread(new Consumer());

        producerThread.start();
        consumerThread.start();
    }

    private class Producer implements Runnable {
        @Override
        public void run() {
            // Simulate adding services to the discoveredServices list
            // Replace this with your actual service discovery logic

            // Example:
            for (int i = 0; i < 10; i++) {
                NsdServiceInfo service = new NsdServiceInfo();

                addService(service);

                try {
                    Thread.sleep(1000);  // Simulate delay between service additions
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private class Consumer implements Runnable {
        @Override
        public void run() {
            while (true) {
                NsdServiceInfo service = null;

                synchronized (discoveredServices) {
                    // Wait until a service is available in the discoveredServices list
                    while (discoveredServices.isEmpty()) {
                        try {
                            discoveredServices.wait();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }

                    // Retrieve and remove the service from the discoveredServices list
                    service = discoveredServices.remove(0);
                }

                // Resolve the service
                resolveService(service);

                // Add the resolved service to the resolvedServices list
                synchronized (resolvedServices) {
                    resolvedServices.add(service);
                }
            }
        }

        private void resolveService(NsdServiceInfo service) {
            // Implement your service resolution logic here
            // ...

            System.out.println("Resolved service: " + service);
        }
    }


}
