package app;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import misc.Vector2d;

import java.util.Objects;

/**
 * Класс окружности
 */
public class Circle{
    /**
     * Координаты центра окружности
     */
    public final Vector2d center;

    /**
     * Радиус Окружности
     */
    public final double rad;

    /**
     * Конструктор точки
     *
     * @param center     положение центра окружности
     * @param radius  радиус окружности
     */
    @JsonCreator
    public Circle(@JsonProperty("center") Vector2d center, @JsonProperty("radius") double radius) {
        this.center = center;
        this.rad = radius;
    }

    /**
     * Получить положение
     * (нужен для json)
     *
     * @return положение
     */
    public Vector2d getCenter() {
        return center;
    }

    /**
     * Строковое представление объекта
     *
     * @return строковое представление объекта
     */
    @Override
    public String toString() {
        return "Circle{" +
                "center=" + center +
                ", radius=" + rad +
                '}';
    }

    /**
     * Получить хэш-код объекта
     *
     * @return хэш-код объекта
     */
    @Override
    public int hashCode() {
        return Objects.hash(center, rad);
    }
}
