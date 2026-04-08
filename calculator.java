public class calculator {
    public static double calculate(double left, double right, String operation) {
        switch (operation) {
            case "add":
                return left + right;
            case "subtract":
                return left - right;
            case "multiply":
                return left * right;
            case "divide":
                if (right == 0) {
                    throw new IllegalArgumentException("Division by zero is not allowed.");
                }
                return left / right;
            default:
                throw new IllegalArgumentException("Unsupported operation: " + operation);
        }
    }
}