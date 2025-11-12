import java.util.*;

class Process {
    String pid;
    int arrival, burst, priority;
    int completion, turnaround, waiting;
    boolean completed = false;

    Process(String pid, int arrival, int burst, int priority) {
        this.pid = pid;
        this.arrival = arrival;
        this.burst = burst;
        this.priority = priority;
    }
}

public class CPUSchedulingPriority {

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        System.out.println("CPU Scheduling Simulation");
        System.out.print("Enter number of processes: ");
        int n = sc.nextInt();

        ArrayList<Process> plist = new ArrayList<>();

        for (int i = 0; i < n; i++) {
            System.out.print("Enter Process ID: ");
            String pid = sc.next();
            System.out.print("Enter Arrival Time: ");
            int at = sc.nextInt();
            System.out.print("Enter Burst Time: ");
            int bt = sc.nextInt();
            System.out.print("Enter Priority (lower value = higher priority): ");
            int pr = sc.nextInt();
            plist.add(new Process(pid, at, bt, pr));
        }

        System.out.println("\n--- FCFS Scheduling ---");
        fcfs(new ArrayList<>(plist));

        System.out.println("\n--- Priority (Non-Preemptive) Scheduling ---");
        priorityNonPreemptive(new ArrayList<>(plist));
    }

    static void fcfs(ArrayList<Process> plist) {
        plist.sort(Comparator.comparingInt(p -> p.arrival));

        int time = 0;
        for (Process p : plist) {
            if (time < p.arrival)
                time = p.arrival;
            time += p.burst;
            p.completion = time;
            p.turnaround = p.completion - p.arrival;
            p.waiting = p.turnaround - p.burst;
        }

        printResults(plist);
    }

    static void priorityNonPreemptive(ArrayList<Process> plist) {
        int time = 0, completed = 0, n = plist.size();

        while (completed < n) {
            Process best = null;
            int bestPriority = Integer.MAX_VALUE;

            for (Process p : plist) {
                if (!p.completed && p.arrival <= time && p.priority < bestPriority) {
                    bestPriority = p.priority;
                    best = p;
                }
            }

            if (best == null) {
                time++;
                continue;
            }

            time += best.burst;
            best.completion = time;
            best.turnaround = best.completion - best.arrival;
            best.waiting = best.turnaround - best.burst;
            best.completed = true;
            completed++;
        }

        printResults(plist);
    }

    static void printResults(ArrayList<Process> plist) {
        double avgWT = 0, avgTAT = 0;

        System.out.println("\nPID\tAT\tBT\tPR\tCT\tTAT\tWT");
        for (Process p : plist) {
            System.out.printf("%s\t%d\t%d\t%d\t%d\t%d\t%d\n",
                    p.pid, p.arrival, p.burst, p.priority,
                    p.completion, p.turnaround, p.waiting);
            avgWT += p.waiting;
            avgTAT += p.turnaround;
        }

        avgWT /= plist.size();
        avgTAT /= plist.size();

        System.out.printf("\nAverage Waiting Time: %.2f", avgWT);
        System.out.printf("\nAverage Turnaround Time: %.2f\n", avgTAT);
    }
}
