import java.io.*;
import java.util.*;
import java.util.regex.*;

/**
 * TwoPassAssemblerPass1.java
 * Pass-I implementation for a simple pseudo-machine assembler.
 *
 * Usage:
 *  - Compile: javac TwoPassAssemblerPass1.java
 *  - Run:     java TwoPassAssemblerPass1
 *
 * Input: type or pipe the assembly program lines; when a line contains "END" (alone or as token),
 *        pass-I completes and prints Symbol Table, Literal Table, Pool Table and Intermediate Code.
 */
public class TwoPassAssemblerPass1 {
    static class Symbol {
        String name;
        Integer address; // null if not assigned yet
        String value;    // For DC or EQU value (as string)
        boolean isDefined;

        Symbol(String name) {
            this.name = name;
            this.address = null;
            this.value = null;
            this.isDefined = false;
        }
    }

    static class Literal {
        String literal;   // e.g. ="2" or ='5'
        Integer address;  // assigned at LTORG/END
        Literal(String lit) { this.literal = lit; this.address = null; }
    }

    static Set<String> directives = new HashSet<>(Arrays.asList(
            "START","END","LTORG","ORIGIN","EQU","DS","DC"
    ));

    static Set<String> opcodes = new HashSet<>(Arrays.asList(
            // sample opcodes for pseudo-machine (we just treat them as instructions)
            "MOVER","MOVEM","ADD","SUB","MULT","DIV","PRINT","READ","COMP","BC","JMP","STOP"
    ));

    static Map<String, Symbol> symtab = new LinkedHashMap<>();
    static List<Literal> littab = new ArrayList<>();
    static List<Integer> pooltab = new ArrayList<>(); // indices into littab (0-based)
    static List<String> intermediate = new ArrayList<>();

    static int LC = 0;

    public static void main(String[] args) throws Exception {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        System.out.println("Enter assembly program lines (type/paste). End when a line contains END.");
        List<String> lines = new ArrayList<>();
        String line;
        while ((line = br.readLine()) != null) {
            line = line.trim();
            if (line.length() == 0) continue;
            lines.add(line);
            if (line.toUpperCase().contains("END")) break;
        }

        // initialize pool table with first pool starting at 0 (no literals yet)
        pooltab.add(0);

        // Preprocess each line and execute Pass-I actions
        for (int ln = 0; ln < lines.size(); ln++) {
            String raw = lines.get(ln).trim();
            // Normalize: insert spaces in token like START100 -> START 100
            raw = normalizeCompactTokens(raw);

            // Tokenize: split by whitespace, but keep commas attached as separators handled later
            String[] tokens = raw.split("\\s+");
            // further break tokens by commas, but preserve string/char-literals
            List<String> toks = splitRespectingQuotes(tokens);

            // if no tokens, skip
            if (toks.isEmpty()) continue;

            // detect presence of label: if first token is not directive or opcode -> label
            String first = toks.get(0);
            String label = null;
            int idx = 0;

            String t0uc = first.toUpperCase();
            if (!directives.contains(t0uc) && !opcodes.contains(t0uc)) {
                // first token is label
                label = first;
                idx = 1;
            }

            // next token should exist
            if (idx >= toks.size()) continue;
            String op = toks.get(idx).toUpperCase();
            idx++;

            // If label exists, define its address (for DS/DC or just current LC)
            if (label != null) {
                Symbol s = symtab.getOrDefault(label, new Symbol(label));
                if (!s.isDefined) {
                    s.address = LC;
                    s.isDefined = true;
                }
                symtab.put(label, s);
            }

            // Handle START
            if (op.equals("START")) {
                // operand may be immediate in same token or next token
                String operand = (idx < toks.size()) ? toks.get(idx) : null;
                if (operand != null) {
                    // remove commas
                    operand = operand.replaceAll(",", "");
                    try {
                        LC = Integer.parseInt(operand);
                    } catch (NumberFormatException e) {
                        System.err.println("Invalid START operand: " + operand + " . Default LC=0.");
                        LC = 0;
                    }
                } else {
                    LC = 0;
                }
                intermediate.add(formatIC(LC, "AD", "START", operand));
                continue;
            }

            // Handle END - assign remaining literals and finish
            if (op.equals("END")) {
                intermediate.add(formatIC(LC, "AD", "END", null));
                assignLiteralsAtPool();
                break;
            }

            // Handle LTORG - assign literals currently in pool
            if (op.equals("LTORG")) {
                intermediate.add(formatIC(LC, "AD", "LTORG", null));
                assignLiteralsAtPool();
                continue;
            }

            // Handle ORIGIN
            if (op.equals("ORIGIN")) {
                // operand expression like L2+1 or a number
                String expr = (idx < toks.size()) ? toks.get(idx) : "";
                expr = expr.replaceAll(",", "");
                int newLC = evaluateExpression(expr);
                intermediate.add(formatIC(LC, "AD", "ORIGIN", expr));
                LC = newLC;
                continue;
            }

            // Handle EQU
            if (op.equals("EQU")) {
                // label must have been present (left side)
                if (label == null) {
                    System.err.println("EQU without label at line: " + raw);
                    continue;
                }
                String expr = (idx < toks.size()) ? toks.get(idx) : "";
                expr = expr.replaceAll(",", "");
                int val = evaluateExpression(expr);
                Symbol sym = symtab.get(label);
                if (sym == null) sym = new Symbol(label);
                sym.address = val;
                sym.value = String.valueOf(val);
                sym.isDefined = true;
                symtab.put(label, sym);
                intermediate.add(formatIC(LC, "AD", "EQU", label + "=" + expr));
                continue;
            }

            // Handle DS and DC (declarations)
            if (op.equals("DS")) {
                String sizeTok = (idx < toks.size()) ? toks.get(idx) : "1";
                sizeTok = sizeTok.replaceAll(",", "");
                int size = parseIntOrDefault(sizeTok, 1);
                // label must have been present to place DS
                if (label == null) {
                    System.err.println("DS without label at line: " + raw);
                    continue;
                }
                Symbol s = symtab.getOrDefault(label, new Symbol(label));
                s.address = LC;
                s.isDefined = true;
                symtab.put(label, s);
                intermediate.add(formatIC(LC, "DL", "DS", label + " DS " + size));
                LC += size;
                continue;
            }

            if (op.equals("DC")) {
                // label should be present
                if (label == null) {
                    System.err.println("DC without label at line: " + raw);
                    continue;
                }
                String valTok = (idx < toks.size()) ? toks.get(idx) : "0";
                valTok = valTok.replaceAll(",", "");
                // remove surrounding quotes if any
                String cleaned = stripQuotes(valTok);
                Symbol s = symtab.getOrDefault(label, new Symbol(label));
                s.address = LC;
                s.isDefined = true;
                s.value = cleaned;
                symtab.put(label, s);
                intermediate.add(formatIC(LC, "DL", "DC", label + " DC " + cleaned));
                LC += 1;
                continue;
            }

            // Otherwise treat as a machine instruction (opcode)
            if (opcodes.contains(op)) {
                // prepare operand(s)
                StringBuilder opsb = new StringBuilder();
                while (idx < toks.size()) {
                    String opd = toks.get(idx++);
                    if (opd.equals(",")) continue;
                    opsb.append(opd);
                    if (idx < toks.size()) opsb.append(" ");
                }
                String operands = opsb.toString().trim();
                // scan for literals in operands (start with =)
                List<String> opdTokens = splitOperandsRespectingLiterals(operands);
                for (String opd : opdTokens) {
                    opd = opd.trim();
                    if (opd.startsWith("=")) {
                        // literal
                        String lit = opd;
                        // normalize literal, e.g. ="2" or ='3'
                        littabAddIfAbsent(lit);
                    } else {
                        // might be symbol - add to symbol table if not present
                        // remove commas
                        String symname = opd.replaceAll(",", "").trim();
                        // skip registers like AREG or constants (numeric)
                        if (symname.length() == 0) continue;
                        if (isRegister(symname) || isNumeric(symname) || symname.startsWith("'") || symname.startsWith("\""))
                            continue;
                        // strip any trailing punctuation/comma
                        symname = symname.replaceAll("[,]", "");
                        if (!symtab.containsKey(symname)) symtab.put(symname, new Symbol(symname));
                    }
                }
                intermediate.add(formatIC(LC, "IS", op, operands));
                LC += 1; // assume each instruction occupies 1 memory word
                continue;
            }

            // fallback - unknown token: just store in intermediate
            intermediate.add(formatIC(LC, "??", op, String.join(" ", toks.subList(idx, toks.size()))));
        }

        // After processing, print results
        printIntermediate();
        printSymbolTable();
        printLiteralTable();
        printPoolTable();
        System.out.println("\nPass-I completed.");
    }

    // Helper: add literal if not present already
    static void littabAddIfAbsent(String lit) {
        // normalize e.g. ='2' or ="2" -> keep as-is
        for (Literal L : littab) {
            if (L.literal.equals(lit)) return;
        }
        littab.add(new Literal(lit));
    }

    // Assign literals in the current pool: from poolStartIndex to end of littab (unassigned)
    static void assignLiteralsAtPool() {
        int poolStart = pooltab.get(pooltab.size() - 1);
        // find first unassigned literal index >= poolStart
        int i = poolStart;
        boolean anyAssigned = false;
        for ( ; i < littab.size(); i++) {
            Literal L = littab.get(i);
            if (L.address == null) {
                L.address = LC;
                intermediate.add(formatIC(LC, "LT", "LITERAL", L.literal));
                LC += 1;
                anyAssigned = true;
            }
        }
        if (anyAssigned) {
            // next pool starts at current littab size
            pooltab.add(littab.size());
        }
    }

    static String formatIC(int lc, String type, String op, String operands) {
        return String.format("%04d\t(%s)\t%s\t%s", lc, type, op, (operands == null ? "" : operands));
    }

    static void printIntermediate() {
        System.out.println("\n----- Intermediate Code (Pass-I) -----");
        for (String s : intermediate) {
            System.out.println(s);
        }
    }

    static void printSymbolTable() {
        System.out.println("\n----- Symbol Table -----");
        System.out.format("%-10s %-10s %-10s\n", "Symbol", "Address", "Value");
        for (Map.Entry<String, Symbol> e : symtab.entrySet()) {
            Symbol s = e.getValue();
            System.out.format("%-10s %-10s %-10s\n",
                    s.name,
                    (s.address == null ? "-" : s.address.toString()),
                    (s.value == null ? "-" : s.value));
        }
    }

    static void printLiteralTable() {
        System.out.println("\n----- Literal Table -----");
        System.out.format("%-6s %-15s %-8s\n", "Idx", "Literal", "Address");
        for (int i = 0; i < littab.size(); i++) {
            Literal L = littab.get(i);
            System.out.format("%-6d %-15s %-8s\n", i, L.literal, (L.address == null ? "-" : L.address.toString()));
        }
    }

    static void printPoolTable() {
        System.out.println("\n----- Pool Table -----");
        System.out.format("%-6s %-10s\n", "Idx", "LittabStartIdx");
        for (int i = 0; i < pooltab.size(); i++) {
            System.out.format("%-6d %-10d\n", i, pooltab.get(i));
        }
    }

    // Evaluate expression like "L2+1" or "100" or "A+1"
    // If symbol not defined, assume 0 (and warn)
    static int evaluateExpression(String expr) {
        expr = expr.trim();
        if (expr.length() == 0) return LC;

        // support + and - only, single operation
        String op = null;
        if (expr.contains("+")) op = "+";
        else if (expr.contains("-")) op = "-";

        if (op == null) {
            // single token: either number or symbol
            if (isNumeric(expr)) return Integer.parseInt(expr);
            Symbol s = symtab.get(expr);
            if (s != null && s.address != null) return s.address;
            // maybe symbol defined with value (EQU or DC)
            if (s != null && s.value != null && isNumeric(s.value)) return Integer.parseInt(s.value);
            System.err.println("Warning: evaluating expression '" + expr + "' - symbol not defined. Using 0.");
            return 0;
        } else {
            String[] parts = expr.split(Pattern.quote(op), 2);
            String left = parts[0].trim();
            String right = parts[1].trim();
            int lv = 0, rv = 0;
            if (isNumeric(left)) lv = Integer.parseInt(left);
            else {
                Symbol s = symtab.get(left);
                if (s != null && s.address != null) lv = s.address;
                else if (s != null && s.value != null && isNumeric(s.value)) lv = Integer.parseInt(s.value);
                else {
                    System.err.println("Warning: symbol '" + left + "' not defined while evaluating '" + expr + "'. Using 0.");
                    lv = 0;
                }
            }
            if (isNumeric(right)) rv = Integer.parseInt(right);
            else {
                Symbol s = symtab.get(right);
                if (s != null && s.address != null) rv = s.address;
                else if (s != null && s.value != null && isNumeric(s.value)) rv = Integer.parseInt(s.value);
                else {
                    System.err.println("Warning: symbol '" + right + "' not defined while evaluating '" + expr + "'. Using 0.");
                    rv = 0;
                }
            }
            return op.equals("+") ? (lv + rv) : (lv - rv);
        }
    }

    static boolean isNumeric(String s) {
        if (s == null) return false;
        return s.matches("-?\\d+");
    }

    static boolean isRegister(String s) {
        // basic register detection e.g., AREG, BREG
        return s.toUpperCase().endsWith("REG") || s.equalsIgnoreCase("AREG") || s.equalsIgnoreCase("BREG");
    }

    static int parseIntOrDefault(String tok, int def) {
        try {
            return Integer.parseInt(tok);
        } catch (Exception e) {
            return def;
        }
    }

    // Remove surrounding single or double quotes
    static String stripQuotes(String s) {
        if (s == null) return null;
        s = s.trim();
        if ((s.startsWith("'") && s.endsWith("'")) || (s.startsWith("\"") && s.endsWith("\""))) {
            return s.substring(1, s.length() - 1);
        }
        return s;
    }

    // Split tokens but keep quoted strings together (we already had token array)
    static List<String> splitRespectingQuotes(String[] tokens) {
        List<String> out = new ArrayList<>();
        boolean inQuote = false;
        StringBuilder cur = new StringBuilder();
        for (String tk : tokens) {
            if (!inQuote) {
                if (tk.startsWith("'") || tk.startsWith("\"")) {
                    inQuote = true;
                    cur.setLength(0);
                    cur.append(tk);
                    if ((tk.endsWith("'") || tk.endsWith("\"")) && tk.length() > 1 && !tk.endsWith("\\'") && !tk.endsWith("\\\"")) {
                        // complete quoted token
                        inQuote = false;
                        out.add(cur.toString());
                    }
                } else {
                    // simple token - but also split commas into separate tokens for easier processing
                    if (tk.contains(",")) {
                        String[] parts = tk.split("(?<=.),|,");
                        for (String p : parts) {
                            if (p.length() > 0) out.add(p);
                        }
                    } else {
                        out.add(tk);
                    }
                }
            } else {
                // we're inside a quote
                cur.append(" ").append(tk);
                if ((tk.endsWith("'") || tk.endsWith("\"")) && !tk.endsWith("\\'") && !tk.endsWith("\\\"")) {
                    inQuote = false;
                    out.add(cur.toString());
                }
            }
        }
        return out;
    }

    // Split operands string into list, respecting commas and literals
    static List<String> splitOperandsRespectingLiterals(String operands) {
        List<String> out = new ArrayList<>();
        if (operands == null || operands.trim().isEmpty()) return out;
        // split by comma but ignore commas inside quotes
        StringBuilder cur = new StringBuilder();
        boolean inQuote = false;
        for (int i = 0; i < operands.length(); i++) {
            char c = operands.charAt(i);
            if (c == '\'' || c == '\"') {
                inQuote = !inQuote;
                cur.append(c);
            } else if (c == ',' && !inQuote) {
                out.add(cur.toString().trim());
                cur.setLength(0);
            } else {
                cur.append(c);
            }
        }
        if (cur.length() > 0) out.add(cur.toString().trim());
        return out;
    }

    // For tokens like START100 produce "START 100"
    static String normalizeCompactTokens(String line) {
        // Insert space between letters and numbers (e.g., START100 -> START 100),
        // but avoid touching quoted literals and tokens like A+1.
        // We'll do a simple approach: for leading directive/keyword followed by number
        String trimmed = line.trim();

        // pattern: ^(START|ORIGIN)(\d+)$ or ^(START|ORIGIN)(\d+.*)$
        Pattern p = Pattern.compile("^(START|END|LTORG|ORIGIN|EQU|DS|DC)(\\d+.*)$", Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(trimmed);
        if (m.find()) {
            return m.group(1) + " " + m.group(2);
        }
        // also handle case like "START100" with lowercase/upper mixed
        p = Pattern.compile("^([A-Za-z]+)(\\d+.*)$");
        m = p.matcher(trimmed);
        if (m.find()) {
            String kw = m.group(1).toUpperCase();
            if (directives.contains(kw) || opcodes.contains(kw)) {
                return kw + " " + m.group(2);
            }
        }
        return trimmed;
    }
}
