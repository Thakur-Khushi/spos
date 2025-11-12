import java.util.*;

public class PageReplacementFIFOOptimal {

    public static void main(String[] args) {
        int[] referenceString = {0, 2, 1, 6, 4, 0, 1, 0, 3, 1, 2, 1};
        int frames = 3;

        System.out.println("Reference String: " + Arrays.toString(referenceString));
        System.out.println("\n--- FIFO Page Replacement ---");
        fifo(referenceString, frames);

        System.out.println("\n--- Optimal Page Replacement ---");
        optimal(referenceString, frames);
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

    // Optimal Page Replacement
    static void optimal(int[] ref, int frames) {
        List<Integer> memory = new ArrayList<>();
        int pageFaults = 0;

        for (int i = 0; i < ref.length; i++) {
            int page = ref[i];
            if (!memory.contains(page)) {
                if (memory.size() < frames) {
                    memory.add(page);
                } else {
                    // find the page to replace
                    int farthest = -1;
                    int indexToReplace = -1;
                    for (int j = 0; j < memory.size(); j++) {
                        int memPage = memory.get(j);
                        int nextUse = Integer.MAX_VALUE;
                        for (int k = i + 1; k < ref.length; k++) {
                            if (ref[k] == memPage) {
                                nextUse = k;
                                break;
                            }
                        }
                        if (nextUse > farthest) {
                            farthest = nextUse;
                            indexToReplace = j;
                        }
                    }
                    memory.set(indexToReplace, page);
                }
                pageFaults++;
                System.out.println("Page " + page + " -> Page Fault | Memory: " + memory);
            } else {
                System.out.println("Page " + page + " -> Hit | Memory: " + memory);
            }
        }
        System.out.println("Total Page Faults (Optimal): " + pageFaults);
    }
}
