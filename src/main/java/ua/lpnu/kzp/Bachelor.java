package ua.lpnu.kzp;

/** Студент бакалаврату (курси 1-4). */
public final class Bachelor extends Student {
    private static final double SCHOLARSHIP_BONUS = 5.0;

    public Bachelor(String name, String group, int course, double average, boolean scholarship) {
        super(name, group, course, average, scholarship, StudyStatus.BACHELOR);
        if (course > StudyStatus.LAST_BACHELOR_COURSE) {
            throw new IllegalArgumentException(
                    "бакалавр не може навчатися на курсі понад %d".formatted(StudyStatus.LAST_BACHELOR_COURSE));
        }
    }


    @Override
    public double rating() {
        return getAverage() + (hasScholarship() ? SCHOLARSHIP_BONUS : 0.0);
    }
}
