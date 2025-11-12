import java.util.*;

public class PageReplacementFIFOOptimal {
    public static void main(String[] args) {
        int[] pages = {7, 0, 1, 2, 0, 3, 0, 4, 2, 3, 0, 3, 2, 3};
        int frameSize = 4;

        System.out.println("Page Replacement Simulation (Frame Size = " + frameSize + ")\n");

        fifo(pages, frameSize);
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

    // ---------------- Optimal ----------------
    static void optimal(int[] pages, int frameSize) {
        System.out.println("----- OPTIMAL -----");
        List<Integer> frame = new ArrayList<>();
        int pageFaults = 0;

        for (int i = 0; i < pages.length; i++) {
            int page = pages[i];
            if (!frame.contains(page)) {
                if (frame.size() == frameSize) {
                    // Find page that will not be used for the longest time
                    int indexToRemove = -1;
                    int farthest = i;
                    for (int f : frame) {
                        int j;
                        for (j = i + 1; j < pages.length; j++) {
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
