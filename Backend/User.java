package Backend;

import java.util.Objects;

public class User {
    private String name;
    public String type;
    private double credit;
    private final Inventory<Game> inventory = new Inventory<>();
    private double dailyCredit;

    /**
     * User Constructor
     *
     * @param name   - name of the user
     * @param credit - credit amount of the user
     */
    public User(String name, Double credit) {
        this.name = name;
        this.credit = credit;
    }

    /**
     * notify observer if buyer has bought a game
     *
     * @param game - Game object
     */
    public void buy(Game game) {
        this.credit -= game.getPrice();
        Game gameBought = new Game(game.getName(), game.getPrice(), Game.NFS, this.name);
        gameBought.setSelling(false);
        gameBought.setAddedToday(true);
        this.inventory.addItem(gameBought);
    }


    public void sell(String game, Double price, double discountPercent) {
        Game newGame = new Game(game, price, Game.NFS, this.name);
        newGame.setSelling(true);
        newGame.setDiscount(discountPercent);
        newGame.setAddedToday(true);
        this.inventory.addItem(newGame);

    }

    /**
     * getter method of the name of the user
     *
     * @return the name of the user
     */
    public String getName() {
        return this.name;
    }


    /**
     * setter method for the name of the user
     *
     * @param name - new naem of the user
     */
    public void setName(String name) {
        this.name = name;
    }

    public void setCredit(Double credit) {
        this.credit = credit;
    }

    /**
     * getter method for the type of the user
     *
     * @return the type of the user
     */
    public String getType() {
        return this.type;
    }

    /**
     * getter method for the credit of the user
     *
     * @return the amount of credit for the user
     */
    public double getCredit() {
        return credit;
    }

    /**
     * Getter method for the items inside the users inventory
     *
     * @return the arraylist of games inside inventory
     */
    public Inventory<Game> getInv() {
        return inventory;
    }

    public double getDailyCredit() {
        return dailyCredit;
    }

    public void setDailyCredit(double dailyCredit) {
        this.dailyCredit = dailyCredit;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(name, user.name);
    }

    @Override
    public String toString() {
        return this.name;
    }


    public static void main(String[] args) {
    }
}
