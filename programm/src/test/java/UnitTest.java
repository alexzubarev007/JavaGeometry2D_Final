import app.Circle;
import app.Task;
import misc.CoordinateSystem2d;
import misc.Vector2d;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
 * Класс тестирования
 */
public class UnitTest {

    /**
     * Тест
     *
     * @param circles        список окружностей
     * @param crossed окружности в ответе
     */
    private static void test(ArrayList<Circle> circles, Circle[] crossed) {
        Task task = new Task(new CoordinateSystem2d(10, 10, 20, 20), circles);
        task.solve();

        // проверяем размерности массивов
        assert task.getCrossed().size() == 2 || task.getCrossed().isEmpty();
        assert task.getPointing().length == 2;

        if (!task.getCrossed().isEmpty()) {
            // проверяем, что ответ правильный
            assert crossed[0].toString().equals(task.getCrossed().get(0).toString());
            assert crossed[1].toString().equals(task.getCrossed().get(1).toString());
        }
    }


    /**
     * Первый тест
     */
    @Test
    public void test1() {
        ArrayList<Circle> circles = new ArrayList<>();
        Circle[] crossed = new Circle[2];

        circles.add(new Circle(new Vector2d(0, 0), 5));
        circles.add(new Circle(new Vector2d(-2, -2), 5));

        crossed[0] = (new Circle(new Vector2d(0, 0), 5));
        crossed[1] = (new Circle(new Vector2d(-2, -2), 5));

        test(circles, crossed);
    }

    /**
     * Второй тест
     */
    @Test
    public void test2() {
        ArrayList<Circle> circles = new ArrayList<>();
        Circle[] crossed = new Circle[2];

        circles.add(new Circle(new Vector2d(0, 0), 5));
        circles.add(new Circle(new Vector2d(0, 0), 3));

        test(circles, crossed);
    }

    /**
     * Третий тест
     */
    @Test
    public void test3() {
        ArrayList<Circle> circles = new ArrayList<>();
        Circle[] crossed = new Circle[2];

        circles.add(new Circle(new Vector2d(0, 0), 5));
        circles.add(new Circle(new Vector2d(0, 4), 2));
        circles.add(new Circle(new Vector2d(0, -4), 3));
        circles.add(new Circle(new Vector2d(0, 0), 1));

        crossed[0] = (new Circle(new Vector2d(0, 0), 5));
        crossed[1] = (new Circle(new Vector2d(0, -4), 3));

        test(circles, crossed);
    }
}
