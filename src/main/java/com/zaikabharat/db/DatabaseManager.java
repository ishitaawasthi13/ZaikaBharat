package com.zaikabharat.db;

import java.nio.file.*;
import java.sql.*;

public class DatabaseManager {

    private static Connection conn;
    private static final String DB_PATH =
        System.getProperty("user.home") + "/.zaikabharat/recipes.db";

    public static void initialize() {
        try {
            Files.createDirectories(Paths.get(DB_PATH).getParent());
            conn = DriverManager.getConnection("jdbc:sqlite:" + DB_PATH);
            // MUST use try-with-resources — unclosed Statement = SQLITE_BUSY on next call
            try (Statement st = conn.createStatement()) {
                st.execute("PRAGMA busy_timeout = 10000");
            }
            createSchema();
            seedIfEmpty();
        } catch (Exception e) {
            throw new RuntimeException("DB init failed: " + e.getMessage(), e);
        }
    }

    private static void createSchema() throws SQLException {
        try (Statement st = conn.createStatement()) {
            st.execute("""
            CREATE TABLE IF NOT EXISTS recipes (
                id                INTEGER PRIMARY KEY AUTOINCREMENT,
                name              TEXT NOT NULL,
                info              TEXT,
                origin            TEXT,
                total_time        TEXT,
                ingredients       TEXT,
                tags              TEXT,
                preparation_steps TEXT,
                cook_steps        TEXT,
                recipe            TEXT,
                best_serving_with TEXT,
                type              TEXT,
                is_veg            INTEGER DEFAULT 1,
                calories          INTEGER DEFAULT 0,
                protein_g         INTEGER DEFAULT 0,
                carbs_g           INTEGER DEFAULT 0,
                fiber_g           INTEGER DEFAULT 0,
                fat_g             INTEGER DEFAULT 0,
                spice_level       INTEGER DEFAULT 2,
                difficulty        TEXT    DEFAULT 'Medium'
            )
        """);
            st.execute("""
            CREATE TABLE IF NOT EXISTS meal_plan (
                day_of_week       TEXT NOT NULL,
                meal_type         TEXT NOT NULL,
                recipe_id         INTEGER,
                PRIMARY KEY (day_of_week, meal_type)
            )
        """);
        }
    }

    private static void seedIfEmpty() throws SQLException {
        try (ResultSet rs = conn.createStatement().executeQuery("SELECT COUNT(*) FROM recipes")) {
            if (rs.next() && rs.getInt(1) > 0) return;
        }

        // {name, info, origin, total_time, ingredients, tags, prep_steps, cook_steps,
        //  recipe, best_serving_with, type, is_veg(int), cal, pro, carb, fib, fat, spice, difficulty}
        Object[][] rows = {
            {"Butter Chicken",
             "Creamy, mildly spiced tomato-butter chicken curry — India's most beloved dish abroad.",
             "Delhi, North India","45 min",
             "chicken,butter,cream,tomatoes,onion,garlic,ginger,garam masala,kashmiri chili,cashews",
             "north_india,non_veg,creamy,mild,dinner",
             "1. Marinate chicken|2. Grill pieces|3. Make makhani sauce|4. Combine and simmer",
             "Marinate chicken in yogurt and tandoori masala for 2 hours:7200|"
             +"Heat butter and add whole spices:60|"
             +"Add onion puree and fry golden:300|"
             +"Add ginger garlic paste:120|"
             +"Add tomato puree and cook until oil separates:480|"
             +"Add kashmiri chili and garam masala:60|"
             +"Add grilled chicken and pour in cream:30|"
             +"Simmer gently for 5 minutes:300|"
             +"Adjust salt and sugar to taste:60|"
             +"Garnish with cream swirl and fresh coriander:60",
             "Rich makhani gravy with tender chicken in a velvety tomato-butter sauce.",
             "Naan, Butter Roti, Jeera Rice",
             "Main Course", 0, 320,28,12,2,18,2,"Medium"},

            {"Masala Dosa",
             "Crispy fermented rice crepe with spiced potato masala — the breakfast of the South.",
             "Karnataka, South India","30 min",
             "rice,urad dal,potato,onion,mustard seeds,curry leaves,green chili,turmeric,chana dal",
             "south_india,veg,breakfast,fried,spicy",
             "1. Ferment rice-dal batter overnight|2. Prepare potato masala|3. Spread thin crepe on tawa",
             "Heat tawa and pour a ladle of batter:30|"
             +"Spread in circular motion to make a thin crepe:30|"
             +"Drizzle oil on edges and cook until golden:120|"
             +"Add potato filling on one side:30|"
             +"Fold dosa over filling:15|"
             +"Serve hot immediately:10",
             "Golden crispy rice crepe stuffed with tempered potato filling.",
             "Coconut Chutney, Sambar, Tomato Chutney",
             "Breakfast", 1, 280,7,52,4,6,3,"Medium"},

            {"Rasgulla",
             "Spongy cottage cheese balls in light sugar syrup — sweetness from the East.",
             "West Bengal, East India","60 min",
             "chena,sugar,water,cardamom,rose water,maida",
             "east_india,veg,sweet,festival,diwali,holi,dessert",
             "1. Make fresh chena|2. Knead smooth dough|3. Roll balls|4. Simmer in syrup",
             "Curdle full-fat milk with lemon juice:600|"
             +"Drain whey through muslin cloth:30|"
             +"Knead chena for 10 minutes until smooth:600|"
             +"Roll into 20 equal smooth balls:300|"
             +"Boil sugar and water to make light syrup:300|"
             +"Add chena balls and cover:30|"
             +"Cook on medium flame for 15 minutes:900|"
             +"Cool and refrigerate for 2 hours:7200",
             "Soft spongy chena balls that absorb light rose and cardamom syrup.",
             "Serve chilled as dessert",
             "Dessert", 1, 160,4,34,0,3,1,"Hard"},

            {"Dhokla",
             "Light, fluffy steamed chickpea cake with tangy-sweet temper — pride of Gujarat.",
             "Gujarat, West India","40 min",
             "besan,yogurt,ginger,green chili,turmeric,eno,mustard seeds,curry leaves,sesame seeds,sugar",
             "west_india,veg,breakfast,steamed,mild,sour",
             "1. Mix besan batter|2. Add Eno and steam|3. Temper with mustard and curry leaves",
             "Mix besan, yogurt, turmeric, ginger-green chili paste and salt:120|"
             +"Add Eno and mix gently just before steaming:30|"
             +"Pour into greased steaming plate immediately:30|"
             +"Steam for 20 minutes on medium heat:1200|"
             +"Cool for 5 minutes then cut into pieces:300|"
             +"Heat oil and add mustard seeds, curry leaves, green chilies:60|"
             +"Pour tempering over dhokla and sprinkle coconut and coriander:60",
             "Fluffy yellow steamed cake with perfect tangy-sweet balance.",
             "Green Chutney, Tamarind Chutney",
             "Snack", 1, 180,9,28,3,4,2,"Medium"},

            {"Bhutte Ka Kees",
             "Street-style grated corn cooked with spices — Indore's most beloved street snack.",
             "Indore, Madhya Pradesh","25 min",
             "corn,milk,ghee,green chili,ginger,turmeric,mustard seeds,lime,coconut,coriander",
             "central_india,veg,street_food,spicy,breakfast",
             "1. Grate fresh corn cobs|2. Cook with milk and spices|3. Garnish with coconut and lime",
             "Heat ghee and add mustard seeds until they splutter:60|"
             +"Add green chili and ginger paste:30|"
             +"Add grated corn and mix well:60|"
             +"Pour milk and cook on medium heat:480|"
             +"Add turmeric and salt:30|"
             +"Cook until creamy and thick:300|"
             +"Finish with lime juice and fresh coconut:60|"
             +"Garnish with coriander:30",
             "Creamy grated corn cooked with mustard seeds and finished with fresh coconut.",
             "Serve hot with lime and sev",
             "Snack", 1, 220,6,38,5,6,3,"Easy"},

            {"Chole Bhature",
             "Tangy chickpea curry with fluffy deep-fried bread — the quintessential North Indian brunch.",
             "Punjab, North India","50 min",
             "chickpeas,maida,onion,tomato,ginger,garlic,chole masala,tamarind,oil,yogurt",
             "north_india,veg,breakfast,fried,spicy",
             "1. Pressure cook soaked chickpeas|2. Make masala|3. Knead bhature dough|4. Deep fry",
             "Pressure cook soaked chickpeas for 4 whistles:1200|"
             +"Heat oil and add bay leaf and dried red chilies:30|"
             +"Add onion and fry until deep brown:360|"
             +"Add ginger-garlic paste and cook:120|"
             +"Add tomato and cook until oil separates:480|"
             +"Add chole masala and tamarind pulp:60|"
             +"Add chickpeas and simmer for 20 minutes:1200|"
             +"Knead soft dough with maida, yogurt and oil:600|"
             +"Rest dough for 30 minutes:1800|"
             +"Roll into thick circles and deep fry until puffed:120",
             "Hearty spiced chickpea curry with pillow-soft fried bhaturas.",
             "Sliced onion, green chutney, pickle, lassi",
             "Breakfast", 1, 450,15,65,12,16,3,"Medium"},

            {"Rasam",
             "Thin peppery tomato-tamarind soup — South India's natural cold and flu remedy.",
             "Tamil Nadu, South India","25 min",
             "tamarind,tomatoes,pepper,cumin,garlic,mustard seeds,asafoetida,curry leaves,dried red chili,turmeric",
             "south_india,veg,soup,spicy,hot,cold_remedy,sickness,flu_recovery",
             "1. Extract tamarind water|2. Boil with tomatoes and spices|3. Temper with mustard",
             "Soak tamarind in warm water for 10 minutes then extract pulp:600|"
             +"Boil tamarind water with tomatoes, turmeric and salt:480|"
             +"Coarsely grind pepper, cumin and garlic:120|"
             +"Add ground spices to boiling mixture:30|"
             +"Simmer until raw smell leaves:300|"
             +"Heat oil and add mustard seeds until they splutter:60|"
             +"Add curry leaves and dried red chilies:30|"
             +"Add asafoetida and pour tempering over rasam:30|"
             +"Boil once more and serve piping hot:120",
             "A translucent, intensely peppery and tangy soup that clears the sinuses.",
             "Steamed rice, papad",
             "Soup", 1, 65,2,12,2,1,4,"Easy"},

            {"Khichdi",
             "Comforting rice and lentil porridge — India's ultimate healing food for sick days.",
             "All India","30 min",
             "rice,moong dal,ghee,cumin,turmeric,ginger,asafoetida,salt",
             "veg,sickness,flu_recovery,mild,hot,north_india,south_india,east_india,west_india,central_india",
             "1. Soak rice and dal|2. Pressure cook with spices|3. Finish with generous ghee",
             "Wash and soak rice and moong dal for 20 minutes:1200|"
             +"Heat ghee in pressure cooker:30|"
             +"Add cumin seeds and let them splutter:30|"
             +"Add asafoetida and grated ginger:30|"
             +"Add drained rice and dal and stir:30|"
             +"Add turmeric, salt and 3 cups water:30|"
             +"Pressure cook for 3 whistles:900|"
             +"Let pressure release naturally:600|"
             +"Open and add more ghee on top:30|"
             +"Serve steaming hot:10",
             "Soft nourishing one-pot meal of rice and lentils with minimal spices and golden ghee.",
             "Kadhi, plain yogurt, pickle, papad",
             "Main Course", 1, 240,10,42,6,5,1,"Easy"},

            {"Biryani",
             "Aromatic layered rice with slow-cooked chicken and whole spices — India's feast in a pot.",
             "Hyderabad, South India","90 min",
             "basmati rice,chicken,onion,yogurt,saffron,mint,coriander,fried onions,ghee,biryani masala,rose water",
             "north_india,south_india,non_veg,spicy,festival,dinner,ramadan",
             "1. Marinate chicken overnight|2. Par-cook rice|3. Layer and dum cook",
             "Marinate chicken in yogurt, biryani masala, mint and fried onions overnight:28800|"
             +"Soak basmati rice for 30 minutes:1800|"
             +"Boil water with whole spices and par-cook rice until 70% done:900|"
             +"Heat ghee and cook marinated chicken until sealed:600|"
             +"Layer par-cooked rice over the chicken:60|"
             +"Add saffron milk, mint and fried onions on top:60|"
             +"Seal pot tightly with lid or dough:120|"
             +"Dum cook on low heat for 25 minutes:1500|"
             +"Rest for 10 minutes before opening:600|"
             +"Gently mix layers from bottom and serve:120",
             "Fragrant basmati layered with spiced chicken, caramelized onions and saffron.",
             "Raita, mirchi ka salan, sliced onions",
             "Main Course", 0, 520,32,58,4,18,3,"Hard"},

            {"Gulab Jamun",
             "Soft khoya dumplings soaked in cardamom-rose syrup — the festival dessert of India.",
             "All India","45 min",
             "khoya,maida,baking powder,sugar,rose water,cardamom,saffron,oil",
             "veg,sweet,festival,diwali,holi,ganesh_chaturthi,dessert",
             "1. Knead khoya dough|2. Roll smooth balls|3. Fry on low heat|4. Soak in syrup",
             "Knead khoya, maida and baking powder into smooth dough:300|"
             +"Rest dough for 15 minutes:900|"
             +"Divide into 20 equal balls and roll very smooth:300|"
             +"Heat oil on very low flame:60|"
             +"Fry balls slowly turning constantly until deep brown:600|"
             +"Boil sugar and water to one-string syrup:480|"
             +"Add cardamom and rose water to syrup:30|"
             +"Drop warm fried balls into warm syrup:30|"
             +"Soak for 30 minutes before serving:1800",
             "Pillowy milky dumplings that melt in the mouth, soaked in fragrant rose syrup.",
             "Serve warm with rabri or vanilla ice cream",
             "Dessert", 1, 200,3,35,0,7,1,"Medium"},

            {"Pav Bhaji",
             "Tangy buttery vegetable mash with toasted buns — Mumbai's most iconic street food.",
             "Mumbai, West India","35 min",
             "mixed vegetables,potato,onion,tomato,capsicum,butter,pav bhaji masala,pav buns,lime,coriander",
             "west_india,veg,street_food,spicy,dinner",
             "1. Pressure cook vegetables|2. Mash with spices on tawa|3. Toast pav with butter",
             "Pressure cook potatoes, cauliflower and peas until soft:600|"
             +"Mash coarsely and set aside:120|"
             +"Heat butter in large flat tawa:60|"
             +"Fry onions until golden brown:240|"
             +"Add ginger garlic paste and cook:120|"
             +"Add tomatoes and capsicum, mash together:300|"
             +"Add pav bhaji masala and mashed vegetables:60|"
             +"Mash continuously while cooking on tawa:480|"
             +"Add generous butter and squeeze of lime:60|"
             +"Toast pav buns with butter on the same tawa:120",
             "Thick tangy buttery mash of vegetables, served with buttered toasted bread rolls.",
             "Finely chopped onion, lime wedge, extra butter",
             "Snack / Main Course", 1, 380,9,52,8,15,3,"Easy"},

            {"Poha",
             "Flattened rice stir-fry with onions and peanuts — Central India's quick comforting breakfast.",
             "Madhya Pradesh","15 min",
             "poha,onion,peanuts,mustard seeds,curry leaves,green chili,turmeric,sugar,lime,coriander",
             "central_india,veg,breakfast,mild,quick,hosteller",
             "1. Rinse poha until soft|2. Temper with spices|3. Add poha and gently mix",
             "Wash poha in water and drain immediately leaving moist:30|"
             +"Heat oil and add mustard seeds:30|"
             +"Add curry leaves, green chili and peanuts:60|"
             +"Add onions and fry until translucent:180|"
             +"Add turmeric and salt:30|"
             +"Add poha and mix very gently:120|"
             +"Add sugar and squeeze of lime:30|"
             +"Garnish with fresh coriander and sev:30|"
             +"Serve hot immediately:10",
             "Soft light flattened rice gently spiced with turmeric, mustard and the crunch of peanuts.",
             "Chai, green chutney",
             "Breakfast", 1, 220,6,38,3,6,2,"Easy"},

            {"Thepla",
             "Spiced whole wheat flatbread with fenugreek — Gujarat's legendary travel and hostel food.",
             "Gujarat, West India","25 min",
             "whole wheat flour,methi leaves,yogurt,green chili,turmeric,oil,ajwain,sesame seeds",
             "west_india,veg,breakfast,mild,hosteller,quick",
             "1. Knead spiced dough with methi|2. Roll thin rounds|3. Cook on tawa with oil",
             "Finely chop methi leaves and knead into wheat flour:120|"
             +"Add yogurt, green chili paste, turmeric, ajwain and sesame:60|"
             +"Knead firm yet pliable dough adding water as needed:300|"
             +"Rest dough for 15 minutes:900|"
             +"Divide into portions and roll very thin:300|"
             +"Cook on hot tawa with little oil each side until golden:120|"
             +"Stack finished theplas together:30",
             "Thin golden flatbreads fragrant with fenugreek and ajwain.",
             "Mango pickle, yogurt, masala chai",
             "Breakfast", 1, 180,5,28,4,6,2,"Easy"},

            {"Lassi",
             "Chilled creamy yogurt drink — North India's answer to the summer heat.",
             "Punjab, North India","5 min",
             "yogurt,sugar,cardamom,rose water,milk,ice,saffron",
             "north_india,veg,drink,sweet,cold,refreshing,summer",
             "1. Blend yogurt with sugar|2. Add rose water and cardamom|3. Pour over ice",
             "Add full-fat yogurt to blender:15|"
             +"Add sugar, cardamom and rose water:15|"
             +"Add chilled milk and ice cubes:15|"
             +"Blend until frothy and smooth:60|"
             +"Taste and adjust sugar:15|"
             +"Pour into tall glasses over ice:15|"
             +"Garnish with saffron and a dollop of cream:15",
             "Thick frothy cooling yogurt drink, silky smooth with rose and cardamom notes.",
             "Serve immediately while frothy",
             "Beverage", 1, 180,7,28,0,5,1,"Easy"},

            {"Litti Chokha",
             "Charcoal-roasted wheat balls stuffed with sattu — Bihar's soul food.",
             "Bihar, East India","60 min",
             "whole wheat flour,sattu,brinjal,onion,garlic,green chili,ajwain,mustard oil,lime,coriander",
             "east_india,veg,smoky,dinner,spicy",
             "1. Prepare sattu filling|2. Stuff into dough balls|3. Roast over coals|4. Make chokha",
             "Knead whole wheat dough with oil, ajwain and salt:300|"
             +"Mix sattu with raw onion, ginger, green chili, lime and mustard oil:300|"
             +"Stuff sattu filling inside dough balls and seal tightly:300|"
             +"Roast on direct flame or charcoal, turning continuously:1200|"
             +"Brush with generous ghee while hot:60|"
             +"Roast brinjal, tomatoes and garlic directly on flame until charred:900|"
             +"Peel and mash with raw onion, coriander, mustard oil and salt:300|"
             +"Serve litti dipped in ghee alongside chokha:30",
             "Earth-flavored roasted wheat balls with a nutty sattu heart, with smoky vegetable mash.",
             "Ghee, raw onion, pickled chili",
             "Main Course", 1, 420,16,62,10,14,3,"Hard"},

            {"Idli Sambar",
             "Steamed rice-lentil cakes with spiced vegetable soup — the healthiest South Indian meal.",
             "Tamil Nadu, South India","45 min",
             "idli rice,urad dal,vegetables,toor dal,sambar powder,tamarind,mustard seeds,curry leaves,shallots",
             "south_india,veg,breakfast,steamed,mild,healthy",
             "1. Ferment idli batter overnight|2. Steam idlis|3. Make vegetable sambar",
             "Soak idli rice and urad dal separately for 4 hours:14400|"
             +"Grind separately and mix to smooth batter:900|"
             +"Ferment at room temperature for 8 hours:28800|"
             +"Pour batter into greased idli moulds:60|"
             +"Steam in idli cooker for 10 minutes:600|"
             +"Pressure cook toor dal with vegetables until soft:480|"
             +"Prepare tamarind extract:120|"
             +"Temper mustard, curry leaves and shallots in oil:120|"
             +"Add sambar powder, tamarind and cooked dal:60|"
             +"Simmer for 10 minutes:600",
             "Soft steamed rice-lentil cakes with tangy spiced lentil and vegetable soup.",
             "Coconut chutney, ghee, sambar",
             "Breakfast", 1, 200,8,38,5,3,2,"Hard"},

            {"Macher Jhol",
             "Delicate mustard-flavored Bengali fish curry — East Indian simplicity at its finest.",
             "West Bengal, East India","30 min",
             "rohu fish,potato,tomato,mustard oil,mustard paste,turmeric,green chili,panch phoron,coriander",
             "east_india,non_veg,spicy,lunch,dinner",
             "1. Marinate fish with turmeric|2. Fry fish pieces|3. Make mustard gravy|4. Simmer fish",
             "Marinate fish pieces with turmeric and salt for 20 minutes:1200|"
             +"Heat mustard oil in kadhai until smoking point:120|"
             +"Fry fish pieces 2 minutes per side until golden:240|"
             +"Remove fish and add panch phoron to the same oil:30|"
             +"Add potato wedges and fry until light golden:180|"
             +"Add green chilies and chopped tomato:60|"
             +"Add mustard paste and turmeric diluted in water:60|"
             +"Pour in 2 cups water and bring to boil:300|"
             +"Add fried fish and simmer gently for 8 minutes:480|"
             +"Finish with fresh coriander and serve:30",
             "Clean delicately spiced fish curry where fresh mustard oil carries all the flavor.",
             "Steamed white rice only",
             "Main Course", 0, 280,30,8,2,14,2,"Medium"},

            {"Chai",
             "Spiced milk tea with ginger and cardamom — India's national drink and cold remedy.",
             "North India (Universal)","10 min",
             "black tea,milk,sugar,ginger,cardamom,cinnamon,black pepper,cloves",
             "north_india,veg,drink,hot,cold_remedy,sickness,morning,refreshing",
             "1. Boil water with spices|2. Add tea leaves|3. Add milk|4. Strain and serve",
             "Crush ginger and cardamom coarsely with mortar:30|"
             +"Boil water with ginger, cardamom, cinnamon and pepper:180|"
             +"Add black tea leaves and boil for 1 minute:60|"
             +"Pour in milk and bring to full rolling boil:120|"
             +"Reduce heat and simmer for 2 minutes:120|"
             +"Add sugar and stir:30|"
             +"Strain through fine sieve into cups:30|"
             +"Serve piping hot:10",
             "Bold aromatic brew of black tea and warm spices, simmered with whole milk to a golden hue.",
             "Biscuits, toast, samosa",
             "Beverage", 1, 80,3,12,0,3,2,"Easy"},

            {"Shahi Tukda",
             "Saffron-rose bread pudding in thick rabri — a Mughal-era royal dessert.",
             "Lucknow, North India","45 min",
             "bread,milk,sugar,saffron,cardamom,rose water,ghee,almonds,pistachios",
             "north_india,veg,sweet,festival,diwali,ramadan,dessert",
             "1. Reduce milk to rabri|2. Fry bread in ghee|3. Soak in syrup|4. Top with rabri",
             "Reduce full-fat milk to half with sugar and cardamom stirring continuously:2400|"
             +"Add saffron soaked in warm milk to the rabri:30|"
             +"Add rose water and remove from heat:30|"
             +"Cut bread into triangles and deep fry in ghee until golden:240|"
             +"Prepare light sugar syrup with cardamom:300|"
             +"Dip fried bread briefly in warm sugar syrup:30|"
             +"Arrange on serving plate:30|"
             +"Pour thick cold rabri generously over bread:60|"
             +"Garnish with chopped nuts and refrigerate:1800",
             "Rich fried bread soaked in syrup, smothered with silky saffron-scented thickened milk.",
             "Best served chilled on its own",
             "Dessert", 1, 380,9,48,1,17,1,"Hard"},

            {"Maggi Masala",
             "Two-minute noodles elevated with fresh vegetables — every hosteller's midnight savior.",
             "All India","7 min",
             "maggi noodles,masala packet,onion,tomato,green chili,water,oil",
             "veg,hosteller,quick,spicy,midnight_snack",
             "1. Sauté fresh vegetables|2. Add water and boil|3. Add noodles and masala",
             "Chop onion, tomato and green chili fine:60|"
             +"Heat oil in pan and fry onion until translucent:90|"
             +"Add tomato and green chili and cook for 2 minutes:120|"
             +"Add 1.5 cups water and bring to boil:120|"
             +"Add noodles and masala packet:15|"
             +"Cook stirring for exactly 2 minutes:120|"
             +"Remove from heat while still slightly soupy:15|"
             +"Serve immediately before it goes sticky:10",
             "Instant noodles made proper with fresh vegetables and doubled masala.",
             "Sauce, pickle, eat straight from the pan",
             "Snack", 1, 320,8,52,2,9,3,"Easy"}
        };

        String sql = """
            INSERT INTO recipes (name,info,origin,total_time,ingredients,tags,
                preparation_steps,cook_steps,recipe,best_serving_with,type,is_veg,
                calories,protein_g,carbs_g,fiber_g,fat_g,spice_level,difficulty)
            VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)
        """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (Object[] row : rows) {
                ps.setString(1, (String) row[0]);
                ps.setString(2, (String) row[1]);
                ps.setString(3, (String) row[2]);
                ps.setString(4, (String) row[3]);
                ps.setString(5, (String) row[4]);
                ps.setString(6, (String) row[5]);
                ps.setString(7, (String) row[6]);
                ps.setString(8, (String) row[7]);
                ps.setString(9, (String) row[8]);
                ps.setString(10, (String) row[9]);
                ps.setString(11, (String) row[10]);
                ps.setInt(12, (Integer) row[11]);
                ps.setInt(13, (Integer) row[12]);
                ps.setInt(14, (Integer) row[13]);
                ps.setInt(15, (Integer) row[14]);
                ps.setInt(16, (Integer) row[15]);
                ps.setInt(17, (Integer) row[16]);
                ps.setInt(18, (Integer) row[17]);
                ps.setString(19, (String) row[18]);
                ps.addBatch();
            }
            ps.executeBatch();
        }
        System.out.println("Seeded " + rows.length + " recipes.");
    }

    public static Connection getConnection() { return conn; }

    public static void close() {
        try { if (conn != null && !conn.isClosed()) conn.close(); }
        catch (SQLException ignored) {}
    }
}
