import java.util.*;

public class PageReplacementAlgorithms {
    public static void main(String[] args) {
        int[] pages = {2, 3, 2, 1, 5, 2, 4, 5, 3, 2, 5, 2};
        int frameSize = 3;

        System.out.println("Page Replacement Simulation (Frame Size = " + frameSize + ")\n");

        fifo(pages, frameSize);
        lru(pages, frameSize);
        optimal(pages, frameSize);
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

        double hitRatio = (double)(pages.length - pageFaults) / pages.length;
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

        double hitRatio = (double)(pages.length - pageFaults) / pages.length;
        System.out.println("Page Faults: " + pageFaults);
        System.out.printf("Hit Ratio: %.2f\n\n", hitRatio);
    }

    // ---------------- Optimal ----------------
    static void optimal(int[] pages, int frameSize) {
        System.out.println("----- OPTIMAL -----");
        List<Integer> frame = new ArrayList<>();
        int pageFaults = 0;

        for (int i = 0; i < pages.length; i++) {
            int page = pages[i];
            if (!frame.contains(page)) {
                if (frame.size() == frameSize) {
                    // find page that will not be used for the longest time
                    int indexToRemove = -1;
                    int farthest = i+1;
                    for (int f : frame) {
                        int j;
                        for (j = i+1; j < pages.length; j++) {
                            if (pages[j] == f) break;
                        }
                        if (j == pages.length) { // not used in future
                            indexToRemove = frame.indexOf(f);
                            break;
                        } else if (j > farthest) {
                            farthest = j;
                            indexToRemove = frame.indexOf(f);
                        }
                    }
                    frame.remove(indexToRemove);
                }
                frame.add(page);
                pageFaults++;
            }
            System.out.println("Frame: " + frame);
        }

        double hitRatio = (double)(pages.length - pageFaults) / pages.length;
        System.out.println("Page Faults: " + pageFaults);
        System.out.printf("Hit Ratio: %.2f\n\n", hitRatio);
    }
}
