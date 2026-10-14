## Лабораторна № 5. Параметризоване сховище, винятки й CSV-експорт (варіант 6, реєстр студентів)

### Варіант і модель
- Варіант: **6 — Реєстр студентів** (не змінюється протягом циклу).
- Клас запису: `LearnerRecord`; поля: `fullName: String`, `group: String`, `studyYear: int`, `average: double`.
- Попередня модель (лаб. 3-4): `Student` → `Bachelor`/`Master`, enum `StudyStatus`; `LearnerRecord::from(Student)` робить плоский запис для експорту.
- Ключ round-trip: `fullName + group`; `equals`/`hashCode` порівнюють усі чотири поля.
- Формат звіту: REPORTn.md у `reports/`.

### Компоненти

| Клас | Призначення |
|---|---|
| `Repository<T>` | сховище без raw types: `add` (відхиляє null), `all()` (незмінний знімок), `find(Predicate<? super T>)` |
| `@CsvColumn` | `@Retention(RUNTIME)`, `@Target(FIELD)`, `String value()` — заголовок стовпця |
| `CsvExporter` | універсальний експорт через рефлексію: `annotatedFields`, `header`, `write(path, type, records)`, `escape` |
| `CsvParser` | документний парсер зі станами `quoted`/`quoteClosed`; кома й перенесення в лапках — дані |
| `DataStorageException` | checked-виняток із конструктором `(message, cause)` |
| `LearnerRecord` | валідація моделі, `fromCsv`, `fromFields`, `readCsv(Path)` |

Експортер не містить назв предметних класів і `if` для підтипів: поля беруться з
`getDeclaredFields()`, лишаються з `@CsvColumn` і сортуються за іменем Java-поля.
Тест `exporterWorksWithArbitraryClass` експортує локальний клас без зв'язку з варіантом.
Запис і читання виконуються через `Path`/`Files` з `StandardCharsets.UTF_8`;
запис із `CREATE`, `TRUNCATE_EXISTING`, `WRITE` (старі рядки не лишаються).

### Анотовані поля й порядок стовпців

Порядок визначає сортування імен полів, а не порядок оголошення:

| Поле Java | `@CsvColumn` | № |
|---|---|---|
| `average` | середній бал | 1 |
| `fullName` | ПІБ | 2 |
| `group` | група | 3 |
| `studyYear` | курс | 4 |

Фактичний заголовок: `середній бал,ПІБ,група,курс`

### Приклад CSV (з екранованим рядком)

```csv
середній бал,ПІБ,група,курс
4.5,Петренко Іван,КІ-101,2
4.8,"Коваль, Олена",КІ-102,3
3.9,"Тарас ""Кобзар"" Шевченко",КІ-101,1
4.0,"Марія
Мельник",КІ-103,4
```

Екранування: поле з комою, лапкою, `\r` або `\n` береться в лапки, лапка
всередині подвоюється. `split(",")` для читання непридатний — він розрізав би
`"Коваль, Олена"` на два поля й не зрозумів би multiline-поле.

### Round-trip

```
LearnerRecord[] original → CsvExporter.write → Files.readString(UTF-8)
              → CsvParser.parseDocument → skip(1) → fromFields → equals(original)
```

Результат (тест `roundTripRestoresEqualObjects`, 5 записів, серед них кома, лапки,
перенесення рядка й `0.0`): початковий і відновлений списки **рівні**.
Запуск програми: `java -jar target/lab01-1.0.0.jar --export out/learners.csv` →
`Експорт CSV: 3 записів, round-trip успішний`.

### Таблиця помилок

| Рівень | Приклад | Реакція | Тест |
|---|---|---|---|
| Модель | порожнє ПІБ, курс 0 або 7, бал < 0, NaN | `IllegalArgumentException` у конструкторі | `modelValidation` |
| Формат | `4.5,Іван,КІ-101` (3 поля замість 4) | `DataStorageException` | `wrongFieldCountFails` |
| Формат | незакриті лапки, символ після закритих лапок | `DataStorageException` | `parserRejectsUnclosedQuotes…` |
| Значення | курс `abc` | `DataStorageException`, cause = `NumberFormatException` | `badNumberKeepsCause` |
| Значення | бал `9.5` | `DataStorageException`, cause = `IllegalArgumentException` | `invalidModelValueKeepsCause` |
| Файл | файл відсутній | `DataStorageException`, cause = `IOException` | `missingFileGivesIoCause` |
| Файл | батьківська «тека» є файлом | `DataStorageException`, cause = `IOException` | `unwritableParentGivesIoCause` |
| Рефлексія | немає анотованих полів / доступ заборонено | `DataStorageException` | `classWithoutAnnotatedFieldsFails` |

`catch` ніде не порожній: виняток або перетворюється на `DataStorageException` з
`cause`, або обробляється в `Main` (повідомлення «Помилка експорту»).

### Крайові випадки
- Порожній список: записується лише заголовок, читання повертає порожній список (`emptyListWritesHeaderOnlyAndReadsEmpty`).
- Повністю порожній файл → порожній список; файл лише із заголовком → порожній список.
- Роздільники рядків: `\n`, `\r\n` (`crlfLineEndingsAreSupported`); експорт використовує `System.lineSeparator()`, парсер приймає обидва.
- UTF-8: кирилиця в заголовку й даних читається однаково на всіх ОС, бо кодування явне.

### Цикл і сумісність
Використання в `Main`: прапорець `--export <файл>` читає реєстр, будує
`Repository<LearnerRecord>`, експортує, читає назад і друкує результат round-trip.
Без прапорця звіт і попередня поведінка не змінюються.

### Перевірка

| Перевірка | Результат |
|---|---|
| Попередні тести (`StudentTest`, `MainTest`, `StudentHierarchyTest`, `StudentReportTest`) | 23 + 5 + 24 + 15 = 67, усі пройшли |
| Нові тести (`CsvExportTest`) | 26, усі пройшли |
| Разом | 93, усі пройшли |
| `./mvnw verify` зі SpotBugs | без попереджень |
| `./mvnw package` | створено `target/lab01-1.0.0.jar`, `--export` працює |
| CI (Ubuntu, Windows, macOS) | _вставте посилання на зелений запуск_ |
| JAR-артефакт | _вставте посилання на артефакт_ |

### Використання ШІ
Інструмент: Claude Code. Роль розробника: реалізація компонентів за методичними
вказівками; роль валідатора: добір крайових випадків для тестів. Прийнято:
структуру класів і тести. Власний внесок: вибір ключа round-trip, діапазони
валідації моделі, перевірка кожного анотованого поля й `cause` винятків.
