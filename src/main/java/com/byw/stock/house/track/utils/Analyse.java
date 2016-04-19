package com.byw.stock.house.track.utils;

import com.byw.stock.house.track.exception.NegativeNumberException;

public class Analyse {

    final public static int STOCK_HAND_NUMBER = 100;
    //
    final public double houseLine;

    public Analyse(double houseLine) {

        this.houseLine = houseLine;
    }

    /**
     * check is house's transaction or not.
     * 
     * @param hands
     * @param price
     * @return
     * @throws NegativeNumberException
     */
    public int isHouseTransaction(long hands, double price) throws NegativeNumberException {

        //is house or not.
        //
        return (amount(hands, price) > houseLine ? 1 : 0);
    }

    /**
     * calculate amount by hands and price.
     * 
     * @param hands
     * @param price
     * @return
     * @throws NegativeNumberException
     */
    public double amount(long hands, double price) throws NegativeNumberException {

        double amount = hands * STOCK_HAND_NUMBER * price;
        if (amount < 0) {
            throw new NegativeNumberException(hands + "*" + STOCK_HAND_NUMBER + "*" + price);
        }
        return amount;
    }

    public String[] gtimgResponseContentParse(String content) {

        return null;
    }
}
