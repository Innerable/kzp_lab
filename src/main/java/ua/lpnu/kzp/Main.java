package ua.lpnu.kzp;
import java.util.DoubleSummaryStatistics;
import java.util.stream.Collectors;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class Main {
    private static final String VERSION = "1.0.2";

    private Main() {
    }

    record Summary(int validCount, double meanAverage, double maxAverage, int scholarshipCount) {
    }

    public static void main(String[] args) {
        String inputPath = "data/input.csv";
        String outputPath = "out/report.txt";
        boolean showRatings = false;
        boolean showStream = false;

        for (int i = 0; i < args.length; i++) {
            if ("--version".equals(args[i])) {
                System.out.printf(Locale.ROOT, "lab01 версія %s%n", VERSION);
                return;
            }
            if ("--help".equals(args[i])) {
                System.out.printf(Locale.ROOT,
                        "Використання: java -jar lab01.jar [--help] [--version] [--input <файл>] [--output <файл>] [--ratings] [--stream]%n");
                return;
            }
            if ("--ratings".equals(args[i])) {
                showRatings = true;
            }
            if ("--stream".equals(args[i])) {
                showStream = true;
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
            List<String> errors = new ArrayList<>();
            List<Student> students = parseStudents(lines, errors);
            String report = formatReport(students, errors);
            System.out.print(report);

            Path output = Path.of(outputPath);
            Path outputParent = output.getParent();
            if (outputParent != null) {
                Files.createDirectories(outputParent);
            }
            Files.writeString(output, report, StandardCharsets.UTF_8);

            if (showRatings) {
                System.out.print(buildRatings(students));
            }
            if (showStream) {
                System.out.print(buildStreamReport(students));
            }
        } catch (IOException exception) {
            System.out.printf(Locale.ROOT, "Помилка читання/запису файлу: %s%n", exception.getMessage());
        }
    }


    static String buildRatings(List<Student> students) {
    return students.stream()
            .map(student -> String.format(Locale.ROOT, "%s (%s): рейтинг %.2f%n",
                    student.getName(), student.getStatus().label(), student.rating()))
            .collect(Collectors.joining());
    }

    private static List<Student> parseStudents(List<String> lines, List<String> errors) {
        List<Student> students = new ArrayList<>();
        for (int index = 0; index < lines.size(); index++) {
            try {
                students.add(Student.fromCsv(lines.get(index)));
            } catch (IllegalArgumentException exception) {
                errors.add("Рядок %d: %s".formatted(index + 1, exception.getMessage()));
            }
        }
        return students;
    }

    /* Формує текст звіту за готовими студентами та помилками. */
    private static String formatReport(List<Student> students, List<String> errors) {
        Summary summary = summarize(students);

        StringBuilder sb = new StringBuilder();
        sb.append(String.format(Locale.ROOT, "Коректних записів: %d%n", summary.validCount()));
        sb.append(String.format(Locale.ROOT, "Середній бал: %.2f%n", summary.meanAverage()));
        sb.append(String.format(Locale.ROOT, "Найбільший бал: %.2f%n", summary.maxAverage()));
        sb.append(String.format(Locale.ROOT, "Кількість стипендіатів: %d%n", summary.scholarshipCount()));
        sb.append(String.format(Locale.ROOT, "Помилок: %d%n", errors.size()));
        for (String error : errors) {
            sb.append(error).append(System.lineSeparator());
        }
        return sb.toString();
    }
    static String buildReport(List<String> lines) {
        List<String> errors = new ArrayList<>();
        List<Student> students = parseStudents(lines, errors);
        return formatReport(students, errors);
    }

    static String buildStreamReport(List<Student> students) {
    DoubleSummaryStatistics statistics = StudentReport.averageStatistics(students);
    String statisticsLine = statistics.getCount() == 0
            ? "немає даних"
            : String.format(Locale.ROOT, "мін %.2f, середнє %.2f, макс %.2f",
                    statistics.getMin(), statistics.getAverage(), statistics.getMax());
    return String.format(Locale.ROOT,
            "Бал не нижче %.2f: %s%nКількість за групою: %s%nСтатистика балу: %s%nТоп-%d: %s%n",
            StudentReport.HIGH_AVERAGE_THRESHOLD,
            StudentReport.names(StudentReport.highAchievers(students)),
            StudentReport.countByGroup(students),
            statisticsLine,
            StudentReport.TOP_LIMIT,
            StudentReport.names(StudentReport.topByAverage(students, StudentReport.TOP_LIMIT)));
    }

   static Summary summarize(List<Student> students) {
    if (students.isEmpty()) {
        return new Summary(0, 0.0, 0.0, 0);
    }
    DoubleSummaryStatistics statistics = StudentReport.averageStatistics(students);
    long scholarshipCount = students.stream()
            .filter(Student::hasScholarship)
            .count();
    return new Summary(Math.toIntExact(statistics.getCount()), statistics.getAverage(),
            statistics.getMax(), Math.toIntExact(scholarshipCount));
    }
}
