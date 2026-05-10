package com.mycompany.os;


public class ProcessInfo {

    private int pid;                      // Process identifier
    private String status;                // NEW, READY, RUNNING, TERMINATED
    private int cpuBurst;                 // Original CPU burst time (ms)
    private int timeLeft;                 // Remaining CPU time
    private int currentPriority;          // Priority (may change via aging)
    private int originalPriority;         // Initial priority (saved for report)
    private final int memorySize;         // Memory required (MB)
    private int tat;                      // Turnaround time
    private int wt;                       // Waiting time
    private int firstRun;                 // Time of first CPU dispatch
    private int finishTime;               // Time process completed
    private int admittedAt;              // Time process entered the ready queue
    private boolean isHungry;            // True if process experienced starvation
    private int agingAppliedAt;          // Last time aging was applied

    public ProcessInfo(int pid, int cpuBurst, int currentPriority, int memorySize) {
        this.pid              = pid;
        this.cpuBurst         = cpuBurst;
        this.timeLeft         = cpuBurst;
        this.currentPriority  = currentPriority;
        this.originalPriority = currentPriority;
        this.memorySize       = memorySize;
        this.status           = "NEW";
        this.tat              = 0;
        this.wt               = 0;
        this.firstRun         = -1;
        this.finishTime       = 0;
        this.admittedAt       = -1;
        this.isHungry         = false;
       
        this.agingAppliedAt   = -1;
    }

    // Getters & Setters

    public int getPid()                          { return pid; }
    public void setPid(int pid)                  { this.pid = pid; }

    public String getStatus()                    { return status; }
    public void setStatus(String status)         { this.status = status; }

    public int getCpuBurst()                     { return cpuBurst; }
    public void setCpuBurst(int cpuBurst)        { this.cpuBurst = cpuBurst; }

    public int getTimeLeft()                     { return timeLeft; }
    public void setTimeLeft(int timeLeft)        { this.timeLeft = timeLeft; }

    public int getCurrentPriority()              { return currentPriority; }
    public void setCurrentPriority(int p)        { this.currentPriority = p; }

    public int getOriginalPriority()             { return originalPriority; }

    public int getMemorySize()                   { return memorySize; }

    public int getTat()                          { return tat; }
    public void setTat(int tat)                  { this.tat = tat; }

    public int getWt()                           { return wt; }
    public void setWt(int wt)                    { this.wt = wt; }

    public int getFirstRun()                     { return firstRun; }
    public void setFirstRun(int firstRun)        { this.firstRun = firstRun; }

    public int getFinishTime()                   { return finishTime; }
    public void setFinishTime(int finishTime)    { this.finishTime = finishTime; }

    public int getAdmittedAt()                   { return admittedAt; }
    public void setAdmittedAt(int admittedAt)    { this.admittedAt = admittedAt; }

    public boolean isHungry()                    { return isHungry; }
    public void setHungry(boolean hungry)        { this.isHungry = hungry; }

    public int getAgingAppliedAt()               { return agingAppliedAt; }
    public void setAgingAppliedAt(int t)         { this.agingAppliedAt = t; }
}