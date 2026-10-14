package ua.lpnu.kzp;

import java.util.ArrayList;
import java.util.List;

/** Розбирає CSV-текст з урахуванням лапок і multiline-полів. */
public final class CsvParser {
    private CsvParser() {
    }

    /**
     * Розбирає один CSV-рядок.
     *
     * @param line рядок без перенесення в кінці
     * @return поля рядка
     * @throws DataStorageException якщо формат хибний або рядків не один
     */
    public static List<String> parse(String line) throws DataStorageException {
        List<List<String>> records = parseDocument(line);
        if (records.size() != 1) {
            throw new DataStorageException("Очікується один CSV-рядок");
        }
        return records.get(0);
    }

    /**
     * Розбирає CSV-документ із quoted multiline-полями.
     *
     * <p>Стан {@code quoted} відрізняє роздільники даних від роздільників структури.
     *
     * @param text увесь текст CSV
     * @return список записів (не фізичних рядків файлу)
     * @throws DataStorageException якщо лапки не закриті або текст хибний
     */
    public static List<List<String>> parseDocument(String text) throws DataStorageException {
        if (text == null) {
            throw new DataStorageException("CSV-документ не може бути null");
        }
        if (text.isEmpty()) {
            return List.of();
        }
        List<List<String>> records = new ArrayList<>();
        List<String> fields = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean quoted = false;
        boolean quoteClosed = false;
        for (int index = 0; index < text.length(); index++) {
            char symbol = text.charAt(index);
            if (symbol == '"' && !quoteClosed && !quoted && current.length() == 0) {
                quoted = true;
            } else if (symbol == '"' && quoted
                    && index + 1 < text.length() && text.charAt(index + 1) == '"') {
                current.append('"');
                index++;
            } else if (symbol == '"' && quoted) {
                quoted = false;
                quoteClosed = true;
            } else if (symbol == ',' && !quoted) {
                fields.add(current.toString());
                current.setLength(0);
                quoteClosed = false;
            } else if ((symbol == '\r' || symbol == '\n') && !quoted) {
                fields.add(current.toString());
                records.add(List.copyOf(fields));
                fields = new ArrayList<>();
                current.setLength(0);
                quoteClosed = false;
                if (symbol == '\r' && index + 1 < text.length() && text.charAt(index + 1) == '\n') {
                    index++;
                }
            } else {
                if (quoteClosed) {
                    throw new DataStorageException("Символ після закритих CSV-лапок");
                }
                current.append(symbol);
            }
        }
        if (quoted) {
            throw new DataStorageException("Незакриті CSV-лапки");
        }
        if (!fields.isEmpty() || current.length() > 0 || quoteClosed) {
            fields.add(current.toString());
            records.add(List.copyOf(fields));
        }
        return List.copyOf(records);
    }
}
