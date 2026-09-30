package ua.lpnu.kzp;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import org.junit.jupiter.api.Test;

class MainTest {

    @Test
    void buildReportForValidRecords() {
        List<String> lines = List.of(
                "Іваненко Іван;КН-31;3;88.5;true",
                "Петренко Петро;КН-32;2;70.0;false");

        String report = Main.buildReport(lines);

        String expected = String.format(
                "Коректних записів: 2%nСередній бал: 79.25%nНайбільший бал: 88.50%n"
                        + "Кількість стипендіатів: 1%nПомилок: 0%n");
        assertEquals(expected, report);
    }

    @Test
    void buildReportKeepsErrorMessagesWithLineNumbers() {
        List<String> lines = List.of(
                "Іван;КН-31;1;80.0;true",
                "",
                "a;b;c",
                "Петро;КН-32;1;abc;true",
                "Марія;КН-33;-1;90.0;false");

        String report = Main.buildReport(lines);

        String sep = System.lineSeparator();
        String expected = String.format(
                "Коректних записів: 1%nСередній бал: 80.00%nНайбільший бал: 80.00%n"
                        + "Кількість стипендіатів: 1%nПомилок: 4%n")
                + "Рядок 2: порожній рядок" + sep
                + "Рядок 3: очікується 5 полів, отримано 3" + sep
                + "Рядок 4: числове поле має помилковий формат" + sep
                + "Рядок 5: від'ємне числове значення" + sep;
        assertEquals(expected, report);
    }

    @Test
    void buildReportForEmptyInputGivesZeros() {
        String report = Main.buildReport(List.of());

        String expected = String.format(
                "Коректних записів: 0%nСередній бал: 0.00%nНайбільший бал: 0.00%n"
                        + "Кількість стипендіатів: 0%nПомилок: 0%n");
        assertEquals(expected, report);
    }

    @Test
    void summarizeReturnsExpectedRecordValue() {
        List<Student> students = List.of(
                new Student("Іван", "КН-31", 3, 88.5, true),
                new Student("Петро", "КН-32", 2, 70.0, false));

        assertEquals(new Main.Summary(2, 79.25, 88.5, 1), Main.summarize(students));
    }

    @Test
    void summarizeOfEmptyListIsAllZeros() {
        assertEquals(new Main.Summary(0, 0.0, 0.0, 0), Main.summarize(List.of()));
    }
}
