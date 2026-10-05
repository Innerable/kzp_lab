package ua.lpnu.kzp;

/** Освітній статус (бакалaврат/магістратура) студента. */
public enum StudyStatus {
    BACHELOR("бакалавр"),
    MASTER("магістр");

    public static final int LAST_BACHELOR_COURSE = 4;

    private final String label;

    StudyStatus(String label) {
        this.label = label;
    }


    public String label() {
        return label;
    }


    public static StudyStatus forCourse(int course) {
        return course <= LAST_BACHELOR_COURSE ? BACHELOR : MASTER;
    }
}
