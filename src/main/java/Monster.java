import org.sql2o.Connection;

import java.sql.Timestamp;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class Monster {
    private String name;
    private int personId;
    private int id;
    private int foodLevel;
    private int sleepLevel;
    private int playLevel;
    private Timer timer;

    private Timestamp birthday;
    private Timestamp lastSlept;
    private Timestamp lastAte;
    private Timestamp lastPlayed;

    public static final int MAX_FOOD_LEVEL = 3;
    public static final int MAX_SLEEP_LEVEL = 8;
    public static final int MAX_PLAY_LEVEL = 12;
    public static final int MIN_ALL_LEVELS = 0;

    public Monster(String name, int personId) {
        this.name = name;
        this.personId = personId;
        this.playLevel = MAX_PLAY_LEVEL / 2;
        sleepLevel = MAX_SLEEP_LEVEL / 2;
        foodLevel = MAX_FOOD_LEVEL / 2;
        timer = new Timer();
    }

    public int getPlayLevel(){
        return playLevel;
    }
    public int getSleepLevel(){
        return sleepLevel;
    }
    public int getFoodLevel(){
        return foodLevel;
    }

    public int getId() {
        return id;
    }

    public boolean isAlive() {
        if (foodLevel <= MIN_ALL_LEVELS ||
                playLevel <= MIN_ALL_LEVELS ||
                sleepLevel <= MIN_ALL_LEVELS) {
            return false;
        }
        return true;
    }
    public static List<Monster> all() {
        String sql = "SELECT * FROM monsters";
        try(Connection con = DB.sql2o.open()) {
            return con.createQuery(sql).executeAndFetch(Monster.class);
        }
    }
    public static Monster find(int id) {
        try(Connection con = DB.sql2o.open()) {
            String sql = "SELECT * FROM monsters where id=:id";
            Monster monster = con.createQuery(sql)
                    .addParameter("id", id)
                    .executeAndFetchFirst(Monster.class);
            return monster;
        }
    }
    public void depleteLevels(){
        if (isAlive()) {
            playLevel--;
            foodLevel--;
            sleepLevel--;
        }
    }
    public void play(){
        if (playLevel >= MAX_PLAY_LEVEL){
            throw new UnsupportedOperationException("You cannot play with monster anymore!");
        }
        try(Connection con = DB.sql2o.open()) {
            String sql = "UPDATE monsters SET lastplayed = now() WHERE id = :id";
            con.createQuery(sql)
                    .addParameter("id", id)
                    .executeUpdate();
        }
        playLevel++;
    }
    public void sleep(){
        if (sleepLevel >= MAX_SLEEP_LEVEL){
            throw new UnsupportedOperationException("You cannot make your monster sleep anymore!");
        }
        try(Connection con = DB.sql2o.open()) {
            String sql = "UPDATE monsters SET lastslept = now() WHERE id = :id";
            con.createQuery(sql)
                    .addParameter("id", id)
                    .executeUpdate();
        }
        sleepLevel++;
    }
    public void feed(){
        if (foodLevel >= MAX_FOOD_LEVEL){
            throw new UnsupportedOperationException("You cannot feed your monster anymore!");
        }
        try(Connection con = DB.sql2o.open()) {
            String sql = "UPDATE monsters SET lastate = now() WHERE id = :id";
            con.createQuery(sql)
                    .addParameter("id", id)
                    .executeUpdate();
        }
        foodLevel++;
    }
    public void save() {
        try(Connection con = DB.sql2o.open()) {
            String sql = "INSERT INTO monsters (name, personId, birthday) VALUES (:name, :personId, now())";
            this.id = (int) con.createQuery(sql, true)
                    .addParameter("name", this.name)
                    .addParameter("personId", this.personId)
                    .executeUpdate()
                    .getKey();
        }
    }
    public Timestamp getBirthday(){
        return birthday;
    }
    public Timestamp getLastSlept(){
        return lastSlept;
    }
    public Timestamp getLastAte(){
        return lastAte;
    }
    public Timestamp getLastPlayed(){
        return lastPlayed;
    }
    public void startTimer(){
        Monster currentMonster = this;
        TimerTask timerTask = new TimerTask(){
            @Override
            public void run() {
                if (!currentMonster.isAlive()){
                    cancel();
                }
                depleteLevels();
            }
        };
        this.timer.schedule(timerTask, 0, 600);
    }
}
