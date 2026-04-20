package com.java.game.service;

import org.springframework.stereotype.Service;

@Service
public class ShopServiceImpl implements ShopService {

    @Override
    public boolean buyItem(String itemName, int price) {

        int userCoin = 1000; // 테스트용

        if (userCoin < price) {
            return false;
        }

        userCoin -= price;
        System.out.println(itemName + " 구매 완료");

        return true;
    }
}