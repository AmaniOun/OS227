package com.mycompany.os;

public class OSKernel {

    // Create and initialize a new ProcessInfo object
    public static ProcessInfo createProcess(int pid, int cpuBurst, int priority, int memorySize) {
        ProcessInfo proc = new ProcessInfo(pid, cpuBurst, priority, memorySize);
        proc.setStatus("NEW");
        return proc;
    }

    public static void freeMemory(ProcessInfo proc) {
        SimulatorCore.freeMemory += proc.getMemorySize();
        proc.setStatus("TERMINATED");

        System.out.printf(
            "[Memory] P%d finished, memory freed=%d, available=%d%n",
            proc.getPid(),
            proc.getMemorySize(),
            SimulatorCore.freeMemory
        );

        SimulatorCore.activeQueue.notifyAll();
    }

     // Compute and store final timing metrics for a completed process.
        public static void recordCompletion(ProcessInfo proc, int currentTime) {
        proc.setFinishTime(currentTime);
        proc.setTat(proc.getFinishTime());
        proc.setWt(proc.getTat() - proc.getCpuBurst());
    }
}