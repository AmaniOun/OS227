package com.mycompany.os;


public class ResourceManager implements Runnable {

    @Override
    public void run() {

        while (SimulatorCore.loaderRunning || !SimulatorCore.pendingQueue.isEmpty()) {

            boolean promoted = false;

            synchronized (SimulatorCore.activeQueue) {

                for (int i = 0; i < SimulatorCore.pendingQueue.size(); ) {
                    ProcessInfo proc = SimulatorCore.pendingQueue.get(i);

                    if (proc.getMemorySize() <= SimulatorCore.freeMemory) {
                        SimulatorCore.pendingQueue.remove(i);
                        SimulatorCore.activeQueue.add(proc);
                        SimulatorCore.freeMemory -= proc.getMemorySize();
                        proc.setStatus("READY");

                        // Record the exact time this process entered the ready queue
                        proc.setAdmittedAt(SimulatorCore.clock);

                        
                        proc.setAgingAppliedAt(SimulatorCore.clock);

                        System.out.printf(
                            "[MemoryLoader] Loaded job P%d at time %d, requiredMem=%d, remaining mem=%d%n",
                            proc.getPid(),
                            SimulatorCore.clock,
                            proc.getMemorySize(),
                            SimulatorCore.freeMemory
                        );

                        SimulatorCore.activeQueue.notifyAll();
                        promoted = true;

                    } else {
                        i++;
                    }
                }

                if (!promoted) {
                    try {
                        SimulatorCore.activeQueue.wait(1);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
        }

        System.out.println("[MemoryLoader] Exiting.");
    }
}