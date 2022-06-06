package Backend;

import java.util.ArrayList;


public class Admin extends User {
    public Inventory<User> userlist;

    public Admin(String name, double money) {
        super(name, money);
        type = Data.ADMIN;
    }


    public void updateCredit(double amount, User person) {
        person.setCredit(person.getCredit() + amount);
    }

    /**
     * Validate a name
     *
     * @param name Name to be validated
     * @return True if valid, False else
     */
    public static boolean validName(String name) {
        return name.length() <= 16;
    }

    public boolean userExists(String name) {
        for (User user : userlist.getItems()) {
            if (user.getName().equals(name)) {
                return true;
            }
        }
        return false;
    }


    /**
     * Delete a User given a name
     *
     * @param name name of wanted User
     */
    public void deleteUser(String name) {
        User person = userlist.getObj(name);
        userlist.removeItem(person);
    }


    public void refund(User seller, User buyer, Double credit) {
        seller.setCredit(seller.getCredit() - credit);
        buyer.setCredit(Math.min(buyer.getCredit() + credit, 999999.99));
    }

    public void setAuctionPrices(boolean buttonOn) {
        for (User user : userlist.getItems()) {
            for (Game game : user.getInv().getItems()) {
                if (game.isSelling() && game.getDiscountPercent() != 0) {
                    String price;
                    if (buttonOn) {
                        price = String.valueOf(game.getPrice() - (game.getPrice() * game.getDiscountPercent() / 100));
                    } else {
                        price = String.valueOf((game.getPrice() / (100 - game.getDiscountPercent())) * 100);
                    }
                    game.setPrice(Double.parseDouble(price));
                }
            }
        }

    }


    public static void main(String[] args) {
    }
}
