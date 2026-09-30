package ua.lpnu.kzp;

import java.util.Locale;

/**
 * Сутність предметної області: запис про студента з реєстру.
 * Стан об'єкта прихований, інваріанти перевіряються в конструкторі.
 */
public final class Student {
    private final String name;
    private final String group;
    private final int course;
    private final double average;
    private final boolean scholarship;

    /**
     * Створює студента та перевіряє коректність значень.
     *
     * @param name        ім'я
     * @param group       назва групи
     * @param course      курс (не від'ємний)
     * @param average     середній бал (не від'ємний)
     * @param scholarship чи отримує стипендію
     * @throws IllegalArgumentException якщо значення хибні
     */
    public Student(String name, String group, int course, double average, boolean scholarship) {
        if (name == null || name.isBlank() || group == null || group.isBlank()) {
            throw new IllegalArgumentException("порожнє ім'я або назва групи");
        }
        if (course < 0 || average < 0) {
            throw new IllegalArgumentException("від'ємне числове значення");
        }
        this.name = name.trim();
        this.group = group.trim();
        this.course = course;
        this.average = average;
        this.scholarship = scholarship;
    }

    /**
     * Фабричний метод: створює студента з CSV-рядка формату
     * {@code ім'я;група;курс;середній бал;стипендія}.
     *
     * @param line рядок CSV
     * @return об'єкт студента
     * @throws IllegalArgumentException якщо рядок має хибний формат або значення
     */
    public static Student fromCsv(String line) {
        if (line == null || line.isBlank()) {
            throw new IllegalArgumentException("порожній рядок");
        }

        String[] fields = line.split(";", -1);
        if (fields.length != 5) {
            throw new IllegalArgumentException(
                    "очікується 5 полів, отримано %d".formatted(fields.length));
        }

        int course;
        double average;
        try {
            course = Integer.parseInt(fields[2].trim());
            average = Double.parseDouble(fields[3].trim());
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("числове поле має помилковий формат", exception);
        }
        boolean scholarship = Boolean.parseBoolean(fields[4].trim());

        return new Student(fields[0], fields[1], course, average, scholarship);
    }

    public String getName() {
        return name;
    }

    public String getGroup() {
        return group;
    }

    public int getCourse() {
        return course;
    }

    public double getAverage() {
        return average;
    }

    public boolean hasScholarship() {
        return scholarship;
    }

    @Override
    public String toString() {
        return String.format(Locale.ROOT, "%s (%s, курс %d, бал %.2f, стипендія: %s)",
                name, group, course, average, scholarship ? "так" : "ні");
    }
}
