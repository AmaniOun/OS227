package com.mycompany.os;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;


public class FileProcessor implements Runnable {

    private final String inputFile;

    public FileProcessor(String inputFile) {
        this.inputFile = inputFile;
    }

    @Override
    public void run() {
        try (BufferedReader br = new BufferedReader(new FileReader(inputFile))) {
            String line;

            while ((line = br.readLine()) != null) {

    line = line.trim();

    if (line.isEmpty()) continue;

    String[] fields = line.split(":|;");

    if (fields.length != 4) {
        System.err.println("[FileProcessor] Skipping invalid entry: " + line);
        continue;
    }

    try {

        int pid        = Integer.parseInt(fields[0].trim());
        int burst      = Integer.parseInt(fields[1].trim());
        int priority   = Integer.parseInt(fields[2].trim());
        int memorySize = Integer.parseInt(fields[3].trim());

        if (priority < 1 || priority > 30) {
            throw new IllegalArgumentException(
                "Priority must be between 1 and 30."
            );
        }

        ProcessInfo proc =
                OSKernel.createProcess(pid, burst, priority, memorySize);

        synchronized (SimulatorCore.pendingQueue) {
            SimulatorCore.pendingQueue.add(proc);
        }

    } catch (IllegalArgumentException e) {

        System.err.println(
            "[FileProcessor] Invalid process entry: " + e.getMessage()
        );
    }
}

        } catch (IOException e) {
            System.err.println("[FileProcessor] Failed to read file: " + e.getMessage());
        }
    }
}
