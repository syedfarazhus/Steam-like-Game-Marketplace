package Backend;

public class Buyer extends User {

    /**
     * A constructor for the Buyer user that inherits from the User class
     * @param name - name of the Buyer
     * @param money - Buyer's credit
     */
    public Buyer(String name, double money) {
        super(name, money);
        this.type = Data.BUYER;
    }


    @Override
    public void sell(String game, Double price, double discountPercent) {
        System.out.println("Buyer cannot sell, contact admin");
    }


}