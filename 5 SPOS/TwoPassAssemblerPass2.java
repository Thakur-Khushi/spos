import java.io.*;
import java.util.*;
import java.util.regex.*;

/**
 * TwoPassAssemblerPass2.java
 * Pass-II implementation for a pseudo-machine assembler.
 *
 * Reads:
 *  - Intermediate.txt
 *  - Littab.txt
 *  - Symtab.txt
 *
 * Produces machine code resolving (IS),(DL),(AD) tokens and S/L/C references.
 *
 * Usage:
 *  javac TwoPassAssemblerPass2.java
 *  java TwoPassAssemblerPass2
 */
public class TwoPassAssemblerPass2 {

    static List<Integer> litAddresses = new ArrayList<>();   // index -> address
    static List<Integer> symAddresses = new ArrayList<>();   // index -> address (ordered by file)
    static List<String> intermediate = new ArrayList<>();

    public static void main(String[] args) throws Exception {
        readLiteralTable("Littab.txt");
        readSymbolTable("Symtab.txt");
        readIntermediate("Intermediate.txt");

        int LC = 0;
        boolean startSeen = false;

        System.out.println("----- Generated Machine Code -----");
        System.out.println("LC\tOPCODE\tREG\tOPERAND");

        for (String line : intermediate) {
            // extract tokens like (AD,01), (IS,04), (C,200), (S,2), (L,1), (3), etc.
            List<String> tokens = extractParenTokens(line);

            if (tokens.isEmpty()) continue;

            String first = tokens.get(0); // e.g. (AD,01) or (IS,04) or (DL,01)

            // Helper to parse pair token like (X,YY)
            String t0 = first.replaceAll("[()\\s]", "");
            String[] t0parts = t0.split(",", 2);
            String type = t0parts[0]; // AD / IS / DL / etc
            String code = t0parts.length > 1 ? t0parts[1] : "";

            // Handle assembler directives
            if (type.equalsIgnoreCase("AD")) {
                // START directive (AD,01)(C,200)
                if (code.equals("01")) {
                    // find next token (C,<num>) and set LC
                    for (String tk : tokens) {
                        if (tk.matches("\\(C,\\s*-?\\d+\\)")) {
                            int val = Integer.parseInt(tk.replaceAll("[^0-9\\-]", ""));
                            LC = val;
                            startSeen = true;
                            break;
                        }
                    }
                    System.out.printf("%04d\t%s\t%s\t%s\n", LC, "AD-START", "-", "-");
                    continue;
                }
                // ORIGIN-like directive (AD,03)(S,2)+2 or (AD,03)(S,6)+1 etc
                if (code.equals("03")) {
                    // tokens may contain something like "(S,2)+2" or "(S,2)" and separate +2
                    // We'll try to reconstruct expression by searching for pattern "(S,n)(+|-)?\d*"
                    String exprPart = line.substring(line.indexOf("(AD"));
                    // find first occurrence of (S,x) or (C,x) or plain (number)
                    Matcher m = Pattern.compile("\\((S|L|C),\\s*(\\d+)\\)([\\+\\-]\\d+)?").matcher(line);
                    if (m.find()) {
                        String refType = m.group(1);
                        int idx = Integer.parseInt(m.group(2));
                        int offset = 0;
                        String offStr = m.group(3);
                        if (offStr != null) offset = Integer.parseInt(offStr);
                        int baseAddr = 0;
                        if (refType.equals("S")) {
                            if (idx-1 >= 0 && idx-1 < symAddresses.size()) baseAddr = symAddresses.get(idx-1);
                            else baseAddr = 0;
                        } else if (refType.equals("L")) {
                            if (idx-1 >= 0 && idx-1 < litAddresses.size()) baseAddr = litAddresses.get(idx-1);
                            else baseAddr = 0;
                        } else if (refType.equals("C")) {
                            baseAddr = idx;
                        }
                        LC = baseAddr + offset;
                        System.out.printf("%04d\t%s\t%s\t%s\n", LC, "AD-ORIGIN", "-", "-" );
                    } else {
                        // fallback: no recognizable reference - do nothing
                        System.err.println("Warning: AD,03 expression not parsed in line: " + line);
                    }
                    continue;
                }
                // Other AD: END etc - we can print and continue
                System.out.printf("%04d\t%s\t%s\t%s\n", LC, "AD", "-", "-");
                continue;
            }

            // Handle DL (declaration) e.g., (DL,01)(C,5)  => DC constant, allocate memory
            if (type.equalsIgnoreCase("DL")) {
                // DL,01 usually DC (constant). DL,02 maybe DS (?) but we will treat both as data words.
                int value = 0;
                // find first (C,num)
                for (String tk : tokens) {
                    if (tk.matches("\\(C,\\s*-?\\d+\\)")) {
                        value = Integer.parseInt(tk.replaceAll("[^0-9\\-]", ""));
                        break;
                    }
                }
                System.out.printf("%04d\t%s\t%s\t%d\n", LC, "DL", "-", value);
                LC += 1;
                continue;
            }

            // Handle IS (Imperative Statement) - produce machine word
            if (type.equalsIgnoreCase("IS")) {
                String opcode = code; // opcode number as string
                String regField = "-";
                String operandField = "-";

                // tokens after first contain operands like (1) or (S,1) or (L,2) or (C,200) etc.
                for (int k = 1; k < tokens.size(); k++) {
                    String tk = tokens.get(k).trim();

                    // plain number inside parentheses e.g. (1) => treat as register/ small immediate (prefer register)
                    if (tk.matches("\\(\\s*\\d+\\s*\\)")) {
                        String num = tk.replaceAll("[()\\s]", "");
                        // assign numeric to register if reg is empty, else to operand
                        if (regField.equals("-")) regField = num;
                        else operandField = num;
                        continue;
                    }

                    // Register-like token sometimes appears as (1) - handled above.
                    // Constant token (C,num)
                    if (tk.matches("\\(C,\\s*-?\\d+\\)")) {
                        int val = Integer.parseInt(tk.replaceAll("[^0-9\\-]", ""));
                        operandField = String.valueOf(val);
                        continue;
                    }

                    // Symbol token (S,n)
                    if (tk.matches("\\(S,\\s*\\d+\\)")) {
                        int idx = Integer.parseInt(tk.replaceAll("[^0-9]", ""));
                        int addr = resolveSym(idx);
                        operandField = String.valueOf(addr);
                        continue;
                    }

                    // Literal token (L,n)
                    if (tk.matches("\\(L,\\s*\\d+\\)")) {
                        int idx = Integer.parseInt(tk.replaceAll("[^0-9]", ""));
                        int addr = resolveLit(idx);
                        operandField = String.valueOf(addr);
                        continue;
                    }

                    // Expression like (S,2)+2 or (S,2)-1
                    Matcher expr = Pattern.compile("\\(S,\\s*(\\d+)\\)\\s*([\\+\\-])\\s*(\\d+)").matcher(tk);
                    if (expr.find()) {
                        int idx = Integer.parseInt(expr.group(1));
                        String sign = expr.group(2);
                        int off = Integer.parseInt(expr.group(3));
                        int base = resolveSym(idx);
                        operandField = String.valueOf(sign.equals("+") ? base + off : base - off);
                        continue;
                    }

                    // Expression like (L,2)+1
                    Matcher exprL = Pattern.compile("\\(L,\\s*(\\d+)\\)\\s*([\\+\\-])\\s*(\\d+)").matcher(tk);
                    if (exprL.find()) {
                        int idx = Integer.parseInt(exprL.group(1));
                        String sign = exprL.group(2);
                        int off = Integer.parseInt(exprL.group(3));
                        int base = resolveLit(idx);
                        operandField = String.valueOf(sign.equals("+") ? base + off : base - off);
                        continue;
                    }

                    // Sometimes tokens come glued e.g. "(S,2)+2" as single token; try to match overall
                    Matcher glued = Pattern.compile("\\(S,\\s*(\\d+)\\)\\s*([\\+\\-])\\s*(\\d+)").matcher(line);
                    if (glued.find()) {
                        int idx = Integer.parseInt(glued.group(1));
                        String sign = glued.group(2);
                        int off = Integer.parseInt(glued.group(3));
                        int base = resolveSym(idx);
                        operandField = String.valueOf(sign.equals("+") ? base + off : base - off);
                        continue;
                    }
                }

                // Print the machine code word
                System.out.printf("%04d\t%s\t%s\t%s\n", LC, opcode, regField, operandField);
                LC += 1;
                continue;
            }

            // Unknown token - print warning
            System.err.println("Warning: Unhandled line in intermediate: " + line);
        }

        System.out.println("----- End of Machine Code -----");
    }

    // read Littab.txt where each line is "<something> <address>"
    static void readLiteralTable(String fname) throws Exception {
        File f = new File(fname);
        if (!f.exists()) {
            System.err.println("Warning: " + fname + " not found. Literal table empty.");
            return;
        }
        BufferedReader br = new BufferedReader(new FileReader(f));
        String l;
        while ((l = br.readLine()) != null) {
            l = l.trim();
            if (l.isEmpty()) continue;
            String[] parts = l.split("\\s+");
            // assume last token is address
            String last = parts[parts.length - 1];
            try {
                int addr = Integer.parseInt(last);
                litAddresses.add(addr);
            } catch (NumberFormatException e) {
                // skip line if not parseable
            }
        }
        br.close();
    }

    // read Symtab.txt where each line is "<symbol> ... <address>"
    static void readSymbolTable(String fname) throws Exception {
        File f = new File(fname);
        if (!f.exists()) {
            System.err.println("Warning: " + fname + " not found. Symbol table empty.");
            return;
        }
        BufferedReader br = new BufferedReader(new FileReader(f));
        String l;
        while ((l = br.readLine()) != null) {
            l = l.trim();
            if (l.isEmpty()) continue;
            String[] parts = l.split("\\s+");
            // assume last token is address
            String last = parts[parts.length - 1];
            try {
                int addr = Integer.parseInt(last);
                symAddresses.add(addr);
            } catch (NumberFormatException e) {
                // skip
            }
        }
        br.close();
    }

    static void readIntermediate(String fname) throws Exception {
        File f = new File(fname);
        if (!f.exists()) {
            System.err.println("Error: " + fname + " not found. Exiting.");
            System.exit(1);
        }
        BufferedReader br = new BufferedReader(new FileReader(f));
        String l;
        while ((l = br.readLine()) != null) {
            l = l.trim();
            if (l.isEmpty()) continue;
            intermediate.add(l);
        }
        br.close();
    }

    static List<String> extractParenTokens(String s) {
        List<String> out = new ArrayList<>();
        Matcher m = Pattern.compile("\\([^()]*\\)(?:[\\+\\-]\\d+)?").matcher(s);
        while (m.find()) {
            out.add(m.group());
        }
        return out;
    }

    static int resolveSym(int idx) {
        if (idx - 1 >= 0 && idx - 1 < symAddresses.size()) return symAddresses.get(idx - 1);
        System.err.println("Warning: Symbol index " + idx + " out of range. Using 0.");
        return 0;
    }

    static int resolveLit(int idx) {
        if (idx - 1 >= 0 && idx - 1 < litAddresses.size()) return litAddresses.get(idx - 1);
        System.err.println("Warning: Literal index " + idx + " out of range. Using 0.");
        return 0;
    }
}
