package ua.lpnu.kzp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;

class StudentHierarchyTest {

    private static final double DELTA = 1e-9;

    // ---------- Bachelor ----------

    @Test
    void bachelorAcceptsLastBachelorCourse() {
        Bachelor bachelor = new Bachelor("Іван", "КН-31", 4, 4.5, false);

        assertEquals(4, bachelor.getCourse());
        assertEquals(StudyStatus.BACHELOR, bachelor.getStatus());
    }

    @Test
    void bachelorRejectsMasterCourse() {
        assertThrows(IllegalArgumentException.class, () -> new Bachelor("Іван", "КН-31", 5, 4.5, false));
    }

    @Test
    void bachelorRejectsInvalidCommonState() {
        assertThrows(IllegalArgumentException.class, () -> new Bachelor(" ", "КН-31", 1, 4.5, false));
        assertThrows(IllegalArgumentException.class, () -> new Bachelor("Іван", "КН-31", 1, -0.1, false));
        assertThrows(IllegalArgumentException.class, () -> new Bachelor("Іван", "КН-31", -1, 4.5, false));
    }

    // ---------- Master ----------

    @Test
    void masterAcceptsFirstMasterCourse() {
        Master master = new Master("Петро", "КН-51", 5, 4.5, false);

        assertEquals(5, master.getCourse());
        assertEquals(StudyStatus.MASTER, master.getStatus());
    }

    @Test
    void masterRejectsBachelorCourse() {
        assertThrows(IllegalArgumentException.class, () -> new Master("Петро", "КН-51", 4, 4.5, false));
    }

    @Test
    void masterRejectsInvalidCommonState() {
        assertThrows(IllegalArgumentException.class, () -> new Master(null, "КН-51", 5, 4.5, false));
        assertThrows(IllegalArgumentException.class, () -> new Master("Петро", "КН-51", 5, Double.NaN, false));
    }

    // ---------- Поліморфізм ----------

    @Test
    void ratingIsCalculatedByActualSubtype() {
        Student bachelor = new Bachelor("Іван", "КН-31", 3, 4.0, false);
        Student master = new Master("Петро", "КН-51", 5, 4.0, false);

        assertEquals(4.0, bachelor.rating(), DELTA);
        assertEquals(4.8, master.rating(), DELTA);
        assertNotEquals(bachelor.rating(), master.rating());
    }

    @Test
    void scholarshipBonusDiffersBySubtype() {
        Student bachelor = new Bachelor("Іван", "КН-31", 3, 4.0, true);
        Student master = new Master("Петро", "КН-51", 5, 4.0, true);

        assertEquals(9.0, bachelor.rating(), DELTA);
        assertEquals(14.8, master.rating(), DELTA);
    }

    @Test
    void baseStudentRatingEqualsAverage() {
        assertEquals(3.7, new Student("Олег", "КН-11", 1, 3.7, true).rating(), DELTA);
    }

    @Test
    void zeroAverageGivesZeroRatingWithoutScholarship() {
        assertEquals(0.0, new Bachelor("Іван", "КН-31", 1, 0.0, false).rating(), DELTA);
        assertEquals(0.0, new Master("Петро", "КН-51", 5, 0.0, false).rating(), DELTA);
    }

    @Test
    void polymorphicListUsesActualImplementations() {
        List<Student> students = List.of(
                new Bachelor("Іван", "КН-31", 3, 4.0, false),
                new Master("Петро", "КН-51", 5, 4.0, false),
                new Student("Олег", "КН-11", 1, 4.0, false));

        double total = 0.0;
        for (Student student : students) {
            total += student.rating();
        }

        assertEquals(12.8, total, DELTA);
    }

    @Test
    void buildRatingsUsesSubtypeRatings() {
        List<Student> students = List.of(
                new Bachelor("Іван", "КН-31", 3, 4.0, true),
                new Master("Петро", "КН-51", 5, 4.0, false));

        String expected = String.format("Іван (бакалавр): рейтинг 9.00%nПетро (магістр): рейтинг 4.80%n");
        assertEquals(expected, Main.buildRatings(students));
    }

    // ---------- enum StudyStatus ----------

    @Test
    void forCourseSplitsBachelorAndMasterOnBoundary() {
        assertEquals(StudyStatus.BACHELOR, StudyStatus.forCourse(0));
        assertEquals(StudyStatus.BACHELOR, StudyStatus.forCourse(4));
        assertEquals(StudyStatus.MASTER, StudyStatus.forCourse(5));
        assertEquals(StudyStatus.MASTER, StudyStatus.forCourse(6));
    }

    @Test
    void enumLabelsAreReadable() {
        assertEquals("бакалавр", StudyStatus.BACHELOR.label());
        assertEquals("магістр", StudyStatus.MASTER.label());
    }

    @Test
    void baseStudentStatusFollowsCourse() {
        assertEquals(StudyStatus.BACHELOR, new Student("Олег", "КН-11", 2, 4.0, false).getStatus());
        assertEquals(StudyStatus.MASTER, new Student("Олег", "КН-51", 5, 4.0, false).getStatus());
    }

    @Test
    void fromCsvCreatesBachelorForCourseFour() {
        Student student = Student.fromCsv("Іван;КН-31;4;4.5;false");

        assertTrue(student instanceof Bachelor);
        assertEquals(StudyStatus.BACHELOR, student.getStatus());
    }

    @Test
    void fromCsvCreatesMasterForCourseFive() {
        Student student = Student.fromCsv("Петро;КН-51;5;4.5;false");

        assertTrue(student instanceof Master);
        assertEquals(StudyStatus.MASTER, student.getStatus());
    }

    // ---------- equals і hashCode ----------

    @Test
    void equalStudentsHaveSameHashCode() {
        Student first = new Bachelor("Іван", "КН-31", 3, 4.0, true);
        Student second = new Bachelor("Іван", "КН-31", 3, 4.0, true);

        assertEquals(first, second);
        assertEquals(first.hashCode(), second.hashCode());
    }

    @Test
    void hashSetKeepsOneEntryForEqualStudents() {
        Set<Student> students = new HashSet<>();
        students.add(new Bachelor("Іван", "КН-31", 3, 4.0, true));
        students.add(new Bachelor("  Іван ", "КН-31", 3, 4.0, true));

        assertEquals(1, students.size());
    }

    @Test
    void equalityIgnoresChangeableFields() {
        Student first = new Bachelor("Іван", "КН-31", 3, 4.0, true);
        Student second = new Bachelor("Іван", "КН-31", 2, 3.0, false);

        assertEquals(first, second);
    }

    @Test
    void differentSubtypesWithSameNameAndGroupAreNotEqual() {
        Student bachelor = new Bachelor("Іван", "КН-31", 4, 4.0, false);
        Student master = new Master("Іван", "КН-31", 5, 4.0, false);

        assertNotEquals(bachelor, master);
        Set<Student> students = new HashSet<>(List.of(bachelor, master));
        assertEquals(2, students.size());
    }

    @Test
    void differentNameOrGroupAreNotEqual() {
        Student base = new Bachelor("Іван", "КН-31", 3, 4.0, true);

        assertNotEquals(base, new Bachelor("Петро", "КН-31", 3, 4.0, true));
        assertNotEquals(base, new Bachelor("Іван", "КН-32", 3, 4.0, true));
    }

    @Test
    void hashMapFindsValueByEqualKey() {
        Map<Student, String> notes = new HashMap<>();
        notes.put(new Master("Петро", "КН-51", 5, 4.0, false), "диплом");

        assertEquals("диплом", notes.get(new Master("Петро", "КН-51", 6, 3.0, true)));
    }

    @Test
    void equalsHandlesNullAndForeignTypes() {
        Student student = new Bachelor("Іван", "КН-31", 3, 4.0, true);

        assertFalse(student.equals(null));
        assertFalse(student.equals("Іван"));
        assertTrue(student.equals(student));
    }
}
