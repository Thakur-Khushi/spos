import java.util.*;

class Process {
    String pid;
    int arrival, burst, remaining, completion, turnaround, waiting;
    boolean completed = false;

    Process(String pid, int arrival, int burst) {
        this.pid = pid;
        this.arrival = arrival;
        this.burst = burst;
        this.remaining = burst;
    }
}

public class CPUScheduling {

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        System.out.println("CPU Scheduling Simulation");
        System.out.println("Enter number of processes:");
        int n = sc.nextInt();

        ArrayList<Process> processes = new ArrayList<>();

        for (int i = 0; i < n; i++) {
            System.out.print("Enter Process ID: ");
            String pid = sc.next();
            System.out.print("Enter Arrival Time: ");
            int at = sc.nextInt();
            System.out.print("Enter Burst Time: ");
            int bt = sc.nextInt();
            processes.add(new Process(pid, at, bt));
        }

        System.out.println("\n--- FCFS Scheduling ---");
        fcfs(new ArrayList<>(processes));

        System.out.println("\n--- SJF (Preemptive) Scheduling ---");
        sjfPreemptive(new ArrayList<>(processes));
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

    static void sjfPreemptive(ArrayList<Process> plist) {
        int time = 0, completed = 0, n = plist.size();
        double avgWT = 0, avgTAT = 0;

        while (completed < n) {
            Process shortest = null;
            int minBurst = Integer.MAX_VALUE;

            for (Process p : plist) {
                if (!p.completed && p.arrival <= time && p.remaining < minBurst && p.remaining > 0) {
                    minBurst = p.remaining;
                    shortest = p;
                }
            }

            if (shortest == null) {
                time++;
                continue;
            }

            shortest.remaining--;
            time++;

            if (shortest.remaining == 0) {
                shortest.completed = true;
                shortest.completion = time;
                shortest.turnaround = shortest.completion - shortest.arrival;
                shortest.waiting = shortest.turnaround - shortest.burst;
                completed++;
            }
        }

        printResults(plist);
    }

    static void printResults(ArrayList<Process> plist) {
        double avgWT = 0, avgTAT = 0;

        System.out.println("\nPID\tAT\tBT\tCT\tTAT\tWT");
        for (Process p : plist) {
            System.out.printf("%s\t%d\t%d\t%d\t%d\t%d\n",
                    p.pid, p.arrival, p.burst, p.completion, p.turnaround, p.waiting);
            avgWT += p.waiting;
            avgTAT += p.turnaround;
        }

        avgWT /= plist.size();
        avgTAT /= plist.size();

        System.out.printf("\nAverage Waiting Time: %.2f", avgWT);
        System.out.printf("\nAverage Turnaround Time: %.2f\n", avgTAT);
    }
}
