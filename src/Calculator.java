import java.util.ArrayList;
import java.util.Scanner;
import java.util.Stack;

public class Calculator {
    private ArrayList<String> history;
    private Scanner scanner;

    public Calculator() {
        this.history = new ArrayList<>();
        this.scanner = new Scanner(System.in);
    }

    public void start() {
        System.out.println("Welcome to the Calculator!");
        System.out.println("(Type 'history' to see past calculations)");

        boolean continueCalculating = true;
        while (continueCalculating) {
            System.out.print("Please enter your arithmetic expression: ");
            String expression = scanner.nextLine().trim();

            if (expression.equalsIgnoreCase("history")) {
                showHistory();
            } else {
                try {
                    double result = evaluateExpression(expression);
                    System.out.println("Result: " + result);

                    String historyEntry = expression + " = " + result;
                    history.add(historyEntry);
                } catch (Exception e) {
                    System.out.println("Error: " + e.getMessage());
                }
            }

            continueCalculating = askUserToContinue();
        }

        System.out.println("Thank you for using the Calculator!");
    }

    private boolean askUserToContinue() {
        while (true) {
            System.out.print("Do you want to continue? (y/n): ");
            String answer = scanner.nextLine().trim().toLowerCase();

            if (answer.equals("y")) {
                return true;
            } else if (answer.equals("n")) {
                return false;
            } else {
                System.out.println("Error: Please enter only 'y' or 'n'.");
            }
        }
    }

    private void showHistory() {
        if (history.isEmpty()) {
            System.out.println("No calculations in history.");
        } else {
            System.out.println("Calculation History:");
            for (int i = 0; i < history.size(); i++) {
                System.out.println((i + 1) + ". " + history.get(i));
            }
        }
    }

    private double evaluateExpression(String expression) {
        expression = expression.replaceAll("\\s+", "");

        // Handle function calls
        expression = processFunctions(expression);

        // Evaluate the expression
        return evaluateWithShuntingYard(expression);
    }

    private String processFunctions(String expression) {
        // Process power function
        expression = processPowerFunction(expression);

        // Process sqrt function
        expression = processSqrtFunction(expression);

        // Process abs function
        expression = processAbsFunction(expression);

        // Process round function
        expression = processRoundFunction(expression);

        return expression;
    }

    private String processPowerFunction(String expression) {
        while (expression.contains("power(")) {
            int startIndex = expression.indexOf("power(");
            int endIndex = findClosingParenthesis(expression, startIndex + 6);

            if (endIndex == -1) {
                throw new IllegalArgumentException("Invalid power function format");
            }

            String functionContent = expression.substring(startIndex + 6, endIndex);
            String[] parameters = functionContent.split(",");

            if (parameters.length != 2) {
                throw new IllegalArgumentException("Power function requires two parameters");
            }

            double base = Double.parseDouble(parameters[0]);
            double exponent = Double.parseDouble(parameters[1]);
            double result = Math.pow(base, exponent);

            expression = expression.substring(0, startIndex) + result + expression.substring(endIndex + 1);
        }

        return expression;
    }

    private String processSqrtFunction(String expression) {
        while (expression.contains("sqrt(")) {
            int startIndex = expression.indexOf("sqrt(");
            int endIndex = findClosingParenthesis(expression, startIndex + 5);

            if (endIndex == -1) {
                throw new IllegalArgumentException("Invalid sqrt function format");
            }

            String functionContent = expression.substring(startIndex + 5, endIndex);
            double number = Double.parseDouble(functionContent);

            if (number < 0) {
                throw new IllegalArgumentException("Cannot calculate square root of a negative number");
            }

            double result = Math.sqrt(number);

            expression = expression.substring(0, startIndex) + result + expression.substring(endIndex + 1);
        }

        return expression;
    }

    private String processAbsFunction(String expression) {
        while (expression.contains("abs(")) {
            int startIndex = expression.indexOf("abs(");
            int endIndex = findClosingParenthesis(expression, startIndex + 4);

            if (endIndex == -1) {
                throw new IllegalArgumentException("Invalid abs function format");
            }

            String functionContent = expression.substring(startIndex + 4, endIndex);
            double number = Double.parseDouble(functionContent);
            double result = Math.abs(number);

            expression = expression.substring(0, startIndex) + result + expression.substring(endIndex + 1);
        }

        return expression;
    }

    private String processRoundFunction(String expression) {
        while (expression.contains("round(")) {
            int startIndex = expression.indexOf("round(");
            int endIndex = findClosingParenthesis(expression, startIndex + 6);

            if (endIndex == -1) {
                throw new IllegalArgumentException("Invalid round function format");
            }

            String functionContent = expression.substring(startIndex + 6, endIndex);
            double number = Double.parseDouble(functionContent);
            long result = Math.round(number);

            expression = expression.substring(0, startIndex) + result + expression.substring(endIndex + 1);
        }

        return expression;
    }

    private int findClosingParenthesis(String expression, int startIndex) {
        int count = 1;
        for (int i = startIndex; i < expression.length(); i++) {
            if (expression.charAt(i) == '(') {
                count++;
            } else if (expression.charAt(i) == ')') {
                count--;
                if (count == 0) {
                    return i;
                }
            }
        }
        return -1;
    }

    private double evaluateWithShuntingYard(String expression) {
        if (expression.isEmpty()) {
            throw new IllegalArgumentException("Empty expression");
        }

        Stack<Double> values = new Stack<>();
        Stack<Character> operators = new Stack<>();

        for (int i = 0; i < expression.length(); i++) {
            char c = expression.charAt(i);

            if (Character.isDigit(c) || c == '.') {
                StringBuilder sb = new StringBuilder();

                while (i < expression.length() && (Character.isDigit(expression.charAt(i)) || expression.charAt(i) == '.')) {
                    sb.append(expression.charAt(i));
                    i++;
                }
                i--;

                values.push(Double.parseDouble(sb.toString()));
            } else if (c == '(') {
                operators.push(c);
            } else if (c == ')') {
                while (!operators.isEmpty() && operators.peek() != '(') {
                    if (values.size() < 2) {
                        throw new IllegalArgumentException("Invalid expression");
                    }
                    double b = values.pop();
                    double a = values.pop();
                    values.push(applyOperation(operators.pop(), b, a));
                }

                if (!operators.isEmpty() && operators.peek() == '(') {
                    operators.pop();
                } else {
                    throw new IllegalArgumentException("Mismatched parentheses");
                }
            } else if (c == '+' || c == '-' || c == '*' || c == '/' || c == '%') {
                // Handle unary minus
                if (c == '-' && (i == 0 || expression.charAt(i - 1) == '(' || expression.charAt(i - 1) == '+' ||
                        expression.charAt(i - 1) == '-' || expression.charAt(i - 1) == '*' ||
                        expression.charAt(i - 1) == '/' || expression.charAt(i - 1) == '%')) {
                    values.push(0.0);
                }

                while (!operators.isEmpty() && hasHigherPrecedence(c, operators.peek())) {
                    if (values.size() < 2) {
                        throw new IllegalArgumentException("Invalid expression");
                    }
                    double b = values.pop();
                    double a = values.pop();
                    values.push(applyOperation(operators.pop(), b, a));
                }

                operators.push(c);
            } else {
                throw new IllegalArgumentException("Invalid character in expression: " + c);
            }
        }

        while (!operators.isEmpty()) {
            if (values.size() < 2) {
                throw new IllegalArgumentException("Invalid expression");
            }
            double b = values.pop();
            double a = values.pop();
            values.push(applyOperation(operators.pop(), b, a));
        }

        if (values.size() != 1 || !operators.isEmpty()) {
            throw new IllegalArgumentException("Invalid expression");
        }

        return values.pop();
    }

    private boolean hasHigherPrecedence(char op1, char op2) {
        if (op2 == '(' || op2 == ')') {
            return false;
        }

        if ((op1 == '*' || op1 == '/' || op1 == '%') && (op2 == '+' || op2 == '-')) {
            return false;
        }

        return true;
    }

    private double applyOperation(char operator, double b, double a) {
        switch (operator) {
            case '+':
                return a + b;
            case '-':
                return a - b;
            case '*':
                return a * b;
            case '/':
                if (b == 0) {
                    throw new ArithmeticException("Division by zero");
                }
                return a / b;
            case '%':
                if (b == 0) {
                    throw new ArithmeticException("Modulo by zero");
                }
                return a % b;
        }
        throw new IllegalArgumentException("Unknown operator: " + operator);
    }

    public static void main(String[] args) {
        Calculator calculator = new Calculator();
        calculator.start();
    }
}