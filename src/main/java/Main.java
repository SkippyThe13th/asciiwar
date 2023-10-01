public class Main {
    public static void main (String[] args) {
        int height = 7;
        int width = 30;
        double landRatio = 0.5;

        Map map = new Map(height, width, landRatio);
        System.out.print(map.printMap());
    }
}