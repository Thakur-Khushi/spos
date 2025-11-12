import java.util.*;

public class PageReplacementOptimal {

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

        System.out.println("\n--- Optimal Page Replacement ---");
        optimal(pages, frames);
    }

    // FIFO algorithm
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

    // Optimal algorithm
    static void optimal(int[] pages, int frames) {
        List<Integer> frameList = new ArrayList<>();
        int pageFaults = 0;

        for (int i = 0; i < pages.length; i++) {
            int page = pages[i];

            if (!frameList.contains(page)) {
                if (frameList.size() < frames) {
                    frameList.add(page);
                } else {
                    int indexToReplace = findOptimalIndex(pages, frameList, i + 1);
                    frameList.set(indexToReplace, page);
                }
                pageFaults++;
                System.out.println("Page " + page + " -> Fault | Frames: " + frameList);
            } else {
                System.out.println("Page " + page + " -> Hit   | Frames: " + frameList);
            }
        }

        System.out.println("Total Page Faults (Optimal): " + pageFaults);
    }

    // Finds which page to replace in Optimal algorithm
    static int findOptimalIndex(int[] pages, List<Integer> frameList, int start) {
        int farthest = start, indexToReplace = -1;

        for (int i = 0; i < frameList.size(); i++) {
            int page = frameList.get(i);
            int j;
            for (j = start; j < pages.length; j++) {
                if (pages[j] == page)
                    break;
            }
            if (j == pages.length) // page not used again
                return i;
            if (j > farthest) {
                farthest = j;
                indexToReplace = i;
            }
        }
        return (indexToReplace == -1) ? 0 : indexToReplace;
    }
}
