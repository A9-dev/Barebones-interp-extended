import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class App {
    static Map<String, Integer> variables = new HashMap<String, Integer>();

    public static void main(String[] args) throws Exception {
        long startTime = System.nanoTime();
        // Main function: extracts code from txt, splits into lines, parses lines,
        // executes parsed lines
        String content = extracted();
        String[] lines = content.split(";");
        String[][] code = parse(lines);
        execute(code);
        long endTime = System.nanoTime();
        long duration = (endTime - startTime);
        System.out.println(String.format("Duration: %sms", duration / 1000000));
    }

    private static String extracted() throws IOException {
        // Reads lines from txt and removes \n from every line
        String content = new String(Files.readAllBytes(Paths.get("barebonesCode.txt")));
        content = content.replace("\r", "").replace("\t", "").replace("\n", "").replaceAll(";\s*", ";");
        System.out.println(content);
        return content;
    }

    public static String[][] parse(String[] lines) {
        String[][] linesSplit = new String[lines.length][1];
        for (int i = 0; i < lines.length; i++) {
            linesSplit[i] = lines[i].split(" ");
        }
        return linesSplit;
    }

    private static void execute(String[][] code) throws IOException {
        for (int i = 0; i < code.length; i++) {
            String opcode = code[i][0];
            String operand = "";

            if (!opcode.equals("end")) {
                operand = code[i][1];
            }

            switch (opcode) {
            case "clear":
                variables.put(operand, 0);
                break;

            case "incr":
                try {
                    variables.replace(operand, variables.get(operand) + 1);
                } catch (Exception e) {
                    System.err.println(String.format(
                            "Error occurred on line %s, most likely due to incrementing before variable declaration",
                            i + 1));
                }
                break;

            case "decr":
                try {
                    variables.replace(operand, variables.get(operand) - 1);
                } catch (Exception e) {
                    System.err.println(String.format(
                            "Error occurred on line %s, most likely due to decrementing before variable declaration",
                            i + 1));
                }
                break;

            case "while":
                try {

                    Integer endLine = 0;
                    while (variables.get(operand) != 0) {
                        Integer depth = 0;
                        for (int j = i + 1; j < code.length; j++) {
                            if (code[j][0].equals("while")) {
                                depth += 1;
                            } else if (code[j][0].equals("end")) {
                                if (depth == 0) {
                                    endLine = j;

                                } else {
                                    depth -= 1;
                                }
                            }
                        }
                        String[][] newArray = Arrays.copyOfRange(code, i + 1, endLine);
                        execute(newArray);

                    }
                    i = endLine;

                } catch (Exception e) {
                    System.err.println(e);
                }
                break;

            case "end":
                try {

                } catch (Exception e) {
                    System.err.println(e);
                }
            case "nasa":
                // Creates given variable name with maximum estimated size in metres of asteroid
                // of which will be at their closest approach distance at the date of line
                // execution. Get key here: https://api.nasa.gov/
                String apiKey = "";

                SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
                Date date = new Date();

                URL yahoo = new URL(String.format("https://api.nasa.gov/neo/rest/v1/feed?start_date=%s&api_key=%s",
                        formatter.format(date), apiKey));
                URLConnection yc = yahoo.openConnection();
                BufferedReader in = new BufferedReader(new InputStreamReader(yc.getInputStream()));
                String outputLine = "";
                String inputLine;

                while ((inputLine = in.readLine()) != null)
                    outputLine += inputLine;
                in.close();
                Pattern p = Pattern.compile(
                        "\"meters\":\\{\"estimated_diameter_min\":[+-]?([0-9]*[.])?[0-9]+,\"estimated_diameter_max\":[+-]?([0-9]*[.])?[0-9]+");
                Matcher m = p.matcher(outputLine);
                if (m.find()) {
                    variables.put(operand, Integer.parseInt(m.group(2).substring(0, m.group(2).length() - 1)));
                }

            default:
                break;
            }
            System.out.println(variables.toString());
        }
    }
}
