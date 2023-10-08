public class Main {
    public static void main (String[] args) {
        int height = 7;
        int width = 30;
        double landRatio = 0.8;
        int players = 10;

        Map map = new Map(players, landRatio);
        //System.out.print(map.printMap(map.getMap()));
    }
}