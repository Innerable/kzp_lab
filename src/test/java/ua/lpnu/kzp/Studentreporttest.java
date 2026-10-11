package ua.lpnu.kzp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.DoubleSummaryStatistics;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class StudentReportTest {

    private static final double DELTA = 1e-9;

    // 7 записів: межа фільтра (4.5), рівні бали (4.8) для другого критерію,
    // дубльований ПІБ (Іваненко Іван) і шостий/сьомий записи для усікання top-5.
    private final List<Student> students = List.of(
            new Master("Петренко Петро", "КН-51", 5, 4.5, false),
            new Bachelor("Коваль Олена", "КН-31", 2, 4.8, false),
            new Bachelor("Мельник Марія", "КН-32", 1, 3.9, false),
            new Bachelor("Іваненко Іван", "КН-31", 3, 4.8, true),
            new Bachelor("Шевченко Тарас", "КН-32", 4, 4.2, true),
            new Master("Бондар Андрій", "КН-51", 6, 3.0, false),
            new Bachelor("Іваненко Іван", "КН-33", 1, 3.5, false));

    private static List<String> namesOf(List<Student> list) {
        List<String> result = new ArrayList<>();
        for (Student student : list) {
            result.add(student.getName());
        }
        return result;
    }

    // ---------- filter ----------

    @Test
    void highAchieversKeepsSourceOrderAndIncludesBoundary() {
        assertEquals(List.of("Петренко Петро", "Коваль Олена", "Іваненко Іван"),
                namesOf(StudentReport.highAchievers(students)));
    }

    @Test
    void highAchieversSplitsOnThreshold() {
        List<Student> edge = List.of(
                new Bachelor("A", "G", 1, 4.49, false),
                new Bachelor("B", "G", 1, 4.5, false),
                new Bachelor("C", "G", 1, 4.51, false));

        assertEquals(List.of("B", "C"), namesOf(StudentReport.highAchievers(edge)));
    }

    // ---------- map ----------

    @Test
    void namesReturnsAllNamesIncludingDuplicates() {
        assertEquals(List.of("Петренко Петро", "Коваль Олена", "Мельник Марія", "Іваненко Іван",
                "Шевченко Тарас", "Бондар Андрій", "Іваненко Іван"), StudentReport.names(students));
    }

    // ---------- groupingBy ----------

    @Test
    void countByGroupCountsStudentsPerGroup() {
        Map<String, Long> counts = StudentReport.countByGroup(students);

        assertEquals(Map.of("КН-31", 2L, "КН-32", 2L, "КН-33", 1L, "КН-51", 2L), counts);
        assertEquals(List.of("КН-31", "КН-32", "КН-33", "КН-51"), new ArrayList<>(counts.keySet()));
    }

    // ---------- statistics ----------

    @Test
    void averageStatisticsSummarizesAverages() {
        DoubleSummaryStatistics statistics = StudentReport.averageStatistics(students);

        assertEquals(7L, statistics.getCount());
        assertEquals(28.7, statistics.getSum(), DELTA);
        assertEquals(4.1, statistics.getAverage(), DELTA);
        assertEquals(3.0, statistics.getMin(), DELTA);
        assertEquals(4.8, statistics.getMax(), DELTA);
    }

    @Test
    void averageStatisticsMatchesLegacyLoop() {
        double total = 0.0;
        double max = Double.NEGATIVE_INFINITY;
        for (Student student : students) {
            total += student.getAverage();
            max = Math.max(max, student.getAverage());
        }

        DoubleSummaryStatistics statistics = StudentReport.averageStatistics(students);

        assertEquals(total, statistics.getSum(), DELTA);
        assertEquals(total / students.size(), statistics.getAverage(), DELTA);
        assertEquals(max, statistics.getMax(), DELTA);
    }

    // ---------- top-N ----------

    @Test
    void topByAverageOrdersByAverageThenName() {
        List<Student> top = StudentReport.topByAverage(students, StudentReport.TOP_LIMIT);

        assertEquals(List.of("Іваненко Іван", "Коваль Олена", "Петренко Петро",
                "Шевченко Тарас", "Мельник Марія"), namesOf(top));
        assertEquals("КН-31", top.get(0).getGroup());
    }

    @Test
    void topByAverageHandlesLimitBoundaries() {
        assertEquals(List.of(), StudentReport.topByAverage(students, 0));
        assertEquals(1, StudentReport.topByAverage(students, 1).size());
        assertEquals(7, StudentReport.topByAverage(students, 100).size());
    }

    @Test
    void topByAverageRejectsNegativeLimit() {
        assertThrows(IllegalArgumentException.class, () -> StudentReport.topByAverage(students, -1));
    }

    // ---------- Optional ----------

    @Test
    void findByNameReturnsFirstOfDuplicates() {
        Optional<Student> found = StudentReport.findByName(students, "Іваненко Іван");

        assertTrue(found.isPresent());
        assertEquals("КН-31", found.get().getGroup());
    }

    @Test
    void findByNameReturnsEmptyForMissingName() {
        assertTrue(StudentReport.findByName(students, "Невідомий").isEmpty());
    }

    @Test
    void findByNameRejectsNullName() {
        assertThrows(NullPointerException.class, () -> StudentReport.findByName(students, null));
    }

    // ---------- порожній вхід і відсутність мутації ----------

    @Test
    void emptyInputGivesEmptyResults() {
        List<Student> empty = List.of();

        assertTrue(StudentReport.highAchievers(empty).isEmpty());
        assertTrue(StudentReport.names(empty).isEmpty());
        assertTrue(StudentReport.countByGroup(empty).isEmpty());
        assertTrue(StudentReport.topByAverage(empty, 5).isEmpty());
        assertTrue(StudentReport.findByName(empty, "Іван").isEmpty());

        DoubleSummaryStatistics statistics = StudentReport.averageStatistics(empty);
        assertEquals(0L, statistics.getCount());
        assertEquals(0.0, statistics.getSum(), DELTA);
        assertEquals(Double.POSITIVE_INFINITY, statistics.getMin());
        assertEquals(Double.NEGATIVE_INFINITY, statistics.getMax());
    }

    @Test
    void queriesRejectNullList() {
        assertThrows(NullPointerException.class, () -> StudentReport.names(null));
    }

    @Test
    void queriesDoNotMutateInput() {
        List<Student> before = new ArrayList<>(students);

        StudentReport.highAchievers(students);
        StudentReport.names(students);
        StudentReport.countByGroup(students);
        StudentReport.averageStatistics(students);
        StudentReport.topByAverage(students, 5);
        StudentReport.findByName(students, "Іваненко Іван");

        assertEquals(before, students);
        assertEquals("Петренко Петро", students.get(0).getName());
    }
}
