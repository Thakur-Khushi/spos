import java.util.*;

public class PageReplacementFIFOandLRU {

    public static void main(String[] args) {
        int[] referenceString = {0, 2, 1, 6, 4, 0, 1, 0, 3, 1, 2, 1};
        int frames = 3;

        System.out.println("Reference String: " + Arrays.toString(referenceString));

        System.out.println("\n--- FIFO Page Replacement ---");
        fifo(referenceString, frames);

        System.out.println("\n--- LRU Page Replacement ---");
        lru(referenceString, frames);
    }

    // FIFO Page Replacement
    static void fifo(int[] ref, int frames) {
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
                System.out.println("Page " + page + " -> Page Fault | Memory: " + memory);
            } else {
                System.out.println("Page " + page + " -> Hit | Memory: " + memory);
            }
        }
        System.out.println("Total Page Faults (FIFO): " + pageFaults);
    }

    // LRU Page Replacement
    static void lru(int[] ref, int frames) {
        Set<Integer> memory = new HashSet<>();
        Map<Integer, Integer> recentUse = new HashMap<>(); // page -> last used index
        int pageFaults = 0;

        for (int i = 0; i < ref.length; i++) {
            int page = ref[i];

            if (!memory.contains(page)) {
                if (memory.size() == frames) {
                    // remove least recently used page
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
                System.out.println("Page " + page + " -> Page Fault | Memory: " + memory);
            } else {
                System.out.println("Page " + page + " -> Hit | Memory: " + memory);
            }

            // update last used index
            recentUse.put(page, i);
        }
        System.out.println("Total Page Faults (LRU): " + pageFaults);
    }
}
