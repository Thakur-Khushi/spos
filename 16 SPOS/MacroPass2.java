import java.io.*;
import java.util.*;

public class MacroPass2 {

    static class Macro {
        String name;
        List<String> params;
        List<String> body;
        Macro(String name) {
            this.name = name;
            this.params = new ArrayList<>();
            this.body = new ArrayList<>();
        }
    }

    public static void main(String[] args) throws Exception {
        Map<String, Macro> macroTable = new HashMap<>();
        List<String> intermediate = readFile("Intermediate.txt");

        // Read Mnt.txt and Mdt.txt to build macro table
        readMacroTable(macroTable, "Mnt.txt", "Mdt.txt");

        System.out.println("----- Expanded Code (Pass-II) -----");

        for (String line : intermediate) {
            line = line.trim();
            if (line.isEmpty()) continue;

            // Each line: M1 10,20,&b=CREG
            String[] parts = line.split("\\s+", 2);
            String macroCall = parts[0];
            String argsPart = (parts.length > 1) ? parts[1] : "";

            if (macroTable.containsKey(macroCall)) {
                Macro macro = macroTable.get(macroCall);
                // Map actual arguments to parameters
                Map<String, String> argMap = new HashMap<>();
                String[] argsTokens = argsPart.split(",");
                for (int i = 0; i < macro.params.size() && i < argsTokens.length; i++) {
                    argMap.put(macro.params.get(i), argsTokens[i].trim());
                }
                // Expand macro body with arguments replaced
                for (String bodyLine : macro.body) {
                    String expanded = bodyLine;
                    for (Map.Entry<String, String> e : argMap.entrySet()) {
                        expanded = expanded.replace(e.getKey(), e.getValue());
                    }
                    System.out.println(expanded);
                }
            } else {
                // Not a macro call, print as-is
                System.out.println(line);
            }
        }
        System.out.println("----- End of Expanded Code -----");
    }

    static void readMacroTable(Map<String, Macro> macroTable, String mntFile, String mdtFile) throws Exception {
        List<String> mnt = readFile(mntFile);
        List<String> mdt = readFile(mdtFile);

        int mdtIndex = 0;
        for (String mntLine : mnt) {
            mntLine = mntLine.trim();
            if (mntLine.isEmpty()) continue;
            String[] tokens = mntLine.split("\\s+");
            String macroName = tokens[0];
            int pp = Integer.parseInt(tokens[1]); // positional params
            int kp = Integer.parseInt(tokens[2]); // keyword params
            // Remaining tokens can be ignored for now
            Macro macro = new Macro(macroName);

            // Read parameter names from Mdt
            // In Mdt, parameters appear as #1, #2 etc. We'll map them sequentially
            for (int i = 1; i <= pp + kp; i++) {
                macro.params.add("#" + i);
            }

            // Read macro body from Mdt until MEND
            while (mdtIndex < mdt.size()) {
                String mdtLine = mdt.get(mdtIndex++).trim();
                if (mdtLine.equalsIgnoreCase("MEND")) break;
                macro.body.add(mdtLine);
            }

            macroTable.put(macroName, macro);
        }
    }

    static List<String> readFile(String fileName) throws Exception {
        List<String> lines = new ArrayList<>();
        File f = new File(fileName);
        if (!f.exists()) {
            System.err.println("Warning: " + fileName + " not found. Continuing with empty file.");
            return lines;
        }
        BufferedReader br = new BufferedReader(new FileReader(f));
        String l;
        while ((l = br.readLine()) != null) {
            if (!l.trim().isEmpty())
                lines.add(l.trim());
        }
        br.close();
        return lines;
    }
}
