package ua.lpnu.kzp;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** Консольна програма для обробки реєстру студентів (варіант 6). */
public final class Main {

    private Main() {
    }

    /**
     * Точка входу до програми.
     *
     * @param args аргументи командного рядка
     */
    public static void main(String[] args) {
        String inputPath = "data/input.csv";
        String outputPath = "out/report.txt";

        for (int i = 0; i < args.length; i++) {
            if ("--help".equals(args[i])) {
                System.out.printf(Locale.ROOT,
                        "Використання: java -jar lab01.jar [--help] [--input <файл>] [--output <файл>]%n");
                return;
            }
            if ("--input".equals(args[i]) && i + 1 < args.length) {
                inputPath = args[++i];
            }
            if ("--output".equals(args[i]) && i + 1 < args.length) {
                outputPath = args[++i];
            }
        }

        try {
            List<String> lines = Files.readAllLines(Path.of(inputPath), StandardCharsets.UTF_8);
            String report = buildReport(lines);
            System.out.print(report);

            Path output = Path.of(outputPath);
            Path outputParent = output.getParent();
            if (outputParent != null) {
                Files.createDirectories(outputParent);
            }
            Files.writeString(output, report, StandardCharsets.UTF_8);
        } catch (IOException exception) {
            System.out.printf(Locale.ROOT, "Помилка читання/запису файлу: %s%n", exception.getMessage());
        }
    }

    /**
     * Обробляє список рядків реєстру та формує текст звіту.
     *
     * @param lines рядки вхідного файлу
     * @return готовий текст звіту
     */
    private static String buildReport(List<String> lines) {
        List<String> errors = new ArrayList<>();
        int validCount = 0;
        double totalAverage = 0.0;
        double maxAverage = Double.NEGATIVE_INFINITY;
        int scholarshipCount = 0;

        for (int index = 0; index < lines.size(); index++) {
            String line = lines.get(index);
            int lineNumber = index + 1;

            if (line.isBlank()) {
                errors.add("Рядок %d: порожній рядок".formatted(lineNumber));
                continue;
            }

            String[] fields = line.split(";", -1);
            if (fields.length != 5) {
                errors.add("Рядок %d: очікується 5 полів, отримано %d".formatted(lineNumber, fields.length));
                continue;
            }

            String name = fields[0].trim();
            String group = fields[1].trim();

            if (name.isBlank() || group.isBlank()) {
                errors.add("Рядок %d: порожнє ім'я або назва групи".formatted(lineNumber));
                continue;
            }

            try {
                int course = Integer.parseInt(fields[2].trim());
                double average = Double.parseDouble(fields[3].trim());
                boolean scholarship = Boolean.parseBoolean(fields[4].trim());

                if (course < 0 || average < 0) {
                    errors.add("Рядок %d: від'ємне числове значення".formatted(lineNumber));
                    continue;
                }

                validCount++;
                totalAverage += average;
                maxAverage = Math.max(maxAverage, average);
                if (scholarship) {
                    scholarshipCount++;
                }
            } catch (NumberFormatException exception) {
                errors.add("Рядок %d: числове поле має помилковий формат".formatted(lineNumber));
            }
        }

        double meanAverage = validCount == 0 ? 0.0 : totalAverage / validCount;
        if (validCount == 0) {
            maxAverage = 0.0;
        }

        StringBuilder sb = new StringBuilder();
        sb.append(String.format(Locale.ROOT, "Коректних записів: %d%n", validCount));
        sb.append(String.format(Locale.ROOT, "Середній бал: %.2f%n", meanAverage));
        sb.append(String.format(Locale.ROOT, "Найбільший бал: %.2f%n", maxAverage));
        sb.append(String.format(Locale.ROOT, "Кількість стипендіатів: %d%n", scholarshipCount));
        sb.append(String.format(Locale.ROOT, "Помилок: %d%n", errors.size()));
        for (String error : errors) {
            sb.append(error).append(System.lineSeparator());
        }

        return sb.toString();
    }
}
