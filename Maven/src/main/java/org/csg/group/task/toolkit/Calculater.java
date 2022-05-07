package org.csg.group.task.toolkit;

/**
 * 逆波兰式计算器
 *
 * @author 独孤猿1998
 */
public class Calculater {

    public static double Calculate(String args) {
        args = nifixTransformPostfix(args);
        return calculatePostfixExpression(args);
    }

    /**
     * 中缀表达式转换为后缀表达式的方法
     *
     * @param nifixExpression 中缀表达式
     * @return 后缀表达式（逆波兰表达式）
     */
    public static String nifixTransformPostfix(String nifixExpression) {
        Stack<Character> operatorStack = new Stack<Character>();//初始化存储操作符的操作符栈
        Stack<String> postfixExpressionStack = new Stack<String>();//初始化存储逆波兰表达式的逆波兰表达式栈
        int length = nifixExpression.length();//中缀表达式的字符串长度
        int index = 0;//用于指向扫描到中缀表达式的位置
        char element = ' ';//保存当前扫描到中缀表达式的字符
        String number = null;//用于存储多位数字
        while (index < length) {//从左至右扫描中缀表达式
            number = "";
            element = nifixExpression.charAt(index);
            if (element >= '0' && element <= '9') {//若读取到的字符为数字
                while ((element >= '0' && element <= '9') || element == '.') {//则向后分析直到数字串的结束
                    number = number + element;
                    index++;
                    if (index == length) {
                        break;
                    }
                    element = nifixExpression.charAt(index);
                }
                postfixExpressionStack.push(number);//并将数字串压入逆波兰表达式栈
                continue;
            } else if (element == '(') {//若读取到的字符为'('
                operatorStack.push(element);//则直接将'('压入操作符栈
            } else if (element == ')') {//若读取到的字符为')'
                while (operatorStack.peek() != '(') {//则将操作符栈中的栈顶操作符依次出栈并压入逆波兰表达式栈，直到遇到左括号'('为止
                    postfixExpressionStack.push(operatorStack.pop().toString());
                }
                operatorStack.pop();//将操作符栈中栈顶的左括号'('出栈
            } else if (element == '+' || element == '-' || element == '*' || element == '/') {//若读取到的字符为运算符'+', '-', '*', '/'
                if (operatorStack.isEmpty() || operatorStack.peek() == '(') {//如果操作符栈为空或栈顶元素为'('
                    operatorStack.push(element);//则将读取到运算符直接压入操作符栈
                } else if (Operator.getOperatorPriority(element) > Operator.getOperatorPriority(operatorStack.peek())) {//如果读取的运算符的优先级高于操作符栈栈顶运算符的优先级
                    operatorStack.push(element);//则将读取的运算符直接压入操作符栈
                } else {//如果读取的运算符的优先级低于或等于操作符栈栈顶运算符的优先级
                    //则将操作符栈栈顶运算符出栈并压入逆波兰表达式栈（注意，此处优先级的比较是不断比较操作符栈栈顶运算符的优先级直到读取到的运算符的优先级高于操作符栈栈顶运算符的优先级或遇到'('或栈空）
                    while (!operatorStack.isEmpty() && operatorStack.peek() != '(' && Operator.getOperatorPriority(element) <= Operator.getOperatorPriority(operatorStack.peek())) {
                        postfixExpressionStack.push(operatorStack.pop().toString());
                    }
                    operatorStack.push(element);//将读取到的运算符压入操作符栈。
                }
            } else {
                throw new RuntimeException("扫描到未知字符！");
            }
            index++;
        }
        while (!operatorStack.isEmpty()) {//中缀表达式扫描完毕，若操作符栈中仍然存在运算符
            postfixExpressionStack.push(operatorStack.pop().toString());//则将操作符栈中栈顶的运算符依次出栈并压入逆波兰表达式栈直到操作符栈为空
        }
        //逆波兰表达式栈的出栈元素的逆序为中缀表达式转换后的后缀表达式(用空格隔开)
        String postfixExpression = postfixExpressionStack.pop();
        while (!postfixExpressionStack.isEmpty()) {
            postfixExpression = postfixExpressionStack.pop() + " " + postfixExpression;
        }
        return postfixExpression;
    }

    /**
     * 计算后缀表达式结果的方法
     *
     * @param postfixExpression 后缀表达式（逆波兰表达式）
     * @return 后缀表达式的计算结果
     */
    public static double calculatePostfixExpression(String postfixExpression) {
        Stack<Double> operandStack = new Stack<>();//初始化一个操作数栈
        String[] elements = postfixExpression.split(" ");//将后缀表达式分解成一个个单元
        String element = null;//后缀表达式扫描到的单元
        char elementHead = ' ';
        double operand = 0, operand1 = 0, operand2 = 0;
        for (int i = 0; i < elements.length; i++) {//从左至右依次扫描后缀表达式的单元
            element = elements[i];
            elementHead = element.charAt(0);
            if ((elementHead >= '0' && elementHead <= '9') || elementHead == '.') {//如果扫描的单元是操作数串
                operand = Double.parseDouble(element);//则将其转换为数字
                operandStack.push(operand);//并压入操作数栈
            } else if (elementHead == '+' || elementHead == '-' || elementHead == '*' || elementHead == '/') {// 如果扫描的单元是一个运算符
                operand1 = operandStack.pop();//取操作数栈栈顶
                operand2 = operandStack.pop();//取操作数栈栈顶
                operand = calculateResult(operand1, operand2, elementHead);//则对操作数栈栈顶上的两个操作数执行该运算
                operandStack.push(operand);//将运算结果重新压入操作数栈
            } else {
                throw new RuntimeException("扫描到未知单元！");
            }
        }
        return operandStack.pop();
    }

    /**
     * 用于计算给定操作数及操作符的结果的方法
     *
     * @param operand1
     * @param operand2
     * @param operator
     * @return 计算结果
     */
    public static double calculateResult(double operand1, double operand2, char operator) {
        double result = 0;
        switch (operator) {
            case '+':
                result = operand2 + operand1;
                break;
            case '-':
                result = operand2 - operand1;
                break;
            case '*':
                result = operand2 * operand1;
                break;
            case '/':
                result = operand2 / operand1;
                break;
            default:
                throw new RuntimeException("未定义运算符：" + operator);
        }
        return result;
    }
}

/**
 * 运算符类，用于比较运算符的优先级
 */
class Operator {

    private static final int ADD = 1;//'+' 的优先级定义
    private static final int SUB = 1;//'-' 的优先级定义
    private static final int MUL = 2;//'*' 的优先级定义
    private static final int DIV = 2;//'/' 的优先级定义

    /**
     * 获取给定运算符的优先级的犯法
     *
     * @param operator 给定的运算符
     * @return 给定运算符的优先级
     */
    public static int getOperatorPriority(char operator) {
        int priority = 0;
        switch (operator) {
            case '+':
                priority = ADD;
                break;
            case '-':
                priority = SUB;
                break;
            case '*':
                priority = MUL;
                break;
            case '/':
                priority = DIV;
                break;
            default:
                throw new RuntimeException("未定义运算符：" + operator);
        }
        return priority;
    }
}

/**
 * 使用数组实现栈类
 */
class Stack<E> {

    private int maxSize;//栈的最大容量
    private Object[] elements;
    private int top;//指向栈顶元素

    public Stack() {
        this(30);
    }

    public Stack(int maxSize) {
        this.maxSize = maxSize;
        elements = new Object[this.maxSize];
        top = -1;
    }

    /**
     * 判断栈是否为空的方法
     *
     * @return 判断是否栈空的结果
     */
    public boolean isEmpty() {
        return top == -1;
    }

    /**
     * 判断栈是否已满的方法
     *
     * @return 判断是否栈满的结果
     */
    public boolean isFull() {
        return top == maxSize - 1;
    }

    /**
     * 元素入栈的方法
     *
     * @param element 即将入栈的元素
     */
    public void push(E element) {
        if (isFull()) {
            System.out.println("栈满，元素无法入栈！");
            return;
        }
        elements[++top] = element;
    }

    /**
     * 取栈顶元素的方法，栈顶元素不出栈
     *
     * @return 栈顶元素
     */
    public E peek() {
        if (isEmpty()) {
            throw new RuntimeException("栈空，无法取栈顶元素！");
        }
        return (E) elements[top];
    }

    /**
     * 栈顶元素出栈的方法
     *
     * @return 栈顶出栈的元素
     */
    public E pop() {
        if (isEmpty()) {
            throw new RuntimeException("栈空，栈顶元素无法出栈！");
        }
        return (E) elements[top--];
    }
}
