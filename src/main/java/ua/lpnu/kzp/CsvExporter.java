package ua.lpnu.kzp;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/** Універсальний рефлексивний експортер полів, позначених {@link CsvColumn}. */
public final class CsvExporter {
    private CsvExporter() {
    }

    /**
     * Повертає анотовані поля класу з детермінованим порядком (за іменем поля).
     *
     * @param type клас записів
     * @return відсортовані поля
     * @throws DataStorageException якщо немає анотованих полів або доступ заборонено
     */
    public static List<Field> annotatedFields(Class<?> type) throws DataStorageException {
        List<Field> fields = Arrays.stream(type.getDeclaredFields())
                .filter(field -> field.isAnnotationPresent(CsvColumn.class))
                .sorted(Comparator.comparing(Field::getName))
                .toList();
        if (fields.isEmpty()) {
            throw new DataStorageException("Клас не має CSV-полів: " + type.getName());
        }
        for (Field field : fields) {
            try {
                if (!field.trySetAccessible()) {
                    throw new DataStorageException(
                            "Немає доступу до поля %s".formatted(field.getName()));
                }
            } catch (SecurityException exception) {
                throw new DataStorageException(
                        "Немає доступу до поля %s".formatted(field.getName()), exception);
            }
        }
        return fields;
    }

    /**
     * Будує рядок заголовка CSV для класу (без символу кінця рядка).
     *
     * @param type клас записів
     * @return заголовок
     * @throws DataStorageException якщо поля недоступні
     */
    public static String header(Class<?> type) throws DataStorageException {
        return annotatedFields(type).stream()
                .map(field -> escape(field.getAnnotation(CsvColumn.class).value()))
                .collect(Collectors.joining(","));
    }

    /**
     * Записує анотовані поля записів у UTF-8 CSV.
     *
     * @param <T> тип записів
     * @param path шлях до CSV-файлу
     * @param type клас записів (потрібен для заголовка порожнього списку)
     * @param records записи для експорту
     * @throws DataStorageException якщо поля неможливо прочитати або файл записати
     */
    public static <T> void write(Path path, Class<T> type, List<? extends T> records)
            throws DataStorageException {
        Objects.requireNonNull(path);
        Objects.requireNonNull(records);
        List<Field> fields = annotatedFields(type);
        StringBuilder csv = new StringBuilder(header(type)).append(System.lineSeparator());
        for (T record : records) {
            List<String> values = new ArrayList<>();
            for (Field field : fields) {
                try {
                    values.add(escape(field.get(record)));
                } catch (IllegalAccessException exception) {
                    throw new DataStorageException(
                            "Не вдалося прочитати поле %s".formatted(field.getName()), exception);
                }
            }
            csv.append(String.join(",", values)).append(System.lineSeparator());
        }
        try {
            Path parent = path.toAbsolutePath().getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            Files.writeString(path, csv.toString(), StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING,
                    StandardOpenOption.WRITE);
        } catch (IOException exception) {
            throw new DataStorageException("Не вдалося записати CSV: %s".formatted(path), exception);
        }
    }

    /** Екранує значення за правилами CSV. */
    static String escape(Object value) {
        String text = Objects.toString(value, "");
        boolean quoted = text.contains(",") || text.contains("\"")
                || text.contains("\r") || text.contains("\n");
        return quoted ? "\"%s\"".formatted(text.replace("\"", "\"\"")) : text;
    }
}
