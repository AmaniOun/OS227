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

                int pid        = Integer.parseInt(fields[0].trim());
                int burst      = Integer.parseInt(fields[1].trim());
                int priority   = Integer.parseInt(fields[2].trim());
                int memorySize = Integer.parseInt(fields[3].trim());

                ProcessInfo proc = OSKernel.createProcess(pid, burst, priority, memorySize);

                synchronized (SimulatorCore.pendingQueue) {
                    SimulatorCore.pendingQueue.add(proc);
                }
            }

        } catch (IOException e) {
            System.err.println("[FileProcessor] Failed to read file: " + e.getMessage());
        }
    }
}
