package app;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import io.github.humbleui.skija.Paint;
import lombok.Getter;
import misc.CoordinateSystem2d;
import misc.CoordinateSystem2i;
import misc.Vector2d;
import misc.Vector2i;
import panels.PanelLog;

import java.util.ArrayList;

import static app.Colors.CROSSED_COLOR;
import static app.Colors.CIRCLE_COLOR;

/**
 * Класс задачи
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, property = "@class")
public class Task {
    /**
     * Текст задачи
     */
    public static final String TASK_TEXT = """
            ПОСТАНОВКА ЗАДАЧИ:
            На плоскости задано множество окружностей. Найти
            такую пару пересекающихся окружностей, что длина  
            отрезка, проведенного от одной точки пересечения 
            этих двух окружностей до другой, максимальна.
            """;
    /**
     * Вещественная система координат задачи
     */
    @Getter
    private final CoordinateSystem2d ownCS;
    /**
     * Список окружностей
     */
    @Getter
    private final ArrayList<Circle> circles;
    /**
     * Размер точки
     */
    private static final int POINT_SIZE = 3;
    /**
     * последняя СК окна
     */
    protected CoordinateSystem2i lastWindowCS;

    /**
     * предыдущее нажатие мыши
     */
    private Vector2d prevClick;

    /**
     * счётчик нажатий мыши
     */
    private int clicksCnt;

    /**
     * Флаг, решена ли задача
     */
    private boolean solved;

    /**
     * Окружности в ответе
     */
    @Getter
    @JsonIgnore
    private final ArrayList<Circle> crossed;

    /**
     * Задача
     *
     * @param ownCS  СК задачи
     * @param circles массив окружностей
     */
    @JsonCreator
    public Task(
            @JsonProperty("ownCS") CoordinateSystem2d ownCS,
            @JsonProperty("points") ArrayList<Circle> circles
    ) {
        this.ownCS = ownCS;
        this.circles = circles;
        this.crossed = new ArrayList<>();
        this.clicksCnt = 0;
    }

    /**
     * Рисование задачи
     *
     * @param canvas   область рисования
     * @param windowCS СК окна
     */
    public void paint(io.github.humbleui.skija.Canvas canvas, CoordinateSystem2i windowCS) {
        // Сохраняем последнюю СК
        lastWindowCS = windowCS;
        canvas.save();
        // создаём перо
        try (var paint = new Paint()) {
            paint.setColor(CIRCLE_COLOR);
            for (Circle c : circles) {
                printCircle(c, paint, windowCS, ownCS, canvas);
            }
            paint.setColor(CROSSED_COLOR);
            for (Circle c : crossed) {
                printCircle(c, paint, windowCS, ownCS, canvas);
            }
        }
        canvas.restore();
    }

    protected static void printCircle(Circle c, Paint paint, CoordinateSystem2i windowCS, CoordinateSystem2d ownCS, io.github.humbleui.skija.Canvas canvas) {
        double delta = Math.acos(1 - (1 / (50 * c.rad)));
        for (double angle = 0; angle <= Math.PI * 2; angle += delta) {
            // y-координату разворачиваем, потому что у СК окна ось y направлена вниз,
            // а в классическом представлении - вверх
            Vector2i windowPos1 = windowCS.getCoords(c.center.x + c.rad * Math.cos(angle), -(c.center.y + c.rad * Math.sin(angle)), ownCS);
            Vector2i windowPos2 = windowCS.getCoords(c.center.x + c.rad * Math.cos(angle + delta), -(c.center.y + c.rad * Math.sin(angle + delta)), ownCS);
            // рисуем окружность
            canvas.drawLine(windowPos1.x, windowPos1.y, windowPos2.x, windowPos2.y, paint);
        }
    }

    /**
     * Добавить окружность
     *
     * @param pos      положение центра окружности
     * @param rad      радиус окружности
     */
    public void addCircle(Vector2d pos, double rad) {
        solved = false;
        Circle newCircle = new Circle(pos, rad);
        circles.add(newCircle);
        PanelLog.info("окружность " + newCircle + " добавлена");
    }
    /**
     * Клик мыши по пространству задачи
     *
     * @param pos         положение мыши
     */
    public void click(Vector2i pos) {
        if (lastWindowCS == null) return;
        // получаем положение на экране
        Vector2d taskPos = ownCS.getCoords(pos, lastWindowCS);
        taskPos.y *= -1;
        if (clicksCnt == 0) {
            prevClick = taskPos;
            clicksCnt = 1;
        } else {
            double rad = Vector2d.subtract(taskPos, prevClick).length();
            if (rad != 0)
                addCircle(prevClick, rad);
            clicksCnt = 0;
        }
    }
    /**
     * Добавить случайные окружности
     *
     * @param cnt кол-во случайных окружностей
     */
    public void addRandomCircles(int cnt) {
        // повторяем заданное количество раз
        for (int i = 0; i < cnt; i++) {
            double rad = ownCS.getRandomCoords().x;
            while (rad == 0) rad = ownCS.getRandomCoords().x;
            addCircle(ownCS.getRandomCoords(), Math.abs(rad));
        }
    }
//    /**
//     * Очистить задачу
//     */
//    public void clear() {
//        points.clear();
//        solved = false;
//    }
//    /**
//     * Решить задачу
//     */
//    public void solve() {
//
//
//        // задача решена
//        solved = true;
//    }
//    /**
//     * Отмена решения задачи
//     */
//    public void cancel() {
//        solved = false;
//    }
//    /**
//     * проверка, решена ли задача
//     *
//     * @return флаг
//     */
//    public boolean isSolved() {
//        return solved;
//    }
}


