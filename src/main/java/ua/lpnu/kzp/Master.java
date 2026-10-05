package ua.lpnu.kzp;

/** Студент магістратури (курси понад 4). */
public final class Master extends Student {
    private static final double AVERAGE_WEIGHT = 1.2;
    private static final double SCHOLARSHIP_BONUS = 10.0;


    public Master(String name, String group, int course, double average, boolean scholarship) {
        super(name, group, course, average, scholarship, StudyStatus.MASTER);
        if (course <= StudyStatus.LAST_BACHELOR_COURSE) {
            throw new IllegalArgumentException(
                    "магістр має навчатися на курсі понад %d".formatted(StudyStatus.LAST_BACHELOR_COURSE));
        }
    }

    @Override
    public double rating() {
        return getAverage() * AVERAGE_WEIGHT + (hasScholarship() ? SCHOLARSHIP_BONUS : 0.0);
    }
}
