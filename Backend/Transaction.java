package Backend;


import java.util.ArrayList;


public class Transaction {
    final double maxCredit = 999999.99;//max credit
    final double dailyCredit = 1000.00;//max daily credit
    Data data;
    Inventory<User> userList = new Inventory<>();
    public User currentUser = null;
    public boolean auctionOn = false;

    public void initialStartupT() {
        this.data = new Data();
        data.transaction = this;
        data.initialStartup();
    }

    /**
     * Proccess a given transaction
     *
     * @param info Array of information
     */
    public void processTransaction(String[] info) {
        switch (info[0]) {
            case "00":
                loginVerification(info[1], info[2], info[3]);
                break;
            case "01":
                createUser(info[1], info[2], Double.parseDouble(info[3]), null, true, false);
                break;
            case "02":
                deleteUsers(info[1]);
                break;
            case "03":
                processSell(info[2], info[1], info[4], info[3]);
                break;
            case "04":
                processBuy(info[2], info[1], info[3]);
                break;
            case "05":
                processRefund(info[1], info[2], Double.parseDouble(info[3]));
                break;
            case "06":
                addCredit(info[1], Double.parseDouble(info[3]));
                break;
            case "07":
                auctionSale();
                break;
            case "08":
                processRemoveGame(info[2], info[1]);
                break;
            case "09":
                processGift(info[1], info[2], info[3]);
                break;
            case "10":
                logout(info[1], info[2], info[3]);
                break;
        }
    }

    /**
     * Verify if this user can login
     *
     * @param user   user to login
     * @param type   his type
     * @param credit the credit he has
     */
    public void loginVerification(String user, String type, String credit) {
        if (!checkUserNull()) {//check if a user is logged in
            System.out.println("A user is already logged in");
            return;
        }
        if (userList.getObj(user) != null) {//if the user exits, log him in
            currentUser = userList.getObj(user);
            System.out.println("You are now logged in: " + user);
            //Print warnings if his type/credit is wrong
            if (!currentUser.getType().equals(type)) {
                System.out.println("Warning : input accepted but type in daily.txt invalid");
            }
            if (currentUser.getCredit() != Double.parseDouble(credit)) {
                System.out.println("Warning : input accepted but credit in daily.txt invalid");
            }
        } else {
            System.out.println("User does not exist");
        }

    }

    /**
     * Log a user out of the session
     *
     * @param user   user to logout
     * @param type   his account type
     * @param credit his credit balance
     */
    public void logout(String user, String type, String credit) {
        if (checkUserNull()) {// if the current user is null, no one can loggout
            System.out.println("No one is logged in");
            return;
        }
        if (currentUser.getName().equals(user)) {//if the current user is the person that wants to logout
            //print warning if he has invalid info
            if (!currentUser.getType().equals(type)) {
                System.out.println("Warning : input accepted but type in daily.txt invalid");
            }
            if (currentUser.getCredit() != Double.parseDouble(credit)) {
                System.out.println("Warning : input accepted but credit in daily.txt invalid");
            }
            currentUser = null;//log him out
            System.out.println("You are now logged out: " + user);
        } else {//otherwise, an invalid person is trying to log him out, don't let him
            System.out.println("That user is not the current user that is logged in; cannot logout");
        }
    }

    /**
     * Check if the current user is null
     *
     * @return if the current user is null, True if yes, False otherwise
     */
    public boolean checkUserNull() {
        return currentUser == null;
    }

    /**
     * Turn the auction on/off
     *
     * @param auctionOn boolean that tells us wether we turn the auction on/off
     */
    public void setAuctionOn(boolean auctionOn) {
        this.auctionOn = auctionOn;
    }


    /**
     * Return a boolean expression stating whether a transaction is able to be completed or
     * not, true if a transaction can be done, and false otherwise.
     *
     * @param seller - a seller object
     * @param game   - a game object
     * @return True if and only if a transaction can be completed
     */
    public boolean verifyBuy(String seller, String game, String buyer) {
        if (checkUserNull()) {
            System.out.println("No user currently logged in; cannot buy");
            return false;//makes sure user is not null
        }
        if (!currentUser.getName().equals(buyer)) {
            System.out.println("The user trying to buy, is not the one logged in; cannot buy");
            return false;// makes sure the user is the buyer
        }
        if (buyer.equals(seller)) {//you can't buy from yourself
            System.out.println("Can't buy your own game; cannot buy");
        }
        User seller0 = userList.getObj(seller);//get the seller
        if (seller0 == null) {
            System.out.println("User does not exist; cannot buy");
            return false;//makes sure the seller exists
        }
        if (seller0.getType().equals(Data.BUYER)) {
            System.out.println("Seller is not of appropriate type; cannot buy");
            return false;//Make sure seller is a seller and not a buyer
        }
        Game game0 = seller0.getInv().getObj(game);//get the game
        if (game0 == null) {
            System.out.println("Seller is not selling this game; cannot buy");
            return false;//makes sure the game in the seller's inventory exists
        }
        if (currentUser.getInv().getObj(game) != null) {
            System.out.println("User already has this game; cannot buy");
            return false; //checks if buyer already has that game
        }
        if (!game0.getStatus().equals(Game.FS)) {
            System.out.println("Game is not for sale; cannot buy");
            //makes sure the game is able to be bought
            return false;
        }
        if (!game0.isSelling()) {
            System.out.println("Game not available for sale today; cannot buy");
            //checks that the game is available to be bought today
            return false;
        }
        return (game0.getPrice() <= currentUser.getCredit());
        //returns true if the buyer has enough to buy it
    }


    /**
     * process the transaction between the buyer and the seller if and only if
     * the transaction is verified.
     *
     * @param seller person that is selling game
     * @param game   the game being sold
     * @param buyer  the purchaser of teh game
     */
    public void processBuy(String seller, String game, String buyer) {
        if (verifyBuy(seller, game, buyer)) {// if the transaction is verified
            User seller0 = userList.getObj(seller);//Get the seller and the game objects
            Game game0 = seller0.getInv().getObj(game);
            currentUser.buy(game0);//let the current user buy the game
            data.writeBuy(currentUser.getInv().getObj(game0.getName()));//write it to the database
            System.out.println(buyer + " has successfully bought: " + game);
            if (seller0.getCredit() + game0.getPrice() > maxCredit) {//if a seller reached his max credit
                //print a warning and max his balance
                System.out.println("Warning: " + seller0.getName() + " has reached his maximum credit limit.");
                data.updateCreditLogins(seller, maxCredit - seller0.getCredit());//update database
                seller0.setCredit(maxCredit);//set his credit in the object
            } else {//otherwise just update his credit accordingly
                seller0.setCredit(seller0.getCredit() + game0.getPrice());
                data.updateCreditLogins(seller, game0.getPrice());
            }
        }
    }


    public boolean verifySell(String seller, String gameName, String price, String discountPercent) {
        if (checkUserNull()) {
            System.out.println("No user currently logged in; cannot sell");
            return false; //check for null user
        }
        if (!currentUser.getName().equals(seller)) {
            return false; // checks if the current user is the user trying to sell
        }
        if (currentUser.type.equals(Data.BUYER)) {
            return false; //makes sure the current user is not a buyer
        }
        if (Double.parseDouble(discountPercent) > 90.0) {
            System.out.println("Discount is larger than 90%; cannot sell");
            return false;//make sure discount is less than 90%
        }
        if (currentUser.getInv().getObj(gameName) != null) {
            System.out.println("You are already selling this game; cannot sell");
            return false; //makes sure he is not selling an item that he is already selling
        }
        return gameName.length() <= 25 && Double.parseDouble(price) <= 999.99; //checks length and price
    }

    public void processSell(String seller, String gameName, String price, String discountPercent) {
        if (verifySell(seller, gameName, price, discountPercent)) {
            currentUser.sell(gameName, Double.parseDouble(price), Double.parseDouble(discountPercent));
            data.writeSell(currentUser.getInv().getObj(gameName));
            System.out.println(seller + " has successfully put " + gameName + " up for sale for " + price + "$");
        }
    }

    /**
     * delete users from the inventory user list and delete all games related to them
     *
     * @param name - name of the user
     */
    public void deleteUsers(String name) {
        if (checkUserNull()) {
            System.out.println("No user logged in; cannot delete");
            return;
        }

        if (!currentUser.getType().equals(Data.ADMIN)) {
            System.out.println("Not an admin, cannot delete");
            return;
        }
        if (currentUser.getName().equals(name)) {
            System.out.println("Cannot delete yourself");
            return;
        }
        User person = userList.getObj(name);
        if (person == null) {
            System.out.println("User you are trying to delete does no exist");
            return;
        }
        ((Admin) currentUser).deleteUser(name);
        data.removeUser(name);
        System.out.println(person.getName() + " has been successfully deleted from the user list.");

    }

    public void addCredit(String name, double credit) {
        if (checkUserNull()) {//Checks null user
            System.out.println("No user currently logged in; Cannot add credit");
            return;
        }

        if (userList.getObj(name) == null) {//Makes sure the user exists
            System.out.println("User does not exist");
            return;
        }

        if (currentUser.getName().equals(name)) {//check if its a user trying to add to himself
            dailyCreditHelper(credit, name, false);
        } else if (currentUser.getType().equals(Data.ADMIN)) {//check if its a admin trying to add to a user
            dailyCreditHelper(credit, name, true);
        } else {//Print warning saying the current user cant add to anyone else
            System.out.println(currentUser.getName() + " cannot add money to: " + name);
        }

    }

    public void dailyCreditHelper(Double credit, String name, boolean admin) {
        if (currentUser.getDailyCredit() == dailyCredit) {//check if daily credit limit reached
            System.out.println("Daily add credit limit reached");
            return;
        }
        if (currentUser.getDailyCredit() + credit > dailyCredit) {//If the amount that is being added exceeds
            //1000.00, max it out, and say its reached its max
            currentUser.setDailyCredit(dailyCredit);
            System.out.println("Daily add credit limit reached");
        } else {//else, set his daily amount to his current daily credit plus the amount he is adding
            currentUser.setDailyCredit(currentUser.getDailyCredit() + credit);
        }
        //in the last two if/else, we will updated the credit in the object, and in the database
        addCreditHelper(name, credit, admin);
    }


    public void addCreditHelper(String name, Double credit, boolean admin) {
        if (!admin) {// if its not an admin, were gonna call the setCredit() of user
            if (currentUser.getCredit() + credit > maxCredit) {//If the credit added is more than the max
                //allowed balance, we set it to the max, and print out a warning
                currentUser.setCredit(maxCredit);
                data.updateCreditLogins(name, maxCredit - currentUser.getCredit());
                System.out.println("Credit amount exceeds max; your balance is now maxed");
            } else {
                currentUser.setCredit(currentUser.getCredit() + credit);
                data.updateCreditLogins(name, credit);
                System.out.println("Successfully added: " + " $" + credit + "to " + name + ". Current balance now: "
                        + currentUser.getCredit());
            }
        } else {//if its an admin, were gonna use the updateCredit of admin instead
            User user = userList.getObj(name);
            if (currentUser.getCredit() + credit > maxCredit) {
                ((Admin) currentUser).updateCredit(maxCredit - user.getCredit(), user);
                data.updateCreditLogins(name, maxCredit - user.getCredit());
                System.out.println("Credit amount exceeds max; your balance is now maxed");
            } else {
                ((Admin) currentUser).updateCredit(credit, user);
                data.updateCreditLogins(name, credit);
                System.out.println("Successfully added: " + " $" + credit + "to " + name + ". Current balance now: "
                        + user.getCredit());
            }
        }
    }

    public void auctionSaleInitial() {
        for (User user : userList.getItems()) {
            for (Game game : user.getInv().getItems()) {
                if (game.isSelling() && game.getDiscountPercent() != 0) {
                    game.setPrice((game.getPrice() - (game.getPrice() * game.getDiscountPercent() / 100)));
                }
            }
        }
    }


    public void auctionSale() {
        if (!checkUserNull() && currentUser.getType().equals(Data.ADMIN)) {
            if (auctionOn) {
                setAuctionOn(false);
                data.writeAuction("OFF");
                ((Admin) currentUser).setAuctionPrices(false);
                System.out.println("Auction sale now OFF");
            } else {
                setAuctionOn(true);
                data.writeAuction("ON");
                ((Admin) currentUser).setAuctionPrices(true);
                System.out.println("Auction sale now ON");
            }

        }
    }

    /**
     * Return true if a user refund is able to be completed, false otherwise
     *
     * @param buyer  - a buyer object
     * @param seller - a seller object
     * @return True if and only if a transaction can be complete
     */
    public boolean verifyRefund(String buyer, String seller, Double credit) {
        if (checkUserNull()) {
            System.out.println("No user currently logged in; cannot refund");
            return false;
        }
        if (!currentUser.type.equals(Data.ADMIN)) {//make sure an admin is refunding
            System.out.println("Cannot refund, current user not an admin");
            return false;
        }
        User buyer0 = userList.getObj(buyer);
        User seller0 = userList.getObj(seller);
        if (buyer0 == null || seller0 == null) {//make sure these users exist
            System.out.println("Cannot refund, Buyer or Seller does not exists");
            return false;
        }
        if (buyer0.getType().equals(Data.SELLER) || seller0.getType().equals(Data.BUYER)) {
            System.out.println("Wrong type of account in refund; cannot refund");
            return false;//if the seller we are given is a buyer account, he is of wrong typ
            //and vice versa for the buyer
        }
        if (seller0.getCredit() - credit < 0) {//Make sure the seller has enough money to refund
            System.out.println("Seller has insufficient funds, cannot refund");
            return false;
        }
        if (buyer0.getCredit() + credit > maxCredit) {//just print out a statement if the refund
            //maxes out the buyer's balance
            System.out.println(buyer + " max credit reached, balance is now maxed out");
            return true;
        }
        return true;
    }

    /**
     * Return true if a user refund is able to be completed, false otherwise
     *
     * @param buyer  - a buyer name
     * @param seller - a seller name
     */
    public void processRefund(String buyer, String seller, Double credit) {
        if (verifyRefund(buyer, seller, credit)) {
            User seller0 = userList.getObj(seller);//Get the user objects
            User buyer0 = userList.getObj(buyer);
            data.updateCreditLogins(seller, -credit);
            if (buyer0.getCredit() == maxCredit) {//if his balance is maxed, write the max amount to the
                //database
                data.updateCreditLogins(buyer, maxCredit - buyer0.getCredit());
            } else {//else, just add the amount to his balance in the database
                data.updateCreditLogins(buyer, credit);
            }

            ((Admin) currentUser).refund(seller0, buyer0, credit);//update the objects
            System.out.println("Refund successful");
        }
    }

    public void processRemoveGame(String userName, String gameName) {
        if (checkUserNull()) {//check user null
            System.out.println("No user logged in; cannot remove game");
            return;
        }
        if (currentUser.getType().equals(Data.ADMIN)) {
            if (userList.getObj(userName) != null) {
                if (userList.getObj(userName).getInv().getObj(gameName) != null) {
                    Game game = userList.getObj(userName).getInv().getObj(gameName);
                    if (!game.isAddedToday()) {
                        data.removeGameUserLogins(userName, gameName);
                        System.out.println("Admin removed " + gameName + " from " + userName);
                        return;
                    }
                    System.out.println("Game was added today; cannot remove");
                    return;
                }
                System.out.println("User does not have game; cannot remove");
                return;
            }
            System.out.println("User does not exist; cannot remove");
            return;
        } else if (currentUser.getName().equals(userName)) {
            if (userList.getObj(userName) != null) {
                if (userList.getObj(userName).getInv().getObj(gameName) != null) {
                    Game game = userList.getObj(userName).getInv().getObj(gameName);
                    if (!game.isAddedToday()) {
                        data.removeGameUserLogins(userName, gameName);
                        System.out.println(userName + " removed " + gameName + " from " + userName);
                        return;
                    }
                    System.out.println("Game was added today; cannot remove");
                    return;
                }
                System.out.println("User does not have game; cannot remove");
                return;
            }
            System.out.println("User does not exist; cannot remove");
            return;
        }
        System.out.println("User logged in is not the one trying to remove game; cannot remove game");
    }

/*
    public boolean removeGameVerify(String userName, String gameName) {
        if (userList.getObj(userName) == null) {//Make sure the user exists
            System.out.println("User does not exist; cannot remove game");
            return false;
        }


        User user = userList.getObj(userName);
        if (user.getInv().getObj(gameName) == null) {//make sure the game exists
            System.out.println("User does not have that game; cannot remove game");
            return false;
        }
        Game game = user.getInv().getObj(gameName);
        if (game.isAddedToday()) {//make sure the game was not added (bought or sold) today
            System.out.println("Game was added/bought today; cannot remove game");
            return false;
        }
        return true;
    }

    public void removeGame(String userName, String gameName, boolean admin) {
        User user = userList.getObj(userName);
        Game game = user.getInv().getObj(gameName);
        if (admin) {//if it's an admin, use his function
            ((Admin) currentUser).removeUserGame(user, game);
        } else {//otherwise, use the User's function
            currentUser.removeGame(game);
        }
        System.out.println(gameName + " was successfully removed");
        data.removeGameUserLogins(userName, gameName);//remove the game in the database
    }

 */

    public boolean verifyGift(String gameName, String sender, String receiver) {
        if (checkUserNull()) {//check user null
            System.out.println("No user logged in; cannot gift");
            return false;
        }
        User senderUser = userList.getObj(sender);
        if (senderUser == null) {//makes sure user exists
            System.out.println("Sender does not exist; cannot gift");
            return false;
        }

        User receiverUser = userList.getObj(receiver);

        if (receiverUser == null) {//makes sure receiver exists
            System.out.println("Receiver does not exist; cannot gift");
            return false;
        }

        Game game = senderUser.getInv().getObj(gameName);

        if (game == null) {//makes sure sender has game
            System.out.println("Sender does not have game; cannot gift");
            return false;
        }

        if (receiverUser.getInv().getObj(gameName) != null) {//makes sure receiver doesn't
            // already have that game
            System.out.println("Receiver has game already; cannot gift");
            return false;
        }

        if (game.isAddedToday()) {//makes sure game was not bought or put up for sale today
            System.out.println("Game was bought or put up for sale today; cannot gift");
            return false;
        }
        if (currentUser.getType().equals(Data.ADMIN)) {
            return true;//make sure if its an admin, we let him send a gift
        }
        if (!currentUser.getName().equals(sender)) {//checks current user is the sender
            System.out.println("Sender is not the currently logged in user; cannot gift");
            return false;
        }
        return true;
    }

    public void processGift(String gameName, String sender, String receiver) {
        if (verifyGift(gameName, sender, receiver)) {
            User senderUser = userList.getObj(sender);
            Game game = senderUser.getInv().getObj(gameName);
            if (!game.isSelling()) {
                data.removeGameUserLogins(sender, gameName);
            }
            data.addGame(game, receiver, false);
            System.out.println(sender + " successfully gifted " + receiver + " " + gameName);
        }
    }

    public void createUser(String name, String type, Double money, ArrayList<String[]> games, boolean isNew, boolean
            initialStartup) {
        //check if it is and admin creating users, or if this is the initial startup
        if ((checkUserNull() && initialStartup) || (!checkUserNull() && currentUser.getType().equals(Data.ADMIN))) {
            User newUser = null;
            if (Admin.validName(name)) {
                if (type.equals(Data.ADMIN)) {
                    newUser = new Admin(name.strip(), money);
                    ((Admin) newUser).userlist = this.userList;
                }
                if (type.equals(Data.BUYER)) {
                    newUser = new Buyer(name.strip(), money);
                }
                if (type.equals(Data.SELLER)) {
                    newUser = new Seller(name.strip(), money);
                }
                if (type.equals(Data.STANDARD)) {
                    newUser = new StandardUser(name.strip(), money);
                }
            }
            if (newUser != null) {
                if (userList.containsObj(newUser)) {
                    System.out.println("User exists; cannot create");
                    return;
                }
                if (games != null) {
                    for (String[] game : games) {
                        Game g = null;
                        if (game.length == 4) {
                            g = new Game(game[0].strip(), Double.parseDouble(game[1]), game[3], newUser.getName());
                            g.setSelling(game[3].equals(Game.FS));
                            g.setDiscount(Double.parseDouble(game[2]));
                        }
                        if (game.length == 3) {
                            g = new Game(game[0].strip(), Double.parseDouble(game[1]), game[2], newUser.getName());
                            g.setSelling(false);
                        }
                        assert g != null;
                        g.setAddedToday(false);
                        newUser.getInv().addItem(g);
                    }
                }
                newUser.setDailyCredit(0);
                this.userList.addItem(newUser);
                if (isNew) {
                    data.addLogin(newUser);
                }
                System.out.println("Created user: " + newUser.getName());
                return;
            }
        }
        if (checkUserNull()) {
            System.out.println("No user logged in, cannot create user");

        } else {
            System.out.println("Not an admin; cannot create user");
        }
    }

}
