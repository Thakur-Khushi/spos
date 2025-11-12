import java.io.*;
import java.util.*;

/**
 * MacroPass1.java
 * Implementation of Pass-I of Two-Pass Macroprocessor
 * Input is taken from console (can also redirect from file)
 */
public class MacroPass1 {
    static class MNTEntry {
        String name;
        int mdtIndex;
        int alaIndex;

        MNTEntry(String name, int mdtIndex, int alaIndex) {
            this.name = name;
            this.mdtIndex = mdtIndex;
            this.alaIndex = alaIndex;
        }
    }

    static class ALAEntry {
        Map<String, String> argMap = new LinkedHashMap<>();
    }

    static List<MNTEntry> MNT = new ArrayList<>();
    static List<String> MDT = new ArrayList<>();
    static List<ALAEntry> ALAList = new ArrayList<>();

    public static void main(String[] args) throws Exception {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        System.out.println("Enter macro program lines (type/paste). End with END.");
        List<String> lines = new ArrayList<>();
        String line;
        while ((line = br.readLine()) != null) {
            line = line.trim();
            if (line.isEmpty()) continue;
            lines.add(line);
            if (line.equalsIgnoreCase("END")) break;
        }

        int i = 0;
        while (i < lines.size()) {
            String l = lines.get(i).trim();
            if (l.equalsIgnoreCase("MACRO")) {
                // Start of macro definition
                String header = lines.get(++i).trim();
                String[] headerParts = header.split("\\s+");
                String macroName = headerParts[0];
                ALAEntry ala = new ALAEntry();

                // Fill ALA for this macro
                for (int j = 1; j < headerParts.length; j++) {
                    String arg = headerParts[j];
                    if (arg.startsWith("&"))
                        ala.argMap.put(arg, "#" + j); // positionally replace with #1, #2 ...
                }

                int alaIndex = ALAList.size();
                ALAList.add(ala);

                int mdtIndex = MDT.size();
                MNT.add(new MNTEntry(macroName, mdtIndex, alaIndex));

                // Add macro definition to MDT
                while (!lines.get(++i).equalsIgnoreCase("MEND")) {
                    String body = lines.get(i);
                    // Replace formal args with positional notation
                    for (Map.Entry<String, String> e : ala.argMap.entrySet()) {
                        body = body.replace(e.getKey(), e.getValue());
                    }
                    MDT.add(body);
                }
                MDT.add("MEND");
            }
            i++;
        }

        printTables();
    }

    static void printTables() {
        System.out.println("\n----- MACRO NAME TABLE (MNT) -----");
        System.out.printf("%-10s %-10s %-10s\n", "Name", "MDT_Index", "ALA_Index");
        for (MNTEntry e : MNT) {
            System.out.printf("%-10s %-10d %-10d\n", e.name, e.mdtIndex, e.alaIndex);
        }

        System.out.println("\n----- MACRO DEFINITION TABLE (MDT) -----");
        for (int i = 0; i < MDT.size(); i++) {
            System.out.printf("%03d  %s\n", i, MDT.get(i));
        }

        System.out.println("\n----- ARGUMENT LIST ARRAY (ALA) -----");
        for (int i = 0; i < ALAList.size(); i++) {
            System.out.println("ALA #" + i);
            for (Map.Entry<String, String> e : ALAList.get(i).argMap.entrySet()) {
                System.out.printf("   %-10s -> %s\n", e.getKey(), e.getValue());
            }
        }

        System.out.println("\nPass-I of Macro Processor completed.");
    }
}
