package Backend;

import java.util.ArrayList;

public class Inventory<T> {
    private ArrayList<T> items = new ArrayList<>();



    public ArrayList<T> getItems(){
        return this.items;
    }

    /**
     * Adds an object to the inventory
     *
     * @param obj the object to be added to the inventory
     */
    public void addItem(T obj) {
        this.items.add(obj);
    }

    /**
     * REMOVES an object to the inventory
     *
     * @param obj the object to be removed from the inventory
     */
    public void removeItem(T obj) {
        this.items.remove(obj);
    }

    /**
     * Finds and returns the Object in the inventory which matches the name
     * given. if none, returns null
     *
     * @param name String that is used to check through inventory
     */
    public T getObj(String name) {
        for (T obj : this.items) {
            if (obj.toString().equals(name)) {
                return obj;
            }
        }
        return null;
    }

    /**
     * return true if the specified object is in the arraylist and
     * false otherwise.
     *
     * @param obj
     * @return true iff the specified object is in the arraylist
     */
    public boolean containsObj(T obj) {
        return getObj(obj.toString()) != null;
    }

    @Override
    public String toString() {
        return "Inventory{" +
                "items=" + items +
                '}';
    }

    public static void main(String[] args) {
    }

}