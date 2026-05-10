package com.mycompany.os;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;


public class SimulatorCore {

    public static volatile int clock = 0;
    public static volatile int freeMemory = 2048;
    public static String algorithm;
    public static final List<ProcessInfo> pendingQueue = new ArrayList<>();
    public static final List<ProcessInfo> activeQueue = new ArrayList<>();
    public static final List<ProcessInfo> completedList = new ArrayList<>();
    public static List<String>  ganttLabels = new ArrayList<>();
    public static List<Integer> ganttTicks  = new ArrayList<>();
    public static volatile boolean loaderRunning = true;

    public static void selectAlgorithm() {
        Scanner sc = new Scanner(System.in);
        System.out.println("\nSelect Scheduling Algorithm:");
        System.out.println("  1. SJF  (Shortest Job First) - Non-Preemptive");
        System.out.println("  2. RR   (Round Robin)        - Quantum = 5 ms");
        System.out.println("  3. PRIO (Priority Scheduling)- Non-Preemptive");
        System.out.print("Your choice: ");
        int choice = sc.nextInt();
        switch (choice) {
            case 1: algorithm = "SJF";  break;
            case 2: algorithm = "RR";   break;
            case 3: algorithm = "PRIO"; break;
            default:
                System.out.println("Invalid choice. Defaulting to SJF.");
                algorithm = "SJF";
        }
    }
public static void printPrettyGanttChart() {
    StringBuilder topRow = new StringBuilder();
    for (String label : ganttLabels) {
        topRow.append("| ").append(label).append(" ");
    }
    topRow.append("|");

    StringBuilder tickRow = new StringBuilder();
    for (int i = 0; i < ganttLabels.size(); i++) {
        String label = ganttLabels.get(i);
        int cellWidth = label.length() + 3; // "| " + label + " "
        String tick = String.valueOf(ganttTicks.get(i));
        tickRow.append(tick);
        for (int p = tick.length(); p < cellWidth; p++) tickRow.append(" ");
    }
    if (!ganttTicks.isEmpty()) {
        tickRow.append(ganttTicks.get(ganttTicks.size() - 1));
    }

    String separator = "-".repeat(topRow.length());

    System.out.println("\nGantt Chart:");
    System.out.println(separator);
    System.out.println(topRow);
    System.out.println(separator);
    System.out.println(tickRow);
    System.out.println("* indicates a starved process");
}

    public static void printProcessTable() {
        System.out.println("\n====================================================================================");
        System.out.println("Process Execution Table");
        System.out.println("====================================================================================");
        System.out.printf("%-8s %-6s %-6s %-6s %-10s %-12s %-9s %-10s %-8s%n",
            "Process", "Burst", "Start", "End", "Waiting", "Turnaround",
            "InitPrio", "FinalPrio", "Starved");
        completedList.sort((a, b) -> Integer.compare(a.getPid(), b.getPid()));
        for (ProcessInfo proc : completedList) {
            System.out.printf("%-8s %-6d %-6d %-6d %-10d %-12d %-9d %-10d %-8s%n",
                "P" + proc.getPid(), proc.getCpuBurst(), proc.getFirstRun(),
                proc.getFinishTime(), proc.getWt(), proc.getTat(),
                proc.getOriginalPriority(), proc.getCurrentPriority(),
                proc.isHungry() ? "YES" : "NO");
        }
        System.out.println("====================================================================================");
    }

    public static void main(String[] args) throws InterruptedException {

        FileProcessor fileProc = new FileProcessor("job.txt");
        Thread readerThread = new Thread(fileProc, "JobReader");
        readerThread.start();
        readerThread.join();

        selectAlgorithm();

        ResourceManager resMgr = new ResourceManager();
        Thread loaderThread = new Thread(resMgr, "MemoryLoader");
        loaderRunning = true;
        loaderThread.start();

        System.out.println("\nMemory Loader :");

        CPUDispatcher dispatcher = new CPUDispatcher();
        dispatcher.dispatch();

        loaderRunning = false;
        synchronized (activeQueue) { activeQueue.notifyAll(); }
        loaderThread.join();

        double sumWT = 0, sumTAT = 0;
        for (ProcessInfo proc : completedList) {
            sumWT  += proc.getWt();
            sumTAT += proc.getTat();
        }
        int n = completedList.size();
        double avgWT  = (n == 0) ? 0 : sumWT  / n;
        double avgTAT = (n == 0) ? 0 : sumTAT / n;

        printPrettyGanttChart();
        printProcessTable();

        if (algorithm.equals("PRIO")) {
            printStarvationReport();
        }

        System.out.println("\nStatistics:");
        System.out.printf("Average Turnaround Time: %.2f ms%n", avgTAT);
        System.out.printf("Average Waiting Time: %.2f ms%n", avgWT);
    }

    public static void printStarvationReport() {
        System.out.println("\n--- Starvation Report (Priority Scheduling Only) ---");
        boolean anyStarved = false;
        for (ProcessInfo proc : completedList) {
            if (proc.isHungry()) {
                System.out.printf("Process P%d suffered from starvation (original priority: %d, final priority: %d).%n",
                    proc.getPid(), proc.getOriginalPriority(), proc.getCurrentPriority());
                anyStarved = true;
            }
        }
        if (!anyStarved) System.out.println("No processes suffered from starvation.");
    }
}