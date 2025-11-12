import java.io.*;
import java.util.*;

public class MacroProcessorPass2 {
    static class MNTEntry {
        String name;
        int pp, kp, mdtp, kpdtp;
        MNTEntry(String n, int pp, int kp, int mdtp, int kpdtp) {
            name = n;
            this.pp = pp;
            this.kp = kp;
            this.mdtp = mdtp;
            this.kpdtp = kpdtp;
        }
    }

    static List<MNTEntry> mnt = new ArrayList<>();
    static List<String> mdt = new ArrayList<>();
    static Map<String, String> aptab = new LinkedHashMap<>();

    public static void main(String[] args) throws Exception {
        readMNT("Mnt.txt");
        readMDT("Mdt.txt");
        processIntermediate("Intermediate.txt");
    }

    static void readMNT(String fname) throws Exception {
        BufferedReader br = new BufferedReader(new FileReader(fname));
        String line;
        while ((line = br.readLine()) != null) {
            line = line.trim();
            if (line.isEmpty()) continue;
            String[] parts = line.split("\\s+");
            if (parts.length < 5) continue;
            mnt.add(new MNTEntry(parts[0],
                    Integer.parseInt(parts[1]),
                    Integer.parseInt(parts[2]),
                    Integer.parseInt(parts[3]),
                    Integer.parseInt(parts[4])));
        }
        br.close();
    }

    static void readMDT(String fname) throws Exception {
        BufferedReader br = new BufferedReader(new FileReader(fname));
        String line;
        while ((line = br.readLine()) != null) {
            mdt.add(line.trim());
        }
        br.close();
    }

    static void processIntermediate(String fname) throws Exception {
        BufferedReader br = new BufferedReader(new FileReader(fname));
        String line;
        System.out.println("----- Expanded Macro Code (Pass-II Output) -----");
        while ((line = br.readLine()) != null) {
            line = line.trim();
            if (line.isEmpty()) continue;

            String[] tokens = line.split("\\s+");
            String macroName = tokens[0];

            MNTEntry macro = findMacro(macroName);
            if (macro == null) {
                System.out.println(line); // Not a macro invocation
                continue;
            }

            // Build APTAB (argument table)
            aptab.clear();
            String argPart = line.substring(line.indexOf(' ') + 1);
            String[] arguments = argPart.split(",");
            for (int i = 0; i < arguments.length; i++) {
                arguments[i] = arguments[i].trim();
                aptab.put("#" + (i + 1), arguments[i]);
            }

            // Expand the macro
            expandMacro(macro);
        }
        br.close();
    }

    static void expandMacro(MNTEntry macro) {
        int index = macro.mdtp - 1; // because array is 0-based
        for (int i = index; i < mdt.size(); i++) {
            String line = mdt.get(i);
            if (line.equalsIgnoreCase("MEND")) break;

            // Replace parameters (#1, #2, etc.) with actual arguments
            for (Map.Entry<String, String> entry : aptab.entrySet()) {
                line = line.replace(entry.getKey(), entry.getValue());
            }

            System.out.println(line);
        }
    }

    static MNTEntry findMacro(String name) {
        for (MNTEntry e : mnt) {
            if (e.name.equalsIgnoreCase(name))
                return e;
        }
        return null;
    }
}
