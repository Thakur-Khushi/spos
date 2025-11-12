import java.util.*;

public class PageReplacement {

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        System.out.println("Enter number of frames: ");
        int frames = sc.nextInt();

        System.out.println("Enter number of page references: ");
        int n = sc.nextInt();

        int[] pages = new int[n];
        System.out.println("Enter the page reference string:");
        for (int i = 0; i < n; i++) {
            pages[i] = sc.nextInt();
        }

        System.out.println("\n--- FIFO Page Replacement ---");
        fifo(pages, frames);

        System.out.println("\n--- LRU Page Replacement ---");
        lru(pages, frames);
    }

    static void fifo(int[] pages, int frames) {
        Set<Integer> set = new HashSet<>();
        Queue<Integer> queue = new LinkedList<>();
        int pageFaults = 0;

        for (int page : pages) {
            if (!set.contains(page)) {
                if (set.size() < frames) {
                    set.add(page);
                    queue.add(page);
                } else {
                    int removed = queue.poll();
                    set.remove(removed);
                    set.add(page);
                    queue.add(page);
                }
                pageFaults++;
                System.out.println("Page " + page + " -> Fault | Frames: " + set);
            } else {
                System.out.println("Page " + page + " -> Hit   | Frames: " + set);
            }
        }

        System.out.println("Total Page Faults (FIFO): " + pageFaults);
    }

    static void lru(int[] pages, int frames) {
        Set<Integer> set = new HashSet<>();
        Map<Integer, Integer> recent = new HashMap<>();
        int pageFaults = 0;

        for (int i = 0; i < pages.length; i++) {
            int page = pages[i];

            if (!set.contains(page)) {
                if (set.size() < frames) {
                    set.add(page);
                } else {
                    // Find least recently used page
                    int lru = Integer.MAX_VALUE, val = 0;
                    for (int p : set) {
                        if (recent.get(p) < lru) {
                            lru = recent.get(p);
                            val = p;
                        }
                    }
                    set.remove(val);
                    set.add(page);
                }
                pageFaults++;
                System.out.println("Page " + page + " -> Fault | Frames: " + set);
            } else {
                System.out.println("Page " + page + " -> Hit   | Frames: " + set);
            }

            recent.put(page, i); // update most recent use
        }

        System.out.println("Total Page Faults (LRU): " + pageFaults);
    }
}
