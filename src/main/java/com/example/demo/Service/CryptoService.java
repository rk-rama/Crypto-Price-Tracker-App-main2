package com.example.demo.Service;

import com.example.demo.model.CryptoCoin;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.json.JSONObject;
import org.springframework.http.*;
import java.util.*;

@Service
public class CryptoService {

    private final String API_URL = "https://api.coingecko.com/api/v3/simple/price?ids=%s&vs_currencies=usd&include_24hr_change=true";

    public List<CryptoCoin> getCryptoPrices(List<String> coins) {
        List<CryptoCoin> coinList = new ArrayList<>();
        try {
            // IDs clean karein
            String ids = String.join(",", coins).toLowerCase().replaceAll("\\s", "");
            String url = String.format(API_URL, ids);

            RestTemplate restTemplate = new RestTemplate();

            // Render ke liye zaroori Headers
            HttpHeaders headers = new HttpHeaders();
            headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
            // Real browser ki tarah behave karne ke liye ye header mast hai
            headers.add("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36");
            headers.add("Accept-Language", "en-US,en;q=0.9");

            HttpEntity<String> entity = new HttpEntity<>(headers);

            // API Call
            ResponseEntity<String> responseEntity = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
            String response = responseEntity.getBody();

            if (response == null || response.equals("{}")) {
                System.out.println("API Response was empty.");
                return coinList;
            }

            JSONObject json = new JSONObject(response);

            for (String originalCoinName : coins) {
                String coinId = originalCoinName.toLowerCase().trim();
                if (json.has(coinId)) {
                    JSONObject data = json.getJSONObject(coinId);
                    double price = data.optDouble("usd", 0.0);
                    double change = data.optDouble("usd_24h_change", 0.0);

                    coinList.add(new CryptoCoin(coinId, originalCoinName.toUpperCase().trim(), price, change));
                }
            }
        } catch (Exception e) {
            System.err.println("Render Error: " + e.getMessage());
        }
        return coinList;
    }
}