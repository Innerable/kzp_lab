package ua.lpnu.kzp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class StudentTest {

    // ---------- конструктор ----------

    @Test
    void constructorStoresAllFields() {
        Student student = new Student("Іваненко Іван", "КН-31", 3, 88.5, true);

        assertEquals("Іваненко Іван", student.getName());
        assertEquals("КН-31", student.getGroup());
        assertEquals(3, student.getCourse());
        assertEquals(88.5, student.getAverage(), 1e-9);
        assertTrue(student.hasScholarship());
    }

    @Test
    void constructorTrimsNameAndGroup() {
        Student student = new Student("  Іван  ", " КН-31 ", 1, 70.0, false);

        assertEquals("Іван", student.getName());
        assertEquals("КН-31", student.getGroup());
    }

    @Test
    void constructorRejectsNullName() {
        assertThrows(IllegalArgumentException.class, () -> new Student(null, "КН-31", 1, 70.0, false));
    }

    @Test
    void constructorRejectsBlankName() {
        assertThrows(IllegalArgumentException.class, () -> new Student("   ", "КН-31", 1, 70.0, false));
    }

    @Test
    void constructorRejectsNullGroup() {
        assertThrows(IllegalArgumentException.class, () -> new Student("Іван", null, 1, 70.0, false));
    }

    @Test
    void constructorRejectsBlankGroup() {
        assertThrows(IllegalArgumentException.class, () -> new Student("Іван", "", 1, 70.0, false));
    }

    @Test
    void constructorRejectsNegativeCourse() {
        assertThrows(IllegalArgumentException.class, () -> new Student("Іван", "КН-31", -1, 70.0, false));
    }

    @Test
    void constructorAcceptsZeroCourse() {
        assertEquals(0, new Student("Іван", "КН-31", 0, 70.0, false).getCourse());
    }

    @Test
    void constructorRejectsNegativeAverage() {
        assertThrows(IllegalArgumentException.class, () -> new Student("Іван", "КН-31", 1, -0.1, false));
    }

    @Test
    void constructorAcceptsZeroAverage() {
        assertEquals(0.0, new Student("Іван", "КН-31", 1, 0.0, false).getAverage(), 1e-9);
    }

    @Test
    void constructorRejectsNaNAverage() {
        assertThrows(IllegalArgumentException.class, () -> new Student("Іван", "КН-31", 1, Double.NaN, false));
    }

    @Test
    void constructorRejectsInfiniteAverage() {
        assertThrows(IllegalArgumentException.class,
                () -> new Student("Іван", "КН-31", 1, Double.POSITIVE_INFINITY, false));
        assertThrows(IllegalArgumentException.class,
                () -> new Student("Іван", "КН-31", 1, Double.NEGATIVE_INFINITY, false));
    }

    // ---------- fromCsv ----------

    @Test
    void fromCsvParsesValidLine() {
        Student student = Student.fromCsv("Іваненко Іван;КН-31;3;88.5;true");

        assertEquals("Іваненко Іван", student.getName());
        assertEquals("КН-31", student.getGroup());
        assertEquals(3, student.getCourse());
        assertEquals(88.5, student.getAverage(), 1e-9);
        assertTrue(student.hasScholarship());
    }

    @Test
    void fromCsvTrimsSpacesAroundFields() {
        Student student = Student.fromCsv(" Іван ; КН-31 ; 2 ; 75.0 ; false ");

        assertEquals("Іван", student.getName());
        assertEquals(2, student.getCourse());
        assertFalse(student.hasScholarship());
    }

    @Test
    void fromCsvTreatsNonTrueAsNoScholarship() {
        assertFalse(Student.fromCsv("Іван;КН-31;1;70.0;так").hasScholarship());
    }

    @Test
    void fromCsvRejectsTooFewFields() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> Student.fromCsv("Іван;КН-31;1;70.0"));
        assertEquals("очікується 5 полів, отримано 4", ex.getMessage());
    }

    @Test
    void fromCsvRejectsTooManyFields() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> Student.fromCsv("Іван;КН-31;1;70.0;true;зайве"));
        assertEquals("очікується 5 полів, отримано 6", ex.getMessage());
    }

    @Test
    void fromCsvRejectsInvalidCourse() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> Student.fromCsv("Іван;КН-31;abc;70.0;true"));
        assertEquals("числове поле має помилковий формат", ex.getMessage());
    }

    @Test
    void fromCsvRejectsInvalidAverage() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> Student.fromCsv("Іван;КН-31;1;xyz;true"));
        assertEquals("числове поле має помилковий формат", ex.getMessage());
    }

    @Test
    void fromCsvRejectsNegativeValue() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> Student.fromCsv("Іван;КН-31;1;-5.0;true"));
        assertEquals("від'ємне числове значення", ex.getMessage());
    }

    @Test
    void fromCsvRejectsEmptyNameOrGroup() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> Student.fromCsv(";КН-31;1;70.0;true"));
        assertEquals("порожнє ім'я або назва групи", ex.getMessage());
    }

    @Test
    void fromCsvRejectsBlankAndNullLine() {
        IllegalArgumentException blank = assertThrows(IllegalArgumentException.class,
                () -> Student.fromCsv("   "));
        assertEquals("порожній рядок", blank.getMessage());
        assertThrows(IllegalArgumentException.class, () -> Student.fromCsv(null));
    }

    // ---------- toString ----------

    @Test
    void toStringContainsReadableData() {
        String text = new Student("Іван", "КН-31", 3, 88.5, true).toString();

        assertEquals("Іван (КН-31, курс 3, бал 88.50, стипендія: так)", text);
    }
}
