package ua.lpnu.kzp;

import java.util.Locale;
import java.util.Objects;


public class Student {
    private final String name;
    private final String group;
    private final int course;
    private final double average;
    private final boolean scholarship;
    private final StudyStatus status;

    public Student(String name, String group, int course, double average, boolean scholarship) {
        this(validate(name, group, course, average, StudyStatus.forCourse(course)),
                name, group, course, average, scholarship, StudyStatus.forCourse(course));
    }

    protected Student(String name, String group, int course, double average,
                      boolean scholarship, StudyStatus status) {
        this(validate(name, group, course, average, status),
                name, group, course, average, scholarship, status);
    }

    private Student(boolean validated, String name, String group, int course,
                    double average, boolean scholarship, StudyStatus status) {
        this.name = name.trim(); //Для виведення з Upper-case
        this.group = group.trim();
        this.course = course;
        this.average = average;
        this.scholarship = scholarship;
        this.status = status;
    }

    private static boolean validate(String name, String group, int course,
                                    double average, StudyStatus status) {
        if (name == null || name.isBlank() || group == null || group.isBlank()) {
            throw new IllegalArgumentException("порожнє ім'я або назва групи");
        }
        if (Double.isNaN(average) || Double.isInfinite(average)) {
            throw new IllegalArgumentException("середній бал має бути скінченним числом");
        }
        if (course < 0 || average < 0) {
            throw new IllegalArgumentException("від'ємне числове значення");
        }
        Objects.requireNonNull(status, "Рівень не може бути null");
        return true;
    }

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

        if (StudyStatus.forCourse(course) == StudyStatus.MASTER) {
            return new Master(fields[0], fields[1], course, average, scholarship);
        }
        return new Bachelor(fields[0], fields[1], course, average, scholarship);
    }

    public double rating() {
        return average;
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

    public StudyStatus getStatus() {
        return status;
    }

    @Override
    public final boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || getClass() != other.getClass()) {
            return false;
        }
        Student student = (Student) other;
        return name.equals(student.name) && group.equals(student.group);
    }

    @Override
    public final int hashCode() {
        return Objects.hash(getClass(), name, group);
    }

    @Override
    public String toString() {
        return String.format(Locale.ROOT, "%s (%s, курс %d, бал %.2f, стипендія: %s)",
                name, group, course, average, scholarship ? "так" : "ні");
    }
}
