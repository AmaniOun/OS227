package com.mycompany.os;

import java.util.Comparator;

public class CPUDispatcher {

    private static final int QUANTUM = 5;

    public void dispatch() throws InterruptedException {
        switch (SimulatorCore.algorithm) {
            case "SJF":  runSJF();      break;
            case "RR":   runRR();       break;
            case "PRIO": runPriority(); break;
            default: System.out.println("[CPUDispatcher] Unknown algorithm.");
        }
    }

    // SJF — Shortest Job First (Non-Preemptive)
    private void runSJF() throws InterruptedException {

        while (true) {

            ProcessInfo chosen = null;
            synchronized (SimulatorCore.activeQueue) {
                if (!SimulatorCore.activeQueue.isEmpty()) {
                    chosen = SimulatorCore.activeQueue.stream()
                            .min(Comparator.comparingInt(ProcessInfo::getTimeLeft)
                                    .thenComparingInt(ProcessInfo::getAdmittedAt))
                            .orElse(null);
                }
            }

            //wait for ResourceManager to load 
            if (chosen == null) {
                synchronized (SimulatorCore.pendingQueue) {
                    if (SimulatorCore.pendingQueue.isEmpty()
                            && SimulatorCore.activeQueue.isEmpty()) break;
                }
                Thread.sleep(1);
                continue;
            }

            if (chosen.getFirstRun() == -1) chosen.setFirstRun(SimulatorCore.clock);

            int burstBefore = chosen.getTimeLeft();
            chosen.setStatus("RUNNING");
            SimulatorCore.ganttTicks.add(SimulatorCore.clock);
            SimulatorCore.clock += burstBefore;
            chosen.setTimeLeft(0);
            SimulatorCore.ganttLabels.add(
                    "P" + chosen.getPid() + "(" + burstBefore + "->0)");

            synchronized (SimulatorCore.activeQueue) {
                OSKernel.recordCompletion(chosen, SimulatorCore.clock);
                SimulatorCore.activeQueue.remove(chosen);   // remove FIRST
                OSKernel.freeMemory(chosen);                // then free memory
            }
            SimulatorCore.completedList.add(chosen);
            Thread.sleep(1);
        }

        SimulatorCore.ganttTicks.add(SimulatorCore.clock);
    }

    // RR — Round Robin (quantum = 5 ms)
    private void runRR() throws InterruptedException {

        while (true) {

            ProcessInfo chosen;
            synchronized (SimulatorCore.activeQueue) {
                if (SimulatorCore.activeQueue.isEmpty()) {
                    synchronized (SimulatorCore.pendingQueue) {
                        if (SimulatorCore.pendingQueue.isEmpty()) break;
                    }
                    Thread.sleep(1);
                    continue;
                }
                chosen = SimulatorCore.activeQueue.remove(0);
            }

            if (chosen.getFirstRun() == -1) chosen.setFirstRun(SimulatorCore.clock);

            int burstBefore = chosen.getTimeLeft();
            int slice       = Math.min(QUANTUM, burstBefore);
            chosen.setStatus("RUNNING");
            SimulatorCore.ganttTicks.add(SimulatorCore.clock);
            SimulatorCore.clock += slice;
            chosen.setTimeLeft(burstBefore - slice);
            int burstAfter = chosen.getTimeLeft();
            SimulatorCore.ganttLabels.add(
                    "P" + chosen.getPid() + "(" + burstBefore + "->" + burstAfter + ")");

            if (chosen.getTimeLeft() == 0) {
                synchronized (SimulatorCore.activeQueue) {
                    OSKernel.recordCompletion(chosen, SimulatorCore.clock);
                    OSKernel.freeMemory(chosen);   // freeMemory
                }
                SimulatorCore.completedList.add(chosen);
            } else {
                chosen.setStatus("READY");
                synchronized (SimulatorCore.activeQueue) {
                    SimulatorCore.activeQueue.add(chosen);
                }
            }

            Thread.sleep(1);
        }

        SimulatorCore.ganttTicks.add(SimulatorCore.clock);
    }

    // PRIORITY — Non-Preemptive with Aging
   
    private void runPriority() throws InterruptedException {

        while (true) {

            ProcessInfo chosen = null;
            synchronized (SimulatorCore.activeQueue) {
                if (!SimulatorCore.activeQueue.isEmpty()) {
                    chosen = SimulatorCore.activeQueue.stream()
                            .min(Comparator.comparingInt(ProcessInfo::getCurrentPriority)
                                    .thenComparingInt(ProcessInfo::getAdmittedAt))
                            .orElse(null);
                }
            }

            if (chosen == null) {
                synchronized (SimulatorCore.pendingQueue) {
                    if (SimulatorCore.pendingQueue.isEmpty()
                            && SimulatorCore.activeQueue.isEmpty()) break;
                }
                Thread.sleep(1);
                continue;
            }

            if (chosen.getFirstRun() == -1) chosen.setFirstRun(SimulatorCore.clock);

            int burstBefore = chosen.getTimeLeft();
            chosen.setStatus("RUNNING");
            SimulatorCore.ganttTicks.add(SimulatorCore.clock);

            // check starvation & aging each tick
            while (chosen.getTimeLeft() > 0) {
                SimulatorCore.clock++;
                chosen.setTimeLeft(chosen.getTimeLeft() - 1);

                synchronized (SimulatorCore.activeQueue) {
                    final ProcessInfo running = chosen;
                    int N = (int) SimulatorCore.activeQueue.stream()
                            .filter(p -> p != running)
                            .count();

                    for (ProcessInfo proc : SimulatorCore.activeQueue) {
                        if (proc == chosen) continue;   // skip the running process

                        int waited = SimulatorCore.clock - proc.getAdmittedAt();

                        // Starvation: waited more than N * 5 ms without getting CPU
                        if (waited > N * 5) {
                            proc.setHungry(true);
                        }

                        
                        if ((SimulatorCore.clock - proc.getAgingAppliedAt()) >= 4
                                && proc.getCurrentPriority() > 1) {
                            proc.setCurrentPriority(proc.getCurrentPriority() - 1);
                            proc.setAgingAppliedAt(SimulatorCore.clock);
                        }
                    }
                }

                Thread.sleep(1);
            }

            String star = chosen.isHungry() ? "*" : "";
            SimulatorCore.ganttLabels.add(
                    "P" + chosen.getPid() + star + "(" + burstBefore + "->0)");

            synchronized (SimulatorCore.activeQueue) {
                OSKernel.recordCompletion(chosen, SimulatorCore.clock);
                SimulatorCore.activeQueue.remove(chosen);   // remove FIRST
                OSKernel.freeMemory(chosen);                // then free memory
            }
            SimulatorCore.completedList.add(chosen);
        }

        SimulatorCore.ganttTicks.add(SimulatorCore.clock);
    }
}