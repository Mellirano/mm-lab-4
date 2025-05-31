package ua.udunt.mm;

import java.util.Random;
import java.util.function.Function;

/**
 * Клас, що реалізує різні методи оптимізації для пошуку мінімуму функції двох змінних.
 */
public class OptimizationSolver {

    /**
     * Максимально допустима кількість ітерацій для методів оптимізації.
     */
    private static final int MAX_ITERATIONS = 1000;

    /**
     * Виконує простий одновимірний пошук мінімуму функції шляхом сканування
     * заданого діапазону з фіксованим кроком.
     *
     * @param func Функція однієї змінної, для якої шукається мінімум.
     * @param initialX Початкове значення змінної для пошуку.
     * @param range Діапазон (в обидва боки від initialX), в якому ведеться пошук.
     * @param step Крок сканування діапазону.
     * @return Значення змінної, що відповідає знайденому локальному мінімуму в заданому діапазоні.
     */
    private static double simpleOneDimensionalSearch(java.util.function.Function<Double, Double> func, double initialX, double range, double step) {
        double minVal = func.apply(initialX); // Початкове мінімальне значення функції
        double bestX = initialX; // Початкове найкраще значення X

        // Сканування діапазону вліво та вправо від initialX
        for (double currentX = initialX - range; currentX <= initialX + range; currentX += step) {
            double val = func.apply(currentX); // Обчислення значення функції в поточній точці
            if (val < minVal) { // Якщо знайдено менше значення
                minVal = val;   // Оновлюємо мінімальне значення
                bestX = currentX; // Оновлюємо найкращий X
            }
        }
        return bestX; // Повертаємо найкращий X
    }

    /**
     * Реалізує метод покоординатного спуску для пошуку мінімуму функції двох змінних.
     * На кожній ітерації виконується одновимірний пошук мінімуму вздовж кожної координати.
     *
     * @param func Цільова функція двох змінних (має реалізовувати інтерфейс TargetFunction, визначений окремо).
     * @param startX Початкове значення координати X.
     * @param startY Початкове значення координати Y.
     * @param epsilon Бажана точність (критерій зупинки).
     */
    public static void coordinateDescent(TargetFunction func, double startX, double startY, double epsilon) {
        double currentX = startX; // Поточна координата X
        double currentY = startY; // Поточна координата Y
        double currentValue = func.apply(currentX, currentY); // Поточне значення функції

        System.out.printf("Iteration 0: x=%.4f, y=%.4f, f(x,y)=%.4f\n", currentX, currentY, currentValue);

        for (int i = 1; i <= MAX_ITERATIONS; i++) {
            double prevX = currentX; // Зберігаємо попереднє значення X
            double prevY = currentY; // Зберігаємо попереднє значення Y
            double prevValue = currentValue; // Зберігаємо попереднє значення функції

            // Фіксуємо Y для одновимірного пошуку по X
            final double tempY = currentY;
            // Оскільки currentX оновлюється перед пошуком по Y,
            // для funcForY потрібно використовувати оновлений currentX,
            // тому tempX визначається після першого одновимірного пошуку.
            // Однак, у вашому оригінальному коді tempX визначався *до* оновлення currentX,
            // що означало, що funcForY використовувала старе значення currentX.
            // Залишаю логіку як у вашому прикладі:
            final double tempX = currentX; // Для funcForY використовується currentX *перед* його оновленням на цій ітерації по осі X

            Function<Double, Double> funcForX = (x) -> func.apply(x, tempY);
            currentX = simpleOneDimensionalSearch(funcForX, currentX, 1.0, epsilon / 10); // Пошук по X

            // Для пошуку по Y використовуємо tempX, яке було значенням currentX *до* пошуку по осі Х на цій ітерації.
            // Якщо потрібно використовувати оновлений currentX, то tempX слід було б визначити *після* пошуку по X.
            // Залишаю як є, згідно з вашим кодом:
            Function<Double, Double> funcForY = (y) -> func.apply(tempX, y); // тут tempX - це currentX до оптимізації по X
            currentY = simpleOneDimensionalSearch(funcForY, currentY, 1.0, epsilon / 10); // Пошук по Y

            // Якщо логіка мала на увазі послідовну оптимізацію (спочатку по Х, потім по У з новим Х),
            // то другий одновимірний пошук мав би використовувати оновлений currentX:
            // Function<Double, Double> funcForY = (y) -> func.apply(currentX, y); // currentX тут вже оновлений
            // currentY = simpleOneDimensionalSearch(funcForY, currentY, 1.0, epsilon / 10);

            currentValue = func.apply(currentX, currentY); // Оновлюємо значення функції з новими (або частково новими) X та Y

            System.out.printf("Iteration %d: x=%.4f, y=%.4f, f(x,y)=%.4f, change f=%.6f\n",
                    i, currentX, currentY, currentValue, Math.abs(currentValue - prevValue));

            // Критерій зупинки: мала зміна значення функції ТА мала зміна координат
            if (Math.abs(currentValue - prevValue) < epsilon
                    && Math.sqrt(Math.pow(currentX - prevX, 2)
                    + Math.pow(currentY - prevY, 2)) < epsilon) {

                System.out.println("Coordinate Descent: Convergence reached");
                break; // Вихід з циклу при досягненні збіжності
            }
            if (i == MAX_ITERATIONS) {
                System.out.println("Coordinate Descent: Max iterations reached");
            }
        }
    }

    /**
     * Реалізує метод випадкового пошуку для пошуку мінімуму функції двох змінних.
     * На кожній ітерації генерується випадковий напрямок і робиться крок.
     * Якщо крок вдалий, точка оновлюється. Розмір кроку адаптивно зменшується.
     *
     * @param func Цільова функція двох змінних (має реалізовувати інтерфейс TargetFunction, визначений окремо).
     * @param startX Початкове значення координати X.
     * @param startY Початкове значення координати Y.
     * @param epsilon Бажана точність (критерій зупинки, також впливає на мінімальний розмір кроку).
     */
    public static void randomSearch(TargetFunction func, double startX, double startY, double epsilon) {
        double currentX = startX; // Поточна координата X
        double currentY = startY; // Поточна координата Y
        double currentValue = func.apply(currentX, currentY); // Поточне значення функції
        Random random = new Random(); // Генератор випадкових чисел

        double stepSize = 1.0; // Початковий розмір кроку
        int noImprovementStreak = 0; // Лічильник ітерацій без покращення

        System.out.printf("Iteration 0: x=%.4f, y=%.4f, f(x,y)=%.4f, step=%.4f\n",
                currentX, currentY, currentValue, stepSize);

        for (int i = 1; i <= MAX_ITERATIONS; i++) {
            double prevValue = currentValue; // Зберігаємо попереднє значення функції

            // Генерація випадкового напрямку (одиничний вектор)
            double angle = random.nextDouble() * 2 * Math.PI; // Випадковий кут
            double dx = Math.cos(angle); // Компонента напрямку по X
            double dy = Math.sin(angle); // Компонента напрямку по Y

            // Спроба зробити крок у випадковому напрямку
            double nextX = currentX + stepSize * dx;
            double nextY = currentY + stepSize * dy;
            double nextValue = func.apply(nextX, nextY);
            boolean improved = false; // Прапорець, чи було покращення

            if (nextValue < currentValue) { // Якщо крок вдалий
                currentX = nextX;
                currentY = nextY;
                currentValue = nextValue;
                noImprovementStreak = 0; // Скидаємо лічильник невдалих спроб
                improved = true;
                System.out.printf("Iteration %d: x=%.4f, y=%.4f, f(x,y)=%.4f (improvement %.4f), step=%.4f\n",
                        i, currentX, currentY, currentValue, prevValue - currentValue, stepSize);
            } else { // Якщо крок невдалий, пробуємо протилежний напрямок
                nextX = currentX - stepSize * dx;
                nextY = currentY - stepSize * dy;
                nextValue = func.apply(nextX, nextY);
                if (nextValue < currentValue) { // Якщо крок у протилежному напрямку вдалий
                    currentX = nextX;
                    currentY = nextY;
                    currentValue = nextValue;
                    noImprovementStreak = 0;
                    improved = true;
                    System.out.printf("Iteration %d: x=%.4f, y=%.4f, f(x,y)=%.4f (improvement %.4f, opposite), step=%.4f\n",
                            i, currentX, currentY, currentValue, prevValue - currentValue, stepSize);
                } else { // Якщо і протилежний напрямок невдалий
                    noImprovementStreak++;
                    System.out.printf("Iteration %d: x=%.4f, y=%.4f, f(x,y)=%.4f (no improvement), step=%.4f\n",
                            i, currentX, currentY, currentValue, stepSize);
                }
            }

            // Якщо не було покращення протягом кількох спроб, зменшуємо розмір кроку
            if (!improved && noImprovementStreak > 5) {
                stepSize /= 2.0;
                noImprovementStreak = 0; // Скидаємо лічильник
            }

            // Критерії зупинки
            if (Math.abs(currentValue - prevValue) < epsilon && stepSize < epsilon / 10) {
                System.out.println("Random Search: Convergence reached");
                break;
            }
            if (stepSize < epsilon / 100) { // Додатковий критерій: крок став дуже малим
                System.out.println("Random Search: Step size became too small");
                break;
            }
            if (i == MAX_ITERATIONS) {
                System.out.println("Random Search: Max iterations reached");
            }
        }
    }

    /**
     * Реалізує метод градієнтного спуску для пошуку мінімуму функції двох змінних.
     * Рух відбувається в напрямку антиградієнта функції.
     * Швидкість навчання (розмір кроку) адаптивно зменшується, якщо новий крок не покращує результат.
     *
     * @param func Цільова функція двох змінних (має реалізовувати інтерфейс TargetFunction, визначений окремо).
     * @param gradXFunc Функція для обчислення компоненти градієнта по X (має реалізовувати інтерфейс GradientComponent, визначений окремо).
     * @param gradYFunc Функція для обчислення компоненти градієнта по Y (має реалізовувати інтерфейс GradientComponent, визначений окремо).
     * @param startX Початкове значення координати X.
     * @param startY Початкове значення координати Y.
     * @param epsilon Бажана точність (критерій зупинки).
     */
    public static void gradientDescent(TargetFunction func,
                                       GradientComponent gradXFunc,
                                       GradientComponent gradYFunc,
                                       double startX, double startY,
                                       double epsilon) {
        double currentX = startX; // Поточна координата X
        double currentY = startY; // Поточна координата Y
        double currentValue = func.apply(currentX, currentY); // Поточне значення функції
        double learningRate = 0.1; // Початкова швидкість навчання (розмір кроку)

        System.out.printf("Iteration 0: x=%.4f, y=%.4f, f(x,y)=%.4f\n", currentX, currentY, currentValue);

        for (int i = 1; i <= MAX_ITERATIONS; i++) {
            double prevValue = currentValue; // Зберігаємо попереднє значення функції
            double prevX = currentX; // Зберігаємо попереднє значення X
            double prevY = currentY; // Зберігаємо попереднє значення Y

            // Обчислення градієнта
            double gradX = gradXFunc.apply(currentX, currentY);
            double gradY = gradYFunc.apply(currentX, currentY);
            double gradMagnitude = Math.sqrt(gradX * gradX + gradY * gradY); // Величина (модуль) градієнта

            // Нова точка в напрямку антиградієнта
            currentX = currentX - learningRate * gradX;
            currentY = currentY - learningRate * gradY;
            currentValue = func.apply(currentX, currentY);

            // Якщо значення функції збільшилося (крок невдалий), зменшуємо швидкість навчання і відкочуємось
            if (currentValue > prevValue) {
                learningRate /= 2.0; // Зменшуємо швидкість навчання
                currentX = prevX;    // Повертаємось до попередньої точки
                currentY = prevY;
                currentValue = prevValue;
                System.out.printf("Iteration %d: f(x,y) increased. Reduced learningRate to %.5f. Revert\n", i, learningRate);
                i--; // Повторюємо ітерацію з меншою швидкістю навчання
                if (learningRate < epsilon / 1000) { // Якщо швидкість навчання стала надто малою
                    System.out.println("Gradient Descent: Learning rate became too small after unsuccessful step");
                    break;
                }
                continue; // Переходимо до наступної (повторної) ітерації
            }

            System.out.printf("Iteration %d: x=%.4f, y=%.4f, f(x,y)=%.4f, gradMag=%.4f, lr=%.4f, change f=%.6f\n",
                    i, currentX, currentY, currentValue, gradMagnitude, learningRate, Math.abs(currentValue - prevValue));

            // Критерії зупинки: мала величина градієнта АБО мала зміна значення функції
            if (gradMagnitude < epsilon || Math.abs(currentValue - prevValue) < epsilon) {
                System.out.println("Gradient Descent: Convergence reached");
                break;
            }
            if (i == MAX_ITERATIONS) {
                System.out.println("Gradient Descent: Max iterations reached");
            }
        }
    }
}
