public class Main {
    public static void main (String[] args) {
        int jackpot = 100;

        Game game = new Game(Game.LandDensity.NORMAL, jackpot);
        game.addPlayer(new Player("player1"));
        game.addPlayer(new Player("jeff"));
        game.addPlayer(new Player("player3"));
        game.addPlayer(new Player("me"));
        game.addPlayer(new Player("bob"));
        game.startGame();
        System.out.println(game.getMapString());
    }
}