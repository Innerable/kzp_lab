package ua.lpnu.kzp;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Плоский запис реєстру студентів для CSV-експорту (варіант 6).
 *
 * <p>Стовпці CSV (порядок за іменами полів): {@code середній бал, ПІБ, група, курс}.
 * Ключ round-trip: {@code fullName + group}.
 */
public final class LearnerRecord {
    private static final int FIELD_COUNT = 4;
    private static final int MIN_YEAR = 1;
    private static final int MAX_YEAR = 6;
    private static final double MAX_AVERAGE = 5.0;

    @CsvColumn("середній бал")
    private final double average;

    @CsvColumn("ПІБ")
    private final String fullName;

    @CsvColumn("група")
    private final String group;

    @CsvColumn("курс")
    private final int studyYear;

    /**
     * Створює перевірений запис.
     *
     * @param fullName ПІБ (непорожнє)
     * @param group група (непорожня)
     * @param studyYear курс (1-6)
     * @param average середній бал (0-5)
     * @throws IllegalArgumentException якщо значення некоректні
     */
    public LearnerRecord(String fullName, String group, int studyYear, double average) {
        if (fullName == null || fullName.isBlank() || group == null || group.isBlank()) {
            throw new IllegalArgumentException("ПІБ і група не можуть бути порожніми");
        }
        if (studyYear < MIN_YEAR || studyYear > MAX_YEAR) {
            throw new IllegalArgumentException("курс має бути від %d до %d".formatted(MIN_YEAR, MAX_YEAR));
        }
        if (Double.isNaN(average) || average < 0 || average > MAX_AVERAGE) {
            throw new IllegalArgumentException("середній бал має бути від 0 до %s".formatted(MAX_AVERAGE));
        }
        this.fullName = fullName.trim();
        this.group = group.trim();
        this.studyYear = studyYear;
        this.average = average;
    }

    /**
     * Створює запис зі студента реєстру.
     *
     * @param student студент
     * @return плоский запис
     */
    public static LearnerRecord from(Student student) {
        return new LearnerRecord(student.getName(), student.getGroup(),
                student.getCourse(), student.getAverage());
    }

    /**
     * Створює запис із одного CSV-рядка.
     *
     * @param line CSV-рядок
     * @return запис
     * @throws DataStorageException якщо рядок хибний
     */
    public static LearnerRecord fromCsv(String line) throws DataStorageException {
        return fromFields(CsvParser.parse(line));
    }

    /**
     * Створює запис із розібраних CSV-полів у порядку заголовка.
     *
     * @param fields поля: середній бал, ПІБ, група, курс
     * @return запис
     * @throws DataStorageException якщо кількість полів, число або значення хибні
     */
    public static LearnerRecord fromFields(List<String> fields) throws DataStorageException {
        if (fields.size() != FIELD_COUNT) {
            throw new DataStorageException(
                    "Очікується %d поля, отримано %d".formatted(FIELD_COUNT, fields.size()));
        }
        try {
            return new LearnerRecord(fields.get(1), fields.get(2),
                    Integer.parseInt(fields.get(3).trim()), Double.parseDouble(fields.get(0).trim()));
        } catch (IllegalArgumentException exception) {
            throw new DataStorageException("Некоректний рядок студента", exception);
        }
    }

    /**
     * Читає CSV-файл (UTF-8) і відновлює записи; перший запис — заголовок.
     *
     * @param path шлях до CSV-файлу
     * @return відновлені записи (порожній список для порожнього файлу або лише заголовка)
     * @throws DataStorageException якщо файл не прочитано або формат хибний
     */
    public static List<LearnerRecord> readCsv(Path path) throws DataStorageException {
        String text;
        try {
            text = Files.readString(path, StandardCharsets.UTF_8);
        } catch (IOException exception) {
            throw new DataStorageException("Не вдалося прочитати CSV: %s".formatted(path), exception);
        }
        List<List<String>> rows = CsvParser.parseDocument(text);
        List<LearnerRecord> result = new ArrayList<>();
        for (int index = 1; index < rows.size(); index++) {
            result.add(fromFields(rows.get(index)));
        }
        return result;
    }

    /**
     * Повертає ПІБ.
     *
     * @return ПІБ
     */
    public String getFullName() {
        return fullName;
    }

    /**
     * Повертає групу.
     *
     * @return група
     */
    public String getGroup() {
        return group;
    }

    /**
     * Повертає курс.
     *
     * @return курс
     */
    public int getStudyYear() {
        return studyYear;
    }

    /**
     * Повертає середній бал.
     *
     * @return середній бал
     */
    public double getAverage() {
        return average;
    }

    /** Порівнює записи за всіма полями. */
    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof LearnerRecord learner)) {
            return false;
        }
        return studyYear == learner.studyYear
                && Double.compare(average, learner.average) == 0
                && fullName.equals(learner.fullName)
                && group.equals(learner.group);
    }

    /** Повертає хеш-код, узгоджений із {@code equals}. */
    @Override
    public int hashCode() {
        return Objects.hash(fullName, group, studyYear, average);
    }

    @Override
    public String toString() {
        return "LearnerRecord[%s, %s, курс %d, бал %s]".formatted(fullName, group, studyYear, average);
    }
}
