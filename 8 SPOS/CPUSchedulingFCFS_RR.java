import java.util.*;

public class CPUSchedulingFCFS_RR {
    static class Process {
        String name;
        int arrivalTime, burstTime, remainingTime, completionTime, waitingTime, turnaroundTime;
        Process(String n, int at, int bt) {
            name = n;
            arrivalTime = at;
            burstTime = bt;
            remainingTime = bt;
        }
    }

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        List<Process> processes = new ArrayList<>();
        processes.add(new Process("P1", 0, 6));
        processes.add(new Process("P2", 1, 4));
        processes.add(new Process("P3", 4, 8));
        processes.add(new Process("P4", 3, 3));

        System.out.println("CPU Scheduling Simulation");
        System.out.println("1. FCFS");
        System.out.println("2. Round Robin (Time Quantum = 2)");
        System.out.print("Enter your choice: ");
        int choice = sc.nextInt();

        if (choice == 1)
            simulateFCFS(processes);
        else if (choice == 2)
            simulateRR(processes, 2);
        else
            System.out.println("Invalid choice!");
    }

    static void simulateFCFS(List<Process> plist) {
        plist.sort(Comparator.comparingInt(p -> p.arrivalTime));
        int currentTime = 0;
        double totalWT = 0, totalTAT = 0;

        System.out.println("\n--- FCFS Scheduling ---");
        System.out.println("Process\tAT\tBT\tCT\tTAT\tWT");

        for (Process p : plist) {
            if (currentTime < p.arrivalTime)
                currentTime = p.arrivalTime;
            p.completionTime = currentTime + p.burstTime;
            p.turnaroundTime = p.completionTime - p.arrivalTime;
            p.waitingTime = p.turnaroundTime - p.burstTime;
            currentTime = p.completionTime;

            totalWT += p.waitingTime;
            totalTAT += p.turnaroundTime;

            System.out.printf("%s\t%d\t%d\t%d\t%d\t%d\n",
                    p.name, p.arrivalTime, p.burstTime,
                    p.completionTime, p.turnaroundTime, p.waitingTime);
        }

        System.out.printf("Average Waiting Time: %.2f\n", totalWT / plist.size());
        System.out.printf("Average Turnaround Time: %.2f\n", totalTAT / plist.size());
    }

    static void simulateRR(List<Process> plist, int quantum) {
        int n = plist.size();
        plist.sort(Comparator.comparingInt(p -> p.arrivalTime));
        Queue<Process> queue = new LinkedList<>();
        int currentTime = 0, completed = 0;
        double totalWT = 0, totalTAT = 0;
        Set<Process> added = new HashSet<>();

        System.out.println("\n--- Round Robin Scheduling (Time Quantum = " + quantum + ") ---");
        System.out.println("Gantt Chart:");

        while (completed < n) {
            for (Process p : plist) {
                if (p.arrivalTime <= currentTime && p.remainingTime > 0 && !added.contains(p)) {
                    queue.add(p);
                    added.add(p);
                }
            }

            if (queue.isEmpty()) {
                currentTime++;
                continue;
            }

            Process p = queue.poll();
            System.out.print("| " + p.name + " ");
            int execTime = Math.min(p.remainingTime, quantum);
            currentTime += execTime;
            p.remainingTime -= execTime;

            for (Process next : plist) {
                if (next.arrivalTime <= currentTime && next.remainingTime > 0 && !added.contains(next)) {
                    queue.add(next);
                    added.add(next);
                }
            }

            if (p.remainingTime > 0) {
                queue.add(p);
            } else {
                p.completionTime = currentTime;
                p.turnaroundTime = p.completionTime - p.arrivalTime;
                p.waitingTime = p.turnaroundTime - p.burstTime;
                totalWT += p.waitingTime;
                totalTAT += p.turnaroundTime;
                completed++;
            }
        }
        System.out.println("|");

        System.out.println("\nProcess\tAT\tBT\tCT\tTAT\tWT");
        for (Process p : plist) {
            System.out.printf("%s\t%d\t%d\t%d\t%d\t%d\n",
                    p.name, p.arrivalTime, p.burstTime,
                    p.completionTime, p.turnaroundTime, p.waitingTime);
        }

        System.out.printf("Average Waiting Time: %.2f\n", totalWT / n);
        System.out.printf("Average Turnaround Time: %.2f\n", totalTAT / n);
    }
}
