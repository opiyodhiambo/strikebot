package Botlogger.service;

import Botlogger.database.StrikeDatabase;
import Botlogger.model.Strike;

import java.sql.Timestamp;
import java.util.List;

public class StrikeService {
    private final StrikeDatabase database = new StrikeDatabase();

    public StrikeService() {
        initializeStrikesIfNeeded();
    }

    private void initializeStrikesIfNeeded() {

        if (database.hasBeenInitialized()) {
            System.out.println("✅ Database already initialized with strikes - skipping import");
            System.out.println("📊 Current database has " + database.getTotalStrikeCount() + " strikes");
            return;
        }

        if (database.getTotalStrikeCount() == 0) {
            System.out.println("🔄 Fresh database detected, importing initial strikes...");
            importInitialStrikes();
            database.markAsInitialized();
            System.out.println("✅ Strike import complete! Database will not re-import on future startups.");
        } else {

            System.out.println("📊 Database has " + database.getTotalStrikeCount() + " strikes but wasn't marked as initialized");
            System.out.println("🔧 Marking database as initialized to prevent future auto-imports");
            database.markAsInitialized();
        }
    }

    private void importInitialStrikes() {
        Timestamp importDate = Timestamp.valueOf("2025-07-23 12:00:00");

        database.clearStrikes("1254924353364168846");
        database.addStrike("1254924353364168846", "voiding without permission", "ZRE MANAGEMENT", importDate);
        database.addStrike("1254924353364168846", "talking in <#1099483648824201328>", "ZRE MANAGEMENT", importDate);
        database.addStrike("1254924353364168846", "voiding without permission - cg", "ZRE MANAGEMENT", importDate);

        database.clearStrikes("1177869414461345832");
        database.addStrike("1177869414461345832", "talking in <#1099483648824201328>", "ZRE MANAGEMENT", importDate);
        database.addStrike("1177869414461345832", "talking", "ZRE MANAGEMENT", importDate);
        database.addStrike("1177869414461345832", "talking in <#1099483648824201328>", "ZRE MANAGEMENT", importDate);


        database.clearStrikes("1059524335808815185");
        database.addStrike("1059524335808815185", "talking/sending a gif :swag:", "ZRE MANAGEMENT", importDate);
        database.addStrike("1059524335808815185", "talking in <#1099483648824201328>", "ZRE MANAGEMENT", importDate);
        database.addStrike("1059524335808815185", "unnecessary talking in <#1099483648824201328>", "ZRE MANAGEMENT", importDate);


        database.clearStrikes("1209971553257914398");
        database.addStrike("1209971553257914398", "talking in <#1099483648824201328>", "ZRE MANAGEMENT", importDate);
        database.addStrike("1209971553257914398", "talking in <#1099483648824201328>", "ZRE MANAGEMENT", importDate);
        database.addStrike("1209971553257914398", "trying to change format", "ZRE MANAGEMENT", importDate);


        database.clearStrikes("765259790275969105");
        database.addStrike("765259790275969105", "talking in <#1099483648824201328>", "ZRE MANAGEMENT", importDate);
        database.addStrike("765259790275969105", "-", "ZRE MANAGEMENT", importDate);
        database.addStrike("765259790275969105", "trying to change format", "ZRE MANAGEMENT", importDate);


        database.clearStrikes("1300240648829669446");
        database.addStrike("1300240648829669446", "voiding without permission", "ZRE MANAGEMENT", importDate);
        database.addStrike("1300240648829669446", "going to strike 3", "ZRE MANAGEMENT", importDate);
        database.addStrike("1300240648829669446", "ending a event regardless if it's dead or not.", "ZRE MANAGEMENT", importDate);


        database.clearStrikes("358694457169936384");
        database.addStrike("358694457169936384", "talking in <#1099483648824201328>", "ZRE MANAGEMENT", importDate);
        database.addStrike("358694457169936384", "talking, talk in staff chat pls", "ZRE MANAGEMENT", importDate);
        database.addStrike("358694457169936384", "talking in <#1099483648824201328>", "ZRE MANAGEMENT", importDate);


        database.clearStrikes("650201858232549376");
        database.addStrike("650201858232549376", "voiding without permission (earlier today)", "ZRE MANAGEMENT", importDate);
        database.addStrike("650201858232549376", "unncessary comments in <#1099483648824201328>", "ZRE MANAGEMENT", importDate);
        database.addStrike("650201858232549376", "talking and voiding", "ZRE MANAGEMENT", importDate);


        database.clearStrikes("1237479866182664323");
        database.addStrike("1237479866182664323", "talking", "ZRE MANAGEMENT", importDate);
        database.addStrike("1237479866182664323", "voiding without permission", "ZRE MANAGEMENT", importDate);
        database.addStrike("1237479866182664323", "talking in <#1099483648824201328>", "ZRE MANAGEMENT", importDate);


        database.clearStrikes("1378700738267582514");
        database.addStrike("1378700738267582514", "teaming in our own events - 1st time", "ZRE MANAGEMENT", importDate);
        database.addStrike("1378700738267582514", "-", "ZRE MANAGEMENT", importDate);
        database.addStrike("1378700738267582514", "teaming again in our own events - 2nd time being today", "ZRE MANAGEMENT", importDate);


        database.clearStrikes("1334019321126457444");
        database.addStrike("1334019321126457444", "talking in cd channel naughty", "ZRE MANAGEMENT", importDate);
        database.addStrike("1334019321126457444", "talking in <#1099483648824201328>", "ZRE MANAGEMENT", importDate);


        database.clearStrikes("1270153372200341645");
        database.addStrike("1270153372200341645", "talking in <#1099483648824201328>", "ZRE MANAGEMENT", importDate);
        database.addStrike("1270153372200341645", "talking in <#1099483648824201328>", "ZRE MANAGEMENT", importDate);


        database.clearStrikes("1169732913600938126");
        database.addStrike("1169732913600938126", "voiding without permission (yesterdays event)", "ZRE MANAGEMENT", importDate);
        database.addStrike("1169732913600938126", "voiding without permission + unncessary talking in <#1099483648824201328>", "ZRE MANAGEMENT", importDate);


        database.clearStrikes("1319708291407806576");
        database.addStrike("1319708291407806576", "talking in <#1099483648824201328>", "ZRE MANAGEMENT", importDate);
        database.addStrike("1319708291407806576", "talking in <#1099483648824201328>", "ZRE MANAGEMENT", importDate);


        database.clearStrikes("913589810113949756");
        database.addStrike("913589810113949756", "talking in <#1099483648824201328>", "ZRE MANAGEMENT", importDate);
        database.addStrike("913589810113949756", "hosting too early", "ZRE MANAGEMENT", importDate);


        database.clearStrikes("1382914870495809556");
        database.addStrike("1382914870495809556", "voiding without perm", "ZRE MANAGEMENT", importDate);
        database.addStrike("1382914870495809556", "trying to change format + sending cents face picture in <#1099483648824201328> mulitple times", "ZRE MANAGEMENT", importDate);


        database.clearStrikes("1212205486561034331");
        database.addStrike("1212205486561034331", "voiding without permission", "ZRE MANAGEMENT", importDate);
        database.addStrike("1212205486561034331", "talking", "ZRE MANAGEMENT", importDate);


        database.clearStrikes("474012893386375188");
        database.addStrike("474012893386375188", "talking in <#1099483648824201328>", "ZRE MANAGEMENT", importDate);
        database.addStrike("474012893386375188", "talking in <#1099483648824201328> again", "ZRE MANAGEMENT", importDate);


        database.clearStrikes("702979435481006210");
        database.addStrike("702979435481006210", "voiding without perm", "ZRE MANAGEMENT", importDate);
        database.addStrike("702979435481006210", "voiding without permission", "ZRE MANAGEMENT", importDate);


        database.clearStrikes("782688521172418592");
        database.addStrike("782688521172418592", "talking in <#1099483648824201328>", "ZRE MANAGEMENT", importDate);
        database.addStrike("782688521172418592", "talking in <#1099483648824201328>", "ZRE MANAGEMENT", importDate);
        database.addStrike("782688521172418592", "voiding naughty boy", "ZRE MANAGEMENT", importDate);


        database.clearStrikes("1099375815017385994");
        database.addStrike("1099375815017385994", "cd was bad - syco", "ZRE MANAGEMENT", importDate);
        database.addStrike("1099375815017385994", "yapping in cd", "ZRE MANAGEMENT", importDate);


        database.clearStrikes("1300240648829669446");
        database.addStrike("1300240648829669446", "voiding without perm", "ZRE MANAGEMENT", importDate);
        database.addStrike("1300240648829669446", "voiding without perm - syco", "ZRE MANAGEMENT", importDate);


        database.clearStrikes("1168696606560886785");
        database.addStrike("1168696606560886785", "sending some random sexy pic of floaty opening his mouth, again so sexy tho 😩 :BunnyLove:", "ZRE MANAGEMENT", importDate);


        database.clearStrikes("1319708291407806576");
        database.addStrike("1319708291407806576", "talking in <#1099483648824201328>", "ZRE MANAGEMENT", importDate);


        database.clearStrikes("1133260081622949949");
        database.addStrike("1133260081622949949", "voiding without permission", "ZRE MANAGEMENT", importDate);


        database.clearStrikes("1117900730213335111");
        database.addStrike("1117900730213335111", "promoting your clan", "ZRE MANAGEMENT", importDate);


        database.clearStrikes("1217870408888356864");
        database.addStrike("1217870408888356864", "unncessary talking in <#1099483648824201328> + rigging games", "ZRE MANAGEMENT", importDate);


        database.clearStrikes("771132659262291989");
        database.addStrike("771132659262291989", "saying c u r t", "ZRE MANAGEMENT", importDate);


        database.clearStrikes("783838990251458561");
        database.addStrike("783838990251458561", "end a event, idc if its dead u still signed up to host it :swag:", "ZRE MANAGEMENT", importDate);


        database.clearStrikes("783838990251458561");
        database.addStrike("783838990251458561", "changing format bc bryce apparently said to do all east", "ZRE MANAGEMENT", importDate);


        database.clearStrikes("784138288843325470");
        database.addStrike("784138288843325470", "talking in <#1099483648824201328>", "ZRE MANAGEMENT", importDate);
        database.addStrike("784138288843325470", "talking in <#1099483648824201328>", "ZRE MANAGEMENT", importDate);


        database.clearStrikes("1392286882242625701");
        database.addStrike("1392286882242625701", "leaving mid event with no effort to get new hoster", "ZRE MANAGEMENT", importDate);


        database.clearStrikes("1389246078037135510");
        database.addStrike("1389246078037135510", "redoing a game", "ZRE MANAGEMENT", importDate);


        database.clearStrikes("701327814766362625");
        database.addStrike("701327814766362625", "modifying format + did an extra solo game", "ZRE MANAGEMENT", importDate);
    }

    public void issueStrike(String userId, String reason, String moderatorId) {
        database.addStrike(userId, reason, moderatorId, new Timestamp(System.currentTimeMillis()));
    }

    public List<Strike> getStrikes(String userId) {
        return database.getStrikes(userId);
    }

    public void clearStrikes(String userId) {
        database.clearStrikes(userId);
    }

    public void removeStrike(String userId, int strikeNumber) {
        database.removeStrike(userId, strikeNumber);
    }

    public void clearAllStrikes(String userId) {
        database.clearStrikes(userId);
    }

    public void editStrike(String userId, int strikeNumber, String newReason) {
        database.editStrike(userId, strikeNumber, newReason);
    }

    public void resetInitializationFlag() {
        System.out.println("⚠️ Resetting initialization flag - next restart will re-import strikes");
    }

    public List<String> getAllUsersWithStrikes() {
        return database.getAllUsersWithStrikes();
    }
}