package ua.lpnu.kzp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class CsvExportTest {

    private static final String HEADER = "середній бал,ПІБ,група,курс";

    private final List<LearnerRecord> original = List.of(
            new LearnerRecord("Петренко Іван", "КІ-101", 2, 4.5),
            new LearnerRecord("Коваль, Олена", "КІ-102", 3, 4.8),
            new LearnerRecord("Тарас \"Кобзар\" Шевченко", "КІ-101", 1, 3.9),
            new LearnerRecord("Марія\nМельник", "КІ-103", 4, 4.0),
            new LearnerRecord("Бондар Андрій", "КН-51", 6, 0.0));

    // ---- Repository ----

    @Test
    void repositoryAddsAndFinds() {
        Repository<LearnerRecord> repository = new Repository<>();
        original.forEach(repository::add);
        assertEquals(original, repository.all());
        assertEquals(2, repository.find(r -> r.getGroup().equals("КІ-101")).size());
        Object anyCondition = null;
        assertTrue(repository.find(r -> anyCondition != null).isEmpty());
    }

    @Test
    void repositorySnapshotIsImmutableAndIndependent() {
        Repository<String> repository = new Repository<>();
        repository.add("a");
        List<String> snapshot = repository.all();
        repository.add("b");
        assertEquals(1, snapshot.size());
        assertThrows(UnsupportedOperationException.class, () -> snapshot.add("c"));
    }

    @Test
    void repositoryRejectsNullAndAcceptsSuperTypePredicate() {
        Repository<String> repository = new Repository<>();
        assertThrows(NullPointerException.class, () -> repository.add(null));
        repository.add("x");
        assertEquals(List.of("x"), repository.find(Objects::nonNull));
    }

    // ---- Annotation / reflection / header ----

    @Test
    void annotationIsRuntimeVisibleOnFields() throws Exception {
        Field field = LearnerRecord.class.getDeclaredField("fullName");
        assertEquals("ПІБ", field.getAnnotation(CsvColumn.class).value());
    }

    @Test
    void headerIsDeterministicByFieldName() throws DataStorageException {
        assertEquals(HEADER, CsvExporter.header(LearnerRecord.class));
        assertEquals(4, CsvExporter.annotatedFields(LearnerRecord.class).size());
    }

    @Test
    void exporterWorksWithArbitraryClass() throws Exception {
        class Plain {
            @CsvColumn("b")
            private final String second = "x,y";
            @CsvColumn("a")
            private final int first = 7;
        }
        Path path = Files.createTempFile("plain", ".csv");
        try {
            CsvExporter.write(path, Plain.class, List.of(new Plain()));
            String expected = "a,b" + System.lineSeparator() + "7,\"x,y\"" + System.lineSeparator();
            assertEquals(expected, Files.readString(path, StandardCharsets.UTF_8));
        } finally {
            Files.deleteIfExists(path);
        }
    }

    @Test
    void classWithoutAnnotatedFieldsFails() {
        DataStorageException exception = assertThrows(DataStorageException.class,
                () -> CsvExporter.header(String.class));
        assertTrue(exception.getMessage().contains("CSV-полів"));
    }

    // ---- Escaping ----

    @Test
    void escapeHandlesCommaQuoteAndNewline() {
        assertEquals("plain", CsvExporter.escape("plain"));
        assertEquals("\"a,b\"", CsvExporter.escape("a,b"));
        assertEquals("\"say \"\"hi\"\"\"", CsvExporter.escape("say \"hi\""));
        assertEquals("\"a\nb\"", CsvExporter.escape("a\nb"));
        assertEquals("", CsvExporter.escape(null));
    }

    // ---- Round-trip ----

    @Test
    void roundTripRestoresEqualObjects(@TempDir Path dir) throws DataStorageException {
        Path file = dir.resolve("nested").resolve("learners.csv");
        CsvExporter.write(file, LearnerRecord.class, original);
        assertEquals(original, LearnerRecord.readCsv(file));
    }

    @Test
    void fileContainsHeaderAndEscapedRowInUtf8(@TempDir Path dir) throws Exception {
        Path file = dir.resolve("l.csv");
        CsvExporter.write(file, LearnerRecord.class, original);
        String text = Files.readString(file, StandardCharsets.UTF_8);
        assertTrue(text.startsWith(HEADER));
        assertTrue(text.contains("4.8,\"Коваль, Олена\",КІ-102,3"));
        assertTrue(text.contains("\"Тарас \"\"Кобзар\"\" Шевченко\""));
    }

    @Test
    void writeTruncatesOldContent(@TempDir Path dir) throws DataStorageException {
        Path file = dir.resolve("l.csv");
        CsvExporter.write(file, LearnerRecord.class, original);
        CsvExporter.write(file, LearnerRecord.class, original.subList(0, 1));
        assertEquals(original.subList(0, 1), LearnerRecord.readCsv(file));
    }

    @Test
    void emptyListWritesHeaderOnlyAndReadsEmpty(@TempDir Path dir) throws Exception {
        Path file = dir.resolve("empty.csv");
        CsvExporter.write(file, LearnerRecord.class, List.of());
        assertEquals(HEADER + System.lineSeparator(), Files.readString(file, StandardCharsets.UTF_8));
        assertTrue(LearnerRecord.readCsv(file).isEmpty());
    }

    @Test
    void completelyEmptyFileReadsAsEmpty(@TempDir Path dir) throws Exception {
        Path file = dir.resolve("blank.csv");
        Files.writeString(file, "", StandardCharsets.UTF_8);
        assertTrue(LearnerRecord.readCsv(file).isEmpty());
    }

    @Test
    void crlfLineEndingsAreSupported(@TempDir Path dir) throws Exception {
        Path file = dir.resolve("crlf.csv");
        Files.writeString(file, HEADER + "\r\n4.5,Іван,КІ-101,2\r\n", StandardCharsets.UTF_8);
        assertEquals(List.of(new LearnerRecord("Іван", "КІ-101", 2, 4.5)), LearnerRecord.readCsv(file));
    }

    // ---- Parser ----

    @Test
    void parserKeepsMultilineQuotedFieldAsOneRecord() throws DataStorageException {
        List<List<String>> rows = CsvParser.parseDocument("a,\"x\ny\",c\n1,2,3\n");
        assertEquals(2, rows.size());
        assertEquals(List.of("a", "x\ny", "c"), rows.get(0));
    }

    @Test
    void parserRejectsUnclosedQuotesAndGarbageAfterQuotes() {
        assertThrows(DataStorageException.class, () -> CsvParser.parseDocument("\"abc"));
        assertThrows(DataStorageException.class, () -> CsvParser.parseDocument("\"a\"b"));
        assertThrows(DataStorageException.class, () -> CsvParser.parseDocument(null));
        assertThrows(DataStorageException.class, () -> CsvParser.parse("a\nb"));
    }

    @Test
    void parserKeepsTrailingEmptyField() throws DataStorageException {
        assertEquals(List.of("a", ""), CsvParser.parse("a,"));
    }

    // ---- Errors ----

    @Test
    void wrongFieldCountFails() {
        DataStorageException exception = assertThrows(DataStorageException.class,
                () -> LearnerRecord.fromCsv("4.5,Іван,КІ-101"));
        assertTrue(exception.getMessage().contains("поля"));
    }

    @Test
    void badNumberKeepsCause() {
        DataStorageException exception = assertThrows(DataStorageException.class,
                () -> LearnerRecord.fromCsv("4.5,Іван,КІ-101,abc"));
        assertInstanceOf(NumberFormatException.class, exception.getCause());
    }

    @Test
    void invalidModelValueKeepsCause() {
        DataStorageException exception = assertThrows(DataStorageException.class,
                () -> LearnerRecord.fromCsv("9.5,Іван,КІ-101,2"));
        assertInstanceOf(IllegalArgumentException.class, exception.getCause());
    }

    @Test
    void invalidRowInFileFails(@TempDir Path dir) throws Exception {
        Path file = dir.resolve("bad.csv");
        Files.writeString(file, HEADER + "\n4.5,Іван,КІ-101,2\nbroken\n", StandardCharsets.UTF_8);
        assertThrows(DataStorageException.class, () -> LearnerRecord.readCsv(file));
    }

    @Test
    void missingFileGivesIoCause(@TempDir Path dir) {
        DataStorageException exception = assertThrows(DataStorageException.class,
                () -> LearnerRecord.readCsv(dir.resolve("missing.csv")));
        assertInstanceOf(IOException.class, exception.getCause());
    }

    @Test
    void unwritableParentGivesIoCause(@TempDir Path dir) throws Exception {
        Path blocker = dir.resolve("file");
        Files.writeString(blocker, "x", StandardCharsets.UTF_8);
        DataStorageException exception = assertThrows(DataStorageException.class,
                () -> CsvExporter.write(blocker.resolve("out.csv"), LearnerRecord.class, original));
        assertInstanceOf(IOException.class, exception.getCause());
    }

    @Test
    void modelValidation() {
        assertThrows(IllegalArgumentException.class, () -> new LearnerRecord(" ", "КІ-101", 2, 4.0));
        assertThrows(IllegalArgumentException.class, () -> new LearnerRecord("Іван", null, 2, 4.0));
        assertThrows(IllegalArgumentException.class, () -> new LearnerRecord("Іван", "КІ-101", 0, 4.0));
        assertThrows(IllegalArgumentException.class, () -> new LearnerRecord("Іван", "КІ-101", 7, 4.0));
        assertThrows(IllegalArgumentException.class, () -> new LearnerRecord("Іван", "КІ-101", 2, -1));
        assertThrows(IllegalArgumentException.class, () -> new LearnerRecord("Іван", "КІ-101", 2, Double.NaN));
    }

    @Test
    void equalsAndHashCodeUseAllFields() {
        LearnerRecord a = new LearnerRecord("Іван", "КІ-101", 2, 4.0);
        assertEquals(a, new LearnerRecord(" Іван ", "КІ-101", 2, 4.0));
        assertEquals(a.hashCode(), new LearnerRecord("Іван", "КІ-101", 2, 4.0).hashCode());
        assertTrue(!a.equals(new LearnerRecord("Іван", "КІ-101", 3, 4.0)));
    }

    @Test
    void mainExportRoundTrip(@TempDir Path dir) throws DataStorageException {
        List<Student> students = List.of(
                new Bachelor("Петренко Іван", "КІ-101", 2, 4.5, true),
                new Master("Коваль, Олена", "КН-51", 5, 4.8, false));
        String result = Main.exportCsv(students, dir.resolve("out.csv"));
        assertTrue(result.contains("2 записів"));
        assertTrue(result.contains("успішний"));
    }
}
