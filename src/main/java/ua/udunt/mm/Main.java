package ua.udunt.mm;


public class Main {

    public static TargetFunction F = (x, y) -> Math.pow(x, 2) + 2 * Math.pow(y, 2) - 10 * x - 16 * y + 60;
    public static GradientComponent gradFx = (x, y) -> 2 * x - 10;
    public static GradientComponent gradFy = (x, y) -> 4 * y - 16;

    public static void main(String[] args) {
        double startX = 1.0;
        double startY = 1.0;
        double epsilon = 0.001;

        System.out.println("Coordinate Descent Method");
        OptimizationSolver.coordinateDescent(F, startX, startY, epsilon);
        System.out.println("\n----------------------------------------------------\n");

        System.out.println("Random Search Method");
        OptimizationSolver.randomSearch(F, startX, startY, epsilon);
        System.out.println("\n----------------------------------------------------\n");

        System.out.println("Gradient Descent Method");
        OptimizationSolver.gradientDescent(F, gradFx, gradFy, startX, startY, epsilon);
        System.out.println("\n----------------------------------------------------\n");
    }

}