import java.util.*;

public class PageReplacementFIFOAndLRU {
    public static void main(String[] args) {
        int[] pages = {7, 0, 1, 2, 0, 3, 0, 4, 2, 3, 0, 3, 2, 3};
        int frameSize = 4;

        System.out.println("Page Replacement Simulation (Frame Size = " + frameSize + ")\n");

        fifo(pages, frameSize);
        lru(pages, frameSize);
    }

    // ---------------- FIFO ----------------
    static void fifo(int[] pages, int frameSize) {
        System.out.println("----- FIFO -----");
        Set<Integer> frame = new HashSet<>();
        Queue<Integer> queue = new LinkedList<>();
        int pageFaults = 0;

        for (int page : pages) {
            if (!frame.contains(page)) {
                if (frame.size() == frameSize) {
                    int removed = queue.poll();
                    frame.remove(removed);
                }
                frame.add(page);
                queue.add(page);
                pageFaults++;
            }
            System.out.println("Frame: " + frame);
        }

        double hitRatio = (double) (pages.length - pageFaults) / pages.length;
        System.out.println("Page Faults: " + pageFaults);
        System.out.printf("Hit Ratio: %.2f\n\n", hitRatio);
    }

    // ---------------- LRU ----------------
    static void lru(int[] pages, int frameSize) {
        System.out.println("----- LRU -----");
        List<Integer> frame = new ArrayList<>();
        int pageFaults = 0;

        for (int page : pages) {
            if (!frame.contains(page)) {
                if (frame.size() == frameSize) {
                    // remove least recently used
                    frame.remove(0);
                }
                frame.add(page);
                pageFaults++;
            } else {
                // move to most recently used
                frame.remove((Integer) page);
                frame.add(page);
            }
            System.out.println("Frame: " + frame);
        }

        double hitRatio = (double) (pages.length - pageFaults) / pages.length;
        System.out.println("Page Faults: " + pageFaults);
        System.out.printf("Hit Ratio: %.2f\n\n", hitRatio);
    }
}
