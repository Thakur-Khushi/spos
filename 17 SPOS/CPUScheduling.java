import java.util.*;

class Process {
    String name;
    int arrivalTime;
    int burstTime;
    int priority;
    int startTime;
    int completionTime;
    int turnaroundTime;
    int waitingTime;

    Process(String name, int arrivalTime, int burstTime, int priority) {
        this.name = name;
        this.arrivalTime = arrivalTime;
        this.burstTime = burstTime;
        this.priority = priority;
    }
}

public class CPUScheduling {

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        // Sample processes
        List<Process> processes = new ArrayList<>();
        processes.add(new Process("P1", 0, 4, 4));
        processes.add(new Process("P2", 1, 3, 3));
        processes.add(new Process("P3", 3, 4, 1));
        processes.add(new Process("P4", 6, 2, 5));
        processes.add(new Process("P1", 8, 4, 2));

        System.out.println("Choose Scheduling Algorithm: 1.FCFS  2.Priority Non-Preemptive");
        int choice = sc.nextInt();

        if (choice == 1) {
            simulateFCFS(processes);
        } else if (choice == 2) {
            simulatePriority(processes);
        } else {
            System.out.println("Invalid choice!");
        }

        sc.close();
    }

    static void simulateFCFS(List<Process> processes) {
        // Sort by arrival time
        processes.sort(Comparator.comparingInt(p -> p.arrivalTime));
        int currentTime = 0;

        System.out.println("\n----- FCFS Scheduling -----");
        System.out.printf("%-5s %-5s %-5s %-5s %-5s %-5s %-5s\n",
                "Name", "AT", "BT", "ST", "CT", "TAT", "WT");

        double totalTAT = 0, totalWT = 0;

        for (Process p : processes) {
            if (currentTime < p.arrivalTime) currentTime = p.arrivalTime;
            p.startTime = currentTime;
            p.completionTime = p.startTime + p.burstTime;
            p.turnaroundTime = p.completionTime - p.arrivalTime;
            p.waitingTime = p.startTime - p.arrivalTime;

            currentTime += p.burstTime;

            totalTAT += p.turnaroundTime;
            totalWT += p.waitingTime;

            System.out.printf("%-5s %-5d %-5d %-5d %-5d %-5d %-5d\n",
                    p.name, p.arrivalTime, p.burstTime, p.startTime,
                    p.completionTime, p.turnaroundTime, p.waitingTime);
        }

        System.out.printf("Average Turnaround Time: %.2f\n", totalTAT / processes.size());
        System.out.printf("Average Waiting Time: %.2f\n", totalWT / processes.size());
    }

    static void simulatePriority(List<Process> processes) {
        List<Process> completed = new ArrayList<>();
        List<Process> readyQueue = new ArrayList<>();
        int currentTime = 0;

        System.out.println("\n----- Priority Non-Preemptive Scheduling -----");
        System.out.printf("%-5s %-5s %-5s %-5s %-5s %-5s %-5s\n",
                "Name", "AT", "BT", "PR", "ST", "CT", "TAT", "WT");

        double totalTAT = 0, totalWT = 0;

        List<Process> remaining = new ArrayList<>(processes);

        while (!remaining.isEmpty() || !readyQueue.isEmpty()) {
            // Add processes to ready queue whose arrival time <= currentTime
            Iterator<Process> it = remaining.iterator();
            while (it.hasNext()) {
                Process p = it.next();
                if (p.arrivalTime <= currentTime) {
                    readyQueue.add(p);
                    it.remove();
                }
            }

            if (readyQueue.isEmpty()) {
                currentTime++;
                continue;
            }

            // Pick process with highest priority (smallest number)
            readyQueue.sort(Comparator.comparingInt(p -> p.priority));
            Process p = readyQueue.remove(0);

            if (currentTime < p.arrivalTime) currentTime = p.arrivalTime;
            p.startTime = currentTime;
            p.completionTime = p.startTime + p.burstTime;
            p.turnaroundTime = p.completionTime - p.arrivalTime;
            p.waitingTime = p.startTime - p.arrivalTime;

            currentTime += p.burstTime;
            completed.add(p);

            totalTAT += p.turnaroundTime;
            totalWT += p.waitingTime;

            System.out.printf("%-5s %-5d %-5d %-5d %-5d %-5d %-5d\n",
                    p.name, p.arrivalTime, p.burstTime, p.priority,
                    p.startTime, p.completionTime, p.turnaroundTime, p.waitingTime);
        }

        System.out.printf("Average Turnaround Time: %.2f\n", totalTAT / processes.size());
        System.out.printf("Average Waiting Time: %.2f\n", totalWT / processes.size());
    }
}
