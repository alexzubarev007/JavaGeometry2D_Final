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

import java.awt.*;
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
    private ArrayList<Circle> crossed;

    /**
     * Точки в ответе
     */
    @Getter
    @JsonIgnore
    private Vector2d[] pointing;

    /**
     * Задача
     *
     * @param ownCS  СК задачи
     * @param circles массив окружностей
     */
    @JsonCreator
    public Task(
            @JsonProperty("ownCS") CoordinateSystem2d ownCS,
            @JsonProperty("circles") ArrayList<Circle> circles
    ) {
        this.ownCS = ownCS;
        this.circles = circles;
        this.crossed = new ArrayList<>();
        this.clicksCnt = 0;
        this.solved = false;
        this.pointing = new Vector2d[2];
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
            if (solved) {
                paint.setColor(Colors.LINE_COLOR);
                printLine(pointing[0], pointing[1], paint, windowCS, ownCS, canvas);
            }

        }
        canvas.restore();
    }

    /**
     * Рисование окружности
     * @param c окружность
     * @param paint
     * @param windowCS
     * @param ownCS
     * @param canvas
     */

    private static void printCircle(Circle c, Paint paint, CoordinateSystem2i windowCS, CoordinateSystem2d ownCS, io.github.humbleui.skija.Canvas canvas) {
        double delta = Math.acos(1 - (1 / (50 * c.rad)));
        for (double angle = 0; angle <= Math.PI * 2; angle += delta) {
            printLine(new Vector2d(c.center.x + c.rad * Math.cos(angle), c.center.y + c.rad * Math.sin(angle)),
                    new Vector2d(c.center.x + c.rad * Math.cos(angle + delta), c.center.y + c.rad * Math.sin(angle + delta)),
                    paint, windowCS, ownCS, canvas);
        }
    }

    /**
     * Рисование линии
     * @param p1 точка №1
     * @param p2 точка №2
     * @param paint
     * @param windowCS
     * @param ownCS
     * @param canvas
     */

    private static void printLine(Vector2d p1, Vector2d p2, Paint paint, CoordinateSystem2i windowCS, CoordinateSystem2d ownCS, io.github.humbleui.skija.Canvas canvas) {
        // y-координату разворачиваем, потому что у СК окна ось y направлена вниз,
        // а в классическом представлении - вверх
        Vector2i windowPos1 = windowCS.getCoords(p1.x, -p1.y, ownCS);
        Vector2i windowPos2 = windowCS.getCoords(p2.x, -p2.y, ownCS);
        // рисуем линию
        canvas.drawLine(windowPos1.x, windowPos1.y, windowPos2.x, windowPos2.y, paint);
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
        cancel();
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

    /**
     * Очистить задачу
     */
    public void clear() {
        circles.clear();
        crossed.clear();
        pointing = new Vector2d[2];
        solved = false;
    }

    /**
     * Пересекаются ли окржности
     * @param c1 окружность №1
     * @param c2 окружность №2
     */
    private boolean isCrossed (Circle c1, Circle c2) {
        double d = Vector2d.subtract(c1.center, c2.center).length();
        return d < c1.rad + c2.rad && d > Math.abs(c1.rad - c2.rad);
    }

    /**
     * Найти точки пересечения окружностей
     * @param c1 окружность №1
     * @param c2 окружность №2
     */
    private Vector2d[] crossing (Circle c1, Circle c2) {
        Vector2d d = Vector2d.subtract(c2.center, c1.center);
        double dLen = d.length();
        double l = (c1.rad * c1.rad - c2.rad * c2.rad + dLen * dLen) / (2 * dLen);
        Vector2d[] points = {new Vector2d(0, 0), new Vector2d(0, 0)};
        if (d.y == 0) {
            points[0].x = dLen * l / d.x;
            points[1].x = points[0].x;
        } else {
            double a = dLen * dLen;
            double b = -2 * dLen * l * d.x;
            double c = dLen * dLen * l * l - c1.rad * c1.rad * d.y * d.y;
            double Discriminant4 = (b / 2) * (b / 2) - a * c;
            points[0].x = (-b/2 + Math.sqrt(Discriminant4)) / a;
            points[1].x = (-b/2 - Math.sqrt(Discriminant4)) / a;
        }
        points[0].y = (dLen * l - points[0].x * d.x) / d.y;
        points[1].y = (dLen * l - points[1].x * d.x) / d.y;
        points[0].add(c1.center);
        points[1].add(c1.center);
        return points;
    }

    /**
     * Решить задачу
     */
    public void solve() {
        cancel();

        int index1 = -1, index2 = -1;
        double len = 0;
        for (int i = 0; i < circles.size(); ++i) {
            for (int j = i + 1; j < circles.size(); ++j) {
                if (isCrossed(circles.get(i), circles.get(j))) {
                    Vector2d[] now = crossing(circles.get(i), circles.get(j));
                    double nowLen = Vector2d.subtract(now[0], now[1]).length();
                    if (nowLen > len) {
                        len = nowLen;
                        index1 = i;
                        index2 = j;
                        pointing = now;
                    }
                }
            }
        }

        if (index1 != -1 && index2 != -1) {
            crossed.add(circles.get(index1));
            crossed.add(circles.get(index2));
            circles.remove(Math.max(index2, index1));
            circles.remove(Math.min(index2, index1));

            // задача решена
            solved = true;
        } else {
            cancel();
        }
    }
    /**
     * Отмена решения задачи
     */
    public void cancel() {
        if (!crossed.isEmpty()) {
            circles.add(crossed.get(0));
            circles.add(crossed.get(1));
        }
        crossed.clear();
        pointing = new Vector2d[2];
        solved = false;
    }
    /**
     * проверка, решена ли задача
     *
     * @return флаг
     */
    public boolean isSolved() {
        return solved;
    }
}


