package ua.lpnu.kzp;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

/**
 * Типобезпечне сховище об'єктів у пам'яті.
 *
 * @param <T> тип записів
 */
public final class Repository<T> {
    private final List<T> items = new ArrayList<>();

    /**
     * Додає непорожній запис до сховища.
     *
     * @param item запис
     */
    public void add(T item) {
        items.add(Objects.requireNonNull(item));
    }

    /**
     * Повертає незмінний знімок усіх записів.
     *
     * @return копія списку записів
     */
    public List<T> all() {
        return List.copyOf(items);
    }

    /**
     * Повертає записи, що задовольняють умову.
     *
     * @param condition умова відбору (приймає {@code T} або його супертип)
     * @return незмінний список знайдених записів
     */
    public List<T> find(Predicate<? super T> condition) {
        return items.stream().filter(condition).toList();
    }
}
