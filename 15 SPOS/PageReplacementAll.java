import java.util.*;

public class PageReplacementAll {

    public static void main(String[] args) {
        int[] referenceString = {2, 3, 2, 1, 5, 2, 4, 5, 3, 2, 5, 2};
        int frames = 3;

        System.out.println("Reference String: " + Arrays.toString(referenceString));
        System.out.println("\n--- FIFO ---");
        simulateFIFO(referenceString, frames);

        System.out.println("\n--- LRU ---");
        simulateLRU(referenceString, frames);

        System.out.println("\n--- OPTIMAL ---");
        simulateOptimal(referenceString, frames);
    }

    // FIFO
    static void simulateFIFO(int[] ref, int frames) {
        Set<Integer> memory = new HashSet<>();
        Queue<Integer> queue = new LinkedList<>();
        int pageFaults = 0;

        for (int page : ref) {
            if (!memory.contains(page)) {
                if (memory.size() == frames) {
                    int removed = queue.poll();
                    memory.remove(removed);
                }
                memory.add(page);
                queue.add(page);
                pageFaults++;
            }
        }
        printResults("FIFO", ref.length, pageFaults);
    }

    // LRU
    static void simulateLRU(int[] ref, int frames) {
        Set<Integer> memory = new HashSet<>();
        Map<Integer, Integer> recentUse = new HashMap<>();
        int pageFaults = 0;

        for (int i = 0; i < ref.length; i++) {
            int page = ref[i];
            if (!memory.contains(page)) {
                if (memory.size() == frames) {
                    // remove least recently used
                    int lruPage = -1;
                    int minIndex = Integer.MAX_VALUE;
                    for (int p : memory) {
                        int lastUsed = recentUse.getOrDefault(p, -1);
                        if (lastUsed < minIndex) {
                            minIndex = lastUsed;
                            lruPage = p;
                        }
                    }
                    memory.remove(lruPage);
                    recentUse.remove(lruPage);
                }
                memory.add(page);
                pageFaults++;
            }
            recentUse.put(page, i);
        }
        printResults("LRU", ref.length, pageFaults);
    }

    // Optimal
    static void simulateOptimal(int[] ref, int frames) {
        Set<Integer> memory = new HashSet<>();
        int pageFaults = 0;

        for (int i = 0; i < ref.length; i++) {
            int page = ref[i];
            if (!memory.contains(page)) {
                if (memory.size() == frames) {
                    // remove the page that will not be used for the longest time
                    int farthest = -1;
                    int pageToRemove = -1;
                    for (int memPage : memory) {
                        int nextUse = Integer.MAX_VALUE;
                        for (int j = i + 1; j < ref.length; j++) {
                            if (ref[j] == memPage) {
                                nextUse = j;
                                break;
                            }
                        }
                        if (nextUse > farthest) {
                            farthest = nextUse;
                            pageToRemove = memPage;
                        }
                    }
                    memory.remove(pageToRemove);
                }
                memory.add(page);
                pageFaults++;
            }
        }
        printResults("OPTIMAL", ref.length, pageFaults);
    }

    // Print Page Faults and Hit Ratio
    static void printResults(String algo, int totalPages, int pageFaults) {
        double hitRatio = (totalPages - pageFaults) / (double) totalPages;
        System.out.println(algo + " -> Page Faults: " + pageFaults + ", Hit Ratio: " + String.format("%.2f", hitRatio));
    }
}
