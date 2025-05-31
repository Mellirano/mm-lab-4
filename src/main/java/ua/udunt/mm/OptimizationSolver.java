package ua.udunt.mm;

import java.util.Random;
import java.util.function.Function;

public class OptimizationSolver {

    /**
     * Максимально допустима кількість ітерацій для методів оптимізації.
     */
    private static final int MAX_ITERATIONS = 1000;

    /**
     * Виконує простий одновимірний пошук мінімуму функції шляхом сканування
     * заданого діапазону з фіксованим кроком.
     *
     * @param func     Функція однієї змінної, для якої шукається мінімум.
     * @param initialX Початкове значення змінної для пошуку.
     * @param range    Діапазон (в обидва боки від initialX), в якому ведеться пошук.
     * @param step     Крок сканування діапазону.
     * @return Значення змінної, що відповідає знайденому локальному мінімуму в заданому діапазоні.
     */
    private static double simpleOneDimensionalSearch(java.util.function.Function<Double, Double> func, double initialX, double range, double step) {
        double minVal = func.apply(initialX);
        double bestX = initialX;

        for (double currentX = initialX - range; currentX <= initialX + range; currentX += step) {
            double val = func.apply(currentX);
            if (val < minVal) {
                minVal = val;
                bestX = currentX;
            }
        }
        return bestX;
    }

    /**
     * Реалізує метод покоординатного спуску для пошуку мінімуму функції двох змінних.
     * На кожній ітерації виконується одновимірний пошук мінімуму вздовж кожної координати.
     *
     * @param func    Цільова функція двох змінних.
     * @param startX  Початкове значення координати X.
     * @param startY  Початкове значення координати Y.
     * @param epsilon Бажана точність (критерій зупинки).
     */
    public static void coordinateDescent(TargetFunction func, double startX, double startY, double epsilon) {
        double currentX = startX;
        double currentY = startY;
        double currentValue = func.apply(currentX, currentY);

        System.out.printf("Iteration 0: x=%.4f, y=%.4f, f(x,y)=%.4f\n", currentX, currentY, currentValue);

        for (int i = 1; i <= MAX_ITERATIONS; i++) {
            double prevX = currentX;
            double prevY = currentY;
            double prevValue = currentValue;

            final double tempY = currentY;
            final double tempX = currentX;

            Function<Double, Double> funcForX = (x) -> func.apply(x, tempY);
            currentX = simpleOneDimensionalSearch(funcForX, currentX, 1.0, epsilon / 10);

            Function<Double, Double> funcForY = (y) -> func.apply(tempX, y);
            currentY = simpleOneDimensionalSearch(funcForY, currentY, 1.0, epsilon / 10);

            currentValue = func.apply(currentX, currentY);

            System.out.printf("Iteration %d: x=%.4f, y=%.4f, f(x,y)=%.4f, change f=%.6f\n",
                    i, currentX, currentY, currentValue, Math.abs(currentValue - prevValue));

            if (Math.abs(currentValue - prevValue) < epsilon
                    && Math.sqrt(Math.pow(currentX - prevX, 2)
                    + Math.pow(currentY - prevY, 2)) < epsilon) {

                System.out.println("Coordinate Descent: convergence reached");
                break;
            }
            if (i == MAX_ITERATIONS) {
                System.out.println("Coordinate Descent: max iterations reached");
            }
        }
    }

    /**
     * Реалізує метод випадкового пошуку для пошуку мінімуму функції двох змінних.
     * На кожній ітерації генерується випадковий напрямок і робиться крок.
     * Якщо крок вдалий, точка оновлюється. Розмір кроку зменшується.
     *
     * @param func    Цільова функція двох змінних.
     * @param startX  Початкове значення координати X.
     * @param startY  Початкове значення координати Y.
     * @param epsilon Бажана точність (критерій зупинки).
     */
    public static void randomSearch(TargetFunction func, double startX, double startY, double epsilon) {
        double currentX = startX;
        double currentY = startY;
        double currentValue = func.apply(currentX, currentY);
        Random random = new Random();

        double stepSize = 1.0;
        int noImprovementStreak = 0;

        System.out.printf("Iteration 0: x=%.4f, y=%.4f, f(x,y)=%.4f, step=%.4f\n",
                currentX, currentY, currentValue, stepSize);

        for (int i = 1; i <= MAX_ITERATIONS; i++) {
            double prevValue = currentValue;

            double angle = random.nextDouble() * 2 * Math.PI;
            double dx = Math.cos(angle);
            double dy = Math.sin(angle);

            double nextX = currentX + stepSize * dx;
            double nextY = currentY + stepSize * dy;
            double nextValue = func.apply(nextX, nextY);
            boolean improved = false;

            if (nextValue < currentValue) {
                currentX = nextX;
                currentY = nextY;
                currentValue = nextValue;
                noImprovementStreak = 0;
                improved = true;
                System.out.printf("Iteration %d: x=%.4f, y=%.4f, f(x,y)=%.4f (improvement %.4f), step=%.4f\n",
                        i, currentX, currentY, currentValue, prevValue - currentValue, stepSize);
            } else {
                nextX = currentX - stepSize * dx;
                nextY = currentY - stepSize * dy;
                nextValue = func.apply(nextX, nextY);
                if (nextValue < currentValue) {
                    currentX = nextX;
                    currentY = nextY;
                    currentValue = nextValue;
                    noImprovementStreak = 0;
                    improved = true;
                    System.out.printf("Iteration %d: x=%.4f, y=%.4f, f(x,y)=%.4f (improvement %.4f, opposite), step=%.4f\n",
                            i, currentX, currentY, currentValue, prevValue - currentValue, stepSize);
                } else {
                    noImprovementStreak++;
                    System.out.printf("Iteration %d: x=%.4f, y=%.4f, f(x,y)=%.4f (no improvement), step=%.4f\n",
                            i, currentX, currentY, currentValue, stepSize);
                }
            }
            if (!improved && noImprovementStreak > 5) {
                stepSize /= 2.0;
                noImprovementStreak = 0;
            }
            if (Math.abs(currentValue - prevValue) < epsilon && stepSize < epsilon / 10) {
                System.out.println("Random Search: convergence reached");
                break;
            }
            if (stepSize < epsilon / 100) {
                System.out.println("Random Search: step size became too small");
                break;
            }
            if (i == MAX_ITERATIONS) {
                System.out.println("Random Search: max iterations reached");
            }
        }
    }

    /**
     * Реалізує метод градієнтного спуску для пошуку мінімуму функції двох змінних.
     * Рух відбувається в напрямку антиградієнта функції.
     * Швидкість навчання зменшується, якщо новий крок не покращує результат.
     *
     * @param func      Цільова функція двох змінних.
     * @param gradXFunc Функція для обчислення компоненти градієнта по X.
     * @param gradYFunc Функція для обчислення компоненти градієнта по Y.
     * @param startX    Початкове значення координати X.
     * @param startY    Початкове значення координати Y.
     * @param epsilon   Бажана точність (критерій зупинки).
     */
    public static void gradientDescent(TargetFunction func,
                                       GradientComponent gradXFunc,
                                       GradientComponent gradYFunc,
                                       double startX, double startY,
                                       double epsilon) {
        double currentX = startX;
        double currentY = startY;
        double currentValue = func.apply(currentX, currentY);
        double learningRate = 0.1;

        System.out.printf("Iteration 0: x=%.4f, y=%.4f, f(x,y)=%.4f\n", currentX, currentY, currentValue);

        for (int i = 1; i <= MAX_ITERATIONS; i++) {
            double prevValue = currentValue;
            double prevX = currentX;
            double prevY = currentY;

            double gradX = gradXFunc.apply(currentX, currentY);
            double gradY = gradYFunc.apply(currentX, currentY);
            double gradMagnitude = Math.sqrt(gradX * gradX + gradY * gradY);

            currentX = currentX - learningRate * gradX;
            currentY = currentY - learningRate * gradY;
            currentValue = func.apply(currentX, currentY);

            if (currentValue > prevValue) {
                learningRate /= 2.0;
                currentX = prevX;
                currentY = prevY;
                currentValue = prevValue;
                System.out.printf("Iteration %d: f(x,y) increased. Reduced learningRate to %.5f. Revert\n", i, learningRate);
                i--;
                if (learningRate < epsilon / 1000) {
                    System.out.println("Gradient Descent: Learning rate became too small after unsuccessful step");
                    break;
                }
                continue;
            }

            System.out.printf("Iteration %d: x=%.4f, y=%.4f, f(x,y)=%.4f, gradMag=%.4f, lr=%.4f, change f=%.6f\n",
                    i, currentX, currentY, currentValue, gradMagnitude, learningRate, Math.abs(currentValue - prevValue));

            if (gradMagnitude < epsilon || Math.abs(currentValue - prevValue) < epsilon) {
                System.out.println("Gradient Descent: convergence reached");
                break;
            }
            if (i == MAX_ITERATIONS) {
                System.out.println("Gradient Descent: max iterations reached");
            }
        }
    }
}
