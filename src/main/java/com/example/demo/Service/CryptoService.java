package com.example.demo.Service;

import com.example.demo.model.CryptoCoin;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.json.JSONObject;
import org.springframework.http.*;
import java.util.*;

@Service
public class CryptoService {

    // CoinGecko API URL
    private final String API_URL = "https://api.coingecko.com/api/v3/simple/price?ids=%s&vs_currencies=usd&include_24hr_change=true";

    public List<CryptoCoin> getCryptoPrices(List<String> coins) {
        List<CryptoCoin> coinList = new ArrayList<>();

        try {
            // 1. IDs ko clean aur lower case karna zaroori hai (e.g. "Bitcoin " -> "bitcoin")
            String ids = String.join(",", coins).toLowerCase().replaceAll("\\s", "");
            String url = String.format(API_URL, ids);

            RestTemplate restTemplate = new RestTemplate();

            // 2. Custom Headers add karna (Render par deployment ke liye zaroori hai)
            HttpHeaders headers = new HttpHeaders();
            headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
            // User-Agent dalne se API block nahi karega
            headers.add("user-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Safari/537.36");

            HttpEntity<String> entity = new HttpEntity<>("parameters", headers);

            // 3. API Call with Headers
            ResponseEntity<String> responseEntity = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
            String response = responseEntity.getBody();

            if (response == null || response.isEmpty() || response.equals("{}")) {
                return coinList; // Khali list return karein agar data na mile
            }

            // 4. JSON Parsing with Safety
            JSONObject json = new JSONObject(response);

            for (String coin : coins) {
                String cleanCoinKey = coin.toLowerCase().trim();

                if (json.has(cleanCoinKey)) {
                    JSONObject coinData = json.getJSONObject(cleanCoinKey);

                    // optDouble use karne se agar value missing ho toh app crash nahi hogi
                    double price = coinData.optDouble("usd", 0.0);
                    double change24h = coinData.optDouble("usd_24h_change", 0.0);

                    // Naya object banakar list mein dalna
                    coinList.add(new CryptoCoin(cleanCoinKey, coin.toUpperCase().trim(), price, change24h));
                }
            }

        } catch (Exception e) {
            // Render ke logs mein error dekhne ke liye
            System.err.println("Error fetching crypto prices: " + e.getMessage());
            e.printStackTrace();
        }

        return coinList;
    }
}