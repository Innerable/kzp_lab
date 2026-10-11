## Лабораторна № 4. Потокова обробка (варіант 6, реєстр студентів)

### Константи набору
- Поріг відбору: `HIGH_AVERAGE_THRESHOLD = 4.5` (бал не нижче порога; значення 4.49 / 4.5 / 4.51 перевіряють межу).
- N у top-N: `TOP_LIMIT = 5` (контрольний набір містить 7 записів).

### П'ять запитів і пошук (клас `StudentReport`)

| Запит | Конвеєр | Результат |
|---|---|---|
| Відбір | `stream().filter(average >= 4.5).toList()` | `List<Student>` |
| Перетворення | `stream().map(Student::getName).toList()` | `List<String>` |
| Групування | `groupingBy(getGroup, TreeMap::new, counting())` | `Map<String, Long>` |
| Статистика | `collect(summarizingDouble(getAverage))` | `DoubleSummaryStatistics` |
| Top-N | `sorted(бал↓, ПІБ↑).limit(N).toList()` | `List<Student>` |
| Пошук | `filter(name.equals).findFirst()` | `Optional<Student>` |

### Компаратор
`comparingDouble(Student::getAverage).reversed().thenComparing(Student::getName)`:
`reversed()` застосовано до ключа балу до `thenComparing`, тому ПІБ сортується за
зростанням. `sorted` іде перед `limit`, тому повертаються найкращі N серед усіх.

### Порожній вхід і відсутній результат
- Списки та карта порожні; статистика: count = 0, sum = 0, min = +∞, max = -∞.
- `Main.summarize` для порожнього списку явно повертає нулі, тому звіт не змінився.
- `findByName` повертає `Optional.empty()`; null-ПІБ відхиляється (`NullPointerException`).

### Цикл → Stream API

| Було (цикл) | Стало (Stream API) | Результат на контрольному наборі |
|---|---|---|
| сума, максимум і кількість балів у циклі | `summarizingDouble` | count 7, avg 4.10, max 4.80 |
| лічильник стипендіатів у циклі | `filter(hasScholarship).count()` | збігається |
| `StringBuilder` у циклі для рейтингів | `map(...).collect(joining())` | текст збігається |

Парсинг рядків (`parseStudents`) залишено циклом свідомо: потрібні номер рядка та
обробка винятків для кожного запису.

### Результати на контрольному наборі (7 записів)
- Відбір: Петренко Петро, Коваль Олена, Іваненко Іван.
- Групування: КН-31=2, КН-32=2, КН-33=1, КН-51=2.
- Top-5: Іваненко Іван, Коваль Олена, Петренко Петро, Шевченко Тарас, Мельник Марія
  (рівні 4.8 розв'язує ПІБ; шостий і сьомий записи усічено).
- Пошук «Іваненко Іван» (дубль): повертається перший запис (КН-31).

### Перевірка
Попередні тести (`StudentTest`, `MainTest`, `StudentHierarchyTest`) та нові
(`StudentReportTest`) проходять разом; `./mvnw verify` зі SpotBugs без попереджень.
