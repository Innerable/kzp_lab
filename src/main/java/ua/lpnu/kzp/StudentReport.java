package ua.lpnu.kzp;
import java.util.Comparator;
import java.util.DoubleSummaryStatistics;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class StudentReport {
    public static final double HIGH_AVERAGE_THRESHOLD = 4.5;
    public static final int TOP_LIMIT = 5;

    private static final Comparator<Student> BY_AVERAGE_DESC_THEN_NAME = Comparator
            .comparingDouble(Student::getAverage)
            .reversed()
            .thenComparing(Student::getName);

    private StudentReport() {
    }

    public static List<Student> highAchievers(List<Student> students) {
        // filter залишає студентів із балом не нижче порога, toList збирає їх у новий список.
        return streamOf(students)
                .filter(student -> student.getAverage() >= HIGH_AVERAGE_THRESHOLD)
                .toList();
    }

    public static List<String> names(List<Student> students) {
        // map перетворює кожного студента на його ПІБ, порядок вхідного списку зберігається.
        return streamOf(students)
                .map(Student::getName)
                .toList();
    }

    public static Map<String, Long> countByGroup(List<Student> students) {
        // groupingBy групує за назвою групи в TreeMap, counting рахує студентів у кожній групі.
        return streamOf(students)
                .collect(Collectors.groupingBy(
                        Student::getGroup,
                        TreeMap::new,
                        Collectors.counting()));
    }

    public static DoubleSummaryStatistics averageStatistics(List<Student> students) {
        // summarizingDouble за один прохід дає кількість, суму, середнє, мінімум і максимум балу.
        return streamOf(students)
                .collect(Collectors.summarizingDouble(Student::getAverage));
    }

    public static List<Student> topByAverage(List<Student> students, int limit) {
        if (limit < 0) {
            throw new IllegalArgumentException("Ліміт не може бути від'ємним");
        }
        // sorted впорядковує всіх за балом спаданням і ПІБ зростанням, limit лишає перші N.
        return streamOf(students)
                .sorted(BY_AVERAGE_DESC_THEN_NAME)
                .limit(limit)
                .toList();
    }

    public static Optional<Student> findByName(List<Student> students, String name) {
        Objects.requireNonNull(name, "ПІБ не може бути null");
        // filter відбирає збіг за ПІБ, findFirst повертає перший запис або Optional.empty().
        return streamOf(students)
                .filter(student -> student.getName().equals(name))
                .findFirst();
    }

    private static Stream<Student> streamOf(List<Student> students) {
        return Objects.requireNonNull(students, "Список не може бути null").stream();
    }
}
