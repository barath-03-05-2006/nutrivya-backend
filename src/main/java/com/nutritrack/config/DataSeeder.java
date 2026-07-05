package com.nutritrack.config;
import com.nutritrack.entity.*; import com.nutritrack.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner; import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder; import org.springframework.stereotype.Component;
import java.util.List;

@Component @Order(1)
public class DataSeeder implements CommandLineRunner {
    @Autowired private UserRepository userRepo;
    @Autowired private PasswordEncoder encoder;
    @Autowired private FoodDatabaseRepository foodRepo;

    // Pulled from application.properties, which itself pulls from environment
    // variables — no real credentials are ever hardcoded in source code.
    @Value("${app.seed.admin.email}")    private String seedAdminEmail;
    @Value("${app.seed.admin.username}") private String seedAdminUsername;
    @Value("${app.seed.admin.password}") private String seedAdminPassword;
    @Value("${app.seed.admin.fullname}") private String seedAdminFullName;

    @Override public void run(String... args){
        if(!userRepo.existsByEmail(seedAdminEmail) && !userRepo.existsByUsername(seedAdminUsername)){
            User d=new User(); d.setUsername(seedAdminUsername); d.setEmail(seedAdminEmail);
            d.setPassword(encoder.encode(seedAdminPassword)); d.setFullName(seedAdminFullName);
            d.setRole(User.Role.DIETITIAN); userRepo.save(d);
            System.out.println("✅ Dietitian created: " + seedAdminEmail + " (password set via SEED_ADMIN_PASSWORD env var)");
        }
        if(foodRepo.count()==0){ seedFoods(); System.out.println("✅ Food database seeded (190+ items)"); }
    }

    /**
     * Each row: {name, category, servingSize, servingUnit, kcal/100g, protein/100g, carbs/100g, fat/100g, fiber/100g, unitGramsOverride}
     * unitGramsOverride format: "piece:50,cup:240,bowl:150,katori:150,tbsp:15,tsp:5" — grams per non-g/ml unit, specific to that food.
     * If null, frontend uses sensible defaults (piece:50, cup:240, bowl:150, katori:150, tbsp:15, tsp:5, g:1, ml:1).
     */
    private void seedFoods(){
        List<Object[]> foods = List.of(
            // ── GRAINS & CEREALS ─────────────────────────────────────────
            new Object[]{"White Bread","Grains","30","g",265.0,9.0,49.0,3.2,2.7,"piece:28"},
            new Object[]{"Wheat Bread","Grains","30","g",247.0,13.0,41.0,3.4,6.0,"piece:28"},
            new Object[]{"Brown Bread","Grains","30","g",252.0,12.0,43.0,3.5,7.0,"piece:28"},
            new Object[]{"Rice (cooked)","Grains","100","g",130.0,2.7,28.0,0.3,0.4,"cup:195,bowl:150,katori:150"},
            new Object[]{"Brown Rice (cooked)","Grains","100","g",123.0,2.7,26.0,1.0,1.8,"cup:195,bowl:150,katori:150"},
            new Object[]{"Basmati Rice (cooked)","Grains","100","g",121.0,2.5,27.0,0.2,0.3,"cup:195,bowl:150,katori:150"},
            new Object[]{"Oats","Grains","40","g",389.0,17.0,66.0,7.0,10.6,"cup:90,bowl:80,katori:80"},
            new Object[]{"Roti (wheat)","Grains","35","g",297.0,11.0,58.0,2.5,5.0,"piece:35"},
            new Object[]{"Chapati","Grains","35","g",290.0,10.0,55.0,2.0,4.5,"piece:35"},
            new Object[]{"Phulka","Grains","25","g",260.0,9.0,52.0,1.5,4.0,"piece:25"},
            new Object[]{"Paratha (plain)","Grains","60","g",330.0,7.0,40.0,16.0,3.0,"piece:60"},
            new Object[]{"Aloo Paratha","Grains","100","g",265.0,6.0,35.0,11.0,3.0,"piece:100"},
            new Object[]{"Naan","Grains","90","g",310.0,9.0,50.0,8.0,2.5,"piece:90"},
            new Object[]{"Idli","Grains","50","g",78.0,4.0,16.0,0.2,1.0,"piece:50"},
            new Object[]{"Dosa (plain)","Grains","75","g",168.0,3.9,23.0,6.4,1.0,"piece:75"},
            new Object[]{"Masala Dosa","Grains","150","g",195.0,4.5,28.0,7.5,1.5,"piece:150"},
            new Object[]{"Uttapam","Grains","100","g",150.0,4.0,22.0,5.0,1.2,"piece:100"},
            new Object[]{"Poha","Grains","100","g",130.0,2.5,25.0,2.5,0.9,"bowl:150,katori:150"},
            new Object[]{"Upma","Grains","100","g",132.0,3.0,24.0,3.5,1.5,"bowl:150,katori:150"},
            new Object[]{"Vermicelli (cooked)","Grains","100","g",138.0,3.0,28.0,1.0,1.0,"bowl:150,katori:150"},
            new Object[]{"Puri","Grains","20","g",330.0,6.0,38.0,18.0,1.5,"piece:20"},
            new Object[]{"Khichdi","Grains","100","g",120.0,4.0,20.0,2.5,1.5,"bowl:200,katori:200"},
            new Object[]{"Pongal","Grains","100","g",140.0,3.5,22.0,4.0,1.0,"bowl:180,katori:180"},
            new Object[]{"Quinoa (cooked)","Grains","100","g",120.0,4.4,21.0,1.9,2.8,"cup:185,bowl:150"},
            new Object[]{"Vermicelli Upma","Grains","100","g",140.0,3.0,25.0,3.0,1.2,"bowl:150,katori:150"},
            new Object[]{"Bisi Bele Bath","Grains","150","g",145.0,4.5,22.0,4.0,2.0,"bowl:200,katori:200"},

            // ── PROTEIN (Meat, Egg, Soy) ──────────────────────────────────
            new Object[]{"Chicken Breast","Protein","100","g",165.0,31.0,0.0,3.6,0.0,null},
            new Object[]{"Chicken Thigh","Protein","100","g",209.0,26.0,0.0,11.0,0.0,null},
            new Object[]{"Chicken Curry","Protein","150","g",180.0,18.0,5.0,9.0,1.0,"bowl:200,katori:200"},
            new Object[]{"Mutton Curry","Protein","150","g",230.0,20.0,4.0,14.0,1.0,"bowl:200,katori:200"},
            new Object[]{"Fish Curry","Protein","150","g",150.0,18.0,4.0,7.0,0.5,"bowl:200,katori:200"},
            new Object[]{"Egg (whole)","Protein","50","g",155.0,13.0,1.1,11.0,0.0,"piece:50"},
            new Object[]{"Egg White","Protein","30","g",52.0,11.0,0.7,0.2,0.0,"piece:30"},
            new Object[]{"Egg Bhurji","Protein","100","g",185.0,12.0,3.0,14.0,0.5,"bowl:120,katori:120"},
            new Object[]{"Boiled Egg","Protein","50","g",155.0,13.0,1.1,11.0,0.0,"piece:50"},
            new Object[]{"Paneer","Protein","100","g",265.0,18.0,3.4,20.0,0.0,null},
            new Object[]{"Paneer Tikka","Protein","100","g",240.0,16.0,6.0,17.0,1.0,"piece:30"},
            new Object[]{"Paneer Butter Masala","Protein","150","g",250.0,11.0,10.0,18.0,1.5,"bowl:200,katori:200"},
            new Object[]{"Tofu","Protein","100","g",76.0,8.0,1.9,4.8,0.3,null},
            new Object[]{"Soya Chunks (cooked)","Protein","100","g",345.0,52.0,33.0,0.5,13.0,"bowl:150,katori:150"},
            new Object[]{"Prawns","Protein","100","g",99.0,24.0,0.2,0.3,0.0,null},

            // ── LEGUMES & DALS ─────────────────────────────────────────────
            new Object[]{"Toor Dal (cooked)","Legumes","100","g",116.0,7.0,20.0,0.4,5.0,"bowl:150,katori:150"},
            new Object[]{"Moong Dal (cooked)","Legumes","100","g",105.0,7.5,19.0,0.4,5.0,"bowl:150,katori:150"},
            new Object[]{"Chana Dal (cooked)","Legumes","100","g",164.0,9.0,27.0,2.6,7.5,"bowl:150,katori:150"},
            new Object[]{"Masoor Dal (cooked)","Legumes","100","g",116.0,9.0,20.0,0.4,7.9,"bowl:150,katori:150"},
            new Object[]{"Urad Dal (cooked)","Legumes","100","g",120.0,9.0,21.0,0.4,8.0,"bowl:150,katori:150"},
            new Object[]{"Rajma Curry","Legumes","100","g",127.0,8.7,22.8,0.5,6.4,"bowl:150,katori:150"},
            new Object[]{"Chole (Chickpea Curry)","Legumes","100","g",164.0,8.9,27.0,2.6,7.6,"bowl:150,katori:150"},
            new Object[]{"Sambar","Legumes","150","g",70.0,3.5,11.0,1.5,2.5,"bowl:200,katori:200"},
            new Object[]{"Dal Tadka","Legumes","150","g",110.0,6.5,16.0,2.5,4.0,"bowl:200,katori:200"},
            new Object[]{"Lobia Curry","Legumes","100","g",110.0,7.5,19.0,0.6,6.5,"bowl:150,katori:150"},
            new Object[]{"Sprouted Moong","Legumes","100","g",30.0,3.0,5.9,0.2,1.8,"bowl:100,katori:100"},

            // ── DAIRY ───────────────────────────────────────────────────────
            new Object[]{"Milk (full fat)","Dairy","200","ml",61.0,3.2,4.8,3.3,0.0,"cup:240"},
            new Object[]{"Milk (low fat)","Dairy","200","ml",42.0,3.4,5.0,1.0,0.0,"cup:240"},
            new Object[]{"Buttermilk","Dairy","200","ml",40.0,3.0,4.8,1.0,0.0,"cup:240,katori:150"},
            new Object[]{"Curd (yogurt)","Dairy","100","g",98.0,11.0,3.4,4.3,0.0,"bowl:150,katori:150,cup:200"},
            new Object[]{"Greek Yogurt","Dairy","100","g",59.0,10.0,3.6,0.4,0.0,"bowl:150,katori:150,cup:200"},
            new Object[]{"Lassi (sweet)","Dairy","250","ml",90.0,3.0,15.0,2.0,0.0,"cup:250"},
            new Object[]{"Cheese (cheddar)","Dairy","30","g",402.0,25.0,1.3,33.0,0.0,"piece:20"},
            new Object[]{"Paneer Cubes","Dairy","50","g",265.0,18.0,3.4,20.0,0.0,"piece:10"},

            // ── FRUITS ───────────────────────────────────────────────────────
            new Object[]{"Banana","Fruits","120","g",89.0,1.1,23.0,0.3,2.6,"piece:120"},
            new Object[]{"Apple","Fruits","150","g",52.0,0.3,14.0,0.2,2.4,"piece:150"},
            new Object[]{"Orange","Fruits","130","g",47.0,0.9,12.0,0.1,2.4,"piece:130"},
            new Object[]{"Mango","Fruits","100","g",60.0,0.8,15.0,0.4,1.6,"piece:200,cup:165"},
            new Object[]{"Papaya","Fruits","150","g",43.0,0.5,11.0,0.3,1.7,"bowl:150,cup:140"},
            new Object[]{"Watermelon","Fruits","200","g",30.0,0.6,7.6,0.2,0.4,"bowl:200,cup:150"},
            new Object[]{"Pomegranate","Fruits","100","g",83.0,1.7,19.0,1.2,4.0,"bowl:100,cup:170"},
            new Object[]{"Grapes","Fruits","100","g",69.0,0.7,18.0,0.2,0.9,"bowl:100,cup:150"},
            new Object[]{"Guava","Fruits","100","g",68.0,2.6,14.0,1.0,5.4,"piece:100"},
            new Object[]{"Pineapple","Fruits","100","g",50.0,0.5,13.0,0.1,1.4,"bowl:100,cup:165"},
            new Object[]{"Pear","Fruits","150","g",57.0,0.4,15.0,0.1,3.1,"piece:150"},
            new Object[]{"Strawberry","Fruits","100","g",32.0,0.7,7.7,0.3,2.0,"bowl:100,cup:150"},
            new Object[]{"Kiwi","Fruits","75","g",61.0,1.1,15.0,0.5,3.0,"piece:75"},
            new Object[]{"Dates","Fruits","20","g",277.0,1.8,75.0,0.2,6.7,"piece:8"},
            new Object[]{"Raisins","Fruits","15","g",299.0,3.1,79.0,0.5,3.7,"tbsp:9"},

            // ── VEGETABLES ───────────────────────────────────────────────────
            new Object[]{"Spinach (cooked)","Vegetables","100","g",23.0,2.9,3.6,0.4,2.2,"bowl:100,katori:100"},
            new Object[]{"Palak Paneer","Vegetables","150","g",170.0,8.0,8.0,11.0,2.5,"bowl:200,katori:200"},
            new Object[]{"Broccoli","Vegetables","100","g",34.0,2.8,7.0,0.4,2.6,"bowl:100,katori:100"},
            new Object[]{"Carrot","Vegetables","100","g",41.0,0.9,10.0,0.2,2.8,"piece:60,bowl:100"},
            new Object[]{"Tomato","Vegetables","100","g",18.0,0.9,3.9,0.2,1.2,"piece:90"},
            new Object[]{"Onion","Vegetables","100","g",40.0,1.1,9.3,0.1,1.7,"piece:100"},
            new Object[]{"Cucumber","Vegetables","100","g",15.0,0.7,3.6,0.1,0.5,"piece:200,bowl:100"},
            new Object[]{"Sweet Potato","Vegetables","100","g",86.0,1.6,20.0,0.1,3.0,"piece:130"},
            new Object[]{"Potato (boiled)","Vegetables","100","g",87.0,1.9,20.0,0.1,1.8,"piece:150,bowl:100"},
            new Object[]{"Cauliflower","Vegetables","100","g",25.0,1.9,5.0,0.3,2.0,"bowl:100,katori:100"},
            new Object[]{"Cabbage","Vegetables","100","g",25.0,1.3,5.8,0.1,2.5,"bowl:100,katori:100"},
            new Object[]{"Bhindi (Okra)","Vegetables","100","g",33.0,1.9,7.0,0.2,3.2,"bowl:100,katori:100"},
            new Object[]{"Brinjal (Baingan)","Vegetables","100","g",25.0,1.0,5.9,0.2,3.0,"bowl:100,katori:100"},
            new Object[]{"Capsicum","Vegetables","100","g",20.0,0.9,4.6,0.2,1.7,"piece:120,bowl:100"},
            new Object[]{"Mixed Vegetable Curry","Vegetables","150","g",90.0,2.5,12.0,3.5,3.0,"bowl:200,katori:200"},
            new Object[]{"Aloo Gobi","Vegetables","150","g",100.0,2.5,16.0,3.5,2.5,"bowl:200,katori:200"},
            new Object[]{"Bhindi Masala","Vegetables","100","g",75.0,2.2,9.0,4.0,3.0,"bowl:150,katori:150"},
            new Object[]{"Baingan Bharta","Vegetables","150","g",95.0,2.0,11.0,5.0,3.5,"bowl:200,katori:200"},
            new Object[]{"Mushroom","Vegetables","100","g",22.0,3.1,3.3,0.3,1.0,"bowl:100,katori:100"},
            new Object[]{"Beetroot","Vegetables","100","g",43.0,1.6,10.0,0.2,2.8,"bowl:100,piece:80"},
            new Object[]{"Lauki (Bottle Gourd)","Vegetables","100","g",14.0,0.6,3.4,0.0,1.0,"bowl:100,katori:100"},
            new Object[]{"Methi (Fenugreek leaves)","Vegetables","100","g",49.0,4.4,6.0,0.9,3.0,"bowl:80,katori:80"},

            // ── NUTS, SEEDS, OILS ──────────────────────────────────────────
            new Object[]{"Almonds","Nuts","30","g",579.0,21.0,22.0,50.0,12.5,"piece:1.2,tbsp:9"},
            new Object[]{"Walnuts","Nuts","30","g",654.0,15.0,14.0,65.0,6.7,"piece:5,tbsp:8"},
            new Object[]{"Peanuts","Nuts","30","g",567.0,26.0,16.0,49.0,8.5,"tbsp:9"},
            new Object[]{"Cashews","Nuts","30","g",553.0,18.0,30.0,44.0,3.3,"piece:2,tbsp:9"},
            new Object[]{"Pistachios","Nuts","30","g",560.0,20.0,28.0,45.0,10.0,"piece:0.8,tbsp:8"},
            new Object[]{"Chia Seeds","Nuts","15","g",486.0,17.0,42.0,31.0,34.0,"tbsp:12,tsp:4"},
            new Object[]{"Flax Seeds","Nuts","15","g",534.0,18.0,29.0,42.0,27.0,"tbsp:10,tsp:3.5"},
            new Object[]{"Sunflower Seeds","Nuts","15","g",584.0,21.0,20.0,51.0,8.6,"tbsp:8,tsp:2.7"},
            new Object[]{"Olive Oil","Oils","10","ml",884.0,0.0,0.0,100.0,0.0,"tbsp:14,tsp:4.5"},
            new Object[]{"Coconut Oil","Oils","10","ml",862.0,0.0,0.0,100.0,0.0,"tbsp:14,tsp:4.5"},
            new Object[]{"Mustard Oil","Oils","10","ml",884.0,0.0,0.0,100.0,0.0,"tbsp:14,tsp:4.5"},
            new Object[]{"Ghee","Oils","10","g",900.0,0.0,0.0,100.0,0.0,"tbsp:13,tsp:4.5"},
            new Object[]{"Butter","Oils","10","g",717.0,0.9,0.1,81.0,0.0,"tbsp:14,tsp:5"},

            // ── FISH & SEAFOOD ───────────────────────────────────────────────
            new Object[]{"Salmon","Fish","100","g",208.0,20.0,0.0,13.0,0.0,null},
            new Object[]{"Tuna","Fish","100","g",132.0,28.0,0.0,1.0,0.0,null},
            new Object[]{"Rohu Fish Curry","Fish","150","g",125.0,18.0,3.0,5.0,0.5,"bowl:200,katori:200"},
            new Object[]{"Pomfret Fry","Fish","100","g",180.0,19.0,2.0,11.0,0.0,"piece:100"},

            // ── SUPPLEMENTS / SPREADS / SWEETENERS ───────────────────────────
            new Object[]{"Whey Protein","Supplements","30","g",352.0,75.0,8.0,4.0,0.0,"tbsp:30"},
            new Object[]{"Peanut Butter","Spreads","32","g",588.0,25.0,20.0,50.0,6.0,"tbsp:16,tsp:5"},
            new Object[]{"Honey","Sweeteners","21","g",304.0,0.3,82.0,0.0,0.2,"tbsp:21,tsp:7"},
            new Object[]{"Sugar","Sweeteners","10","g",387.0,0.0,100.0,0.0,0.0,"tbsp:12,tsp:4"},
            new Object[]{"Jaggery","Sweeteners","10","g",383.0,0.4,98.0,0.1,0.0,"tbsp:15,tsp:5"},

            // ── BEVERAGES (with more variety) ────────────────────────────────
            new Object[]{"Tea (without milk)","Beverages","200","ml",1.0,0.0,0.3,0.0,0.0,"cup:200"},
            new Object[]{"Tea (with milk)","Beverages","200","ml",35.0,1.2,4.5,1.2,0.0,"cup:200"},
            new Object[]{"Coffee (black)","Beverages","200","ml",2.0,0.3,0.0,0.0,0.0,"cup:200"},
            new Object[]{"Coffee (with milk)","Beverages","200","ml",40.0,1.5,5.0,1.5,0.0,"cup:200"},
            new Object[]{"Filter Coffee","Beverages","150","ml",60.0,2.0,7.0,2.5,0.0,"cup:150"},
            new Object[]{"Green Tea","Beverages","200","ml",1.0,0.0,0.0,0.0,0.0,"cup:200"},
            new Object[]{"Coconut Water","Beverages","250","ml",19.0,0.7,3.7,0.2,1.1,"cup:250"},
            new Object[]{"Fresh Lime Water","Beverages","250","ml",20.0,0.1,5.0,0.0,0.1,"cup:250"},
            new Object[]{"Orange Juice","Beverages","250","ml",45.0,0.7,10.4,0.2,0.2,"cup:250"},
            new Object[]{"Protein Shake","Beverages","300","ml",120.0,20.0,5.0,2.0,1.0,"cup:300"},
            new Object[]{"Buttermilk (spiced)","Beverages","200","ml",38.0,2.8,4.5,1.0,0.0,"cup:200,katori:150"},
            new Object[]{"Sugarcane Juice","Beverages","250","ml",97.0,0.2,25.0,0.0,0.0,"cup:250"},

            // ── SNACKS ───────────────────────────────────────────────────────
            new Object[]{"Sprouts Salad","Snacks","100","g",75.0,7.0,12.0,1.0,4.0,"bowl:100,katori:100"},
            new Object[]{"Bhel Puri","Snacks","100","g",180.0,4.0,30.0,5.0,2.0,"bowl:150,katori:150"},
            new Object[]{"Samosa","Snacks","60","g",262.0,4.0,28.0,15.0,2.0,"piece:60"},
            new Object[]{"Dhokla","Snacks","60","g",160.0,5.0,22.0,5.0,1.5,"piece:60"},
            new Object[]{"Khakhra","Snacks","20","g",380.0,11.0,60.0,9.0,5.0,"piece:20"},
            new Object[]{"Murmura (Puffed Rice)","Snacks","30","g",402.0,7.5,80.0,0.5,2.0,"bowl:30,cup:30"},
            new Object[]{"Roasted Chana","Snacks","30","g",364.0,20.0,57.0,5.0,17.0,"bowl:30,tbsp:10"},
            new Object[]{"Makhana (Fox Nuts)","Snacks","30","g",347.0,9.7,76.0,0.1,14.5,"bowl:30,cup:25"},

            // ── SOUTH INDIAN SPECIALS ───────────────────────────────────────
            new Object[]{"Rasam","South Indian","150","g",45.0,1.5,8.0,1.0,1.0,"bowl:200,katori:200"},
            new Object[]{"Curd Rice","South Indian","150","g",130.0,4.0,22.0,3.0,0.5,"bowl:200,katori:200"},
            new Object[]{"Lemon Rice","South Indian","150","g",165.0,3.0,30.0,4.5,1.0,"bowl:200,katori:200"},
            new Object[]{"Tomato Rice","South Indian","150","g",170.0,3.2,32.0,4.0,1.2,"bowl:200,katori:200"},
            new Object[]{"Coconut Chutney","South Indian","30","g",195.0,2.0,7.0,18.0,2.5,"tbsp:15,katori:30"},
            new Object[]{"Sambar Vada","South Indian","100","g",210.0,5.0,28.0,9.0,2.0,"piece:60"},
            new Object[]{"Medu Vada","South Indian","50","g",180.0,5.5,20.0,9.0,2.0,"piece:50"},
            new Object[]{"Appam","South Indian","60","g",150.0,3.0,28.0,2.5,0.8,"piece:60"}
        );

        for (Object[] f : foods) {
            FoodDatabase fd = new FoodDatabase();
            fd.setFoodName((String) f[0]);
            fd.setCategory((String) f[1]);
            fd.setServingSize((String) f[2]);
            fd.setServingUnit((String) f[3]);
            fd.setCaloriesPer100g((Double) f[4]);
            fd.setProteinPer100g((Double) f[5]);
            fd.setCarbsPer100g((Double) f[6]);
            fd.setFatPer100g((Double) f[7]);
            fd.setFiberPer100g((Double) f[8]);
            fd.setUnitGramsOverride((String) f[9]);
            foodRepo.save(fd);
        }
    }
}
