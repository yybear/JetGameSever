package com.handwin.game.rhythm.match;

/**
 * 匹配的元素，包含主体对象和可能匹配的对象
 * User: qgan(qgan@v5.cn)
 * Date: 14-6-23 上午10:15
 */
public class PlayerMatchItem {
    private String female;  // 主体玩家

    private String male;   // 匹配玩家

    public PlayerMatchItem(String female, String male) {
        this.female = female;
        this.male = male;
    }

    public String getFemale() {
        return female;
    }

    public void setFemale(String female) {
        this.female = female;
    }

    public String getMale() {
        return male;
    }

    public void setMale(String male) {
        this.male = male;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((female == null) ? 0 : female.hashCode());
        result = prime * result
                + ((male == null) ? 0 : male.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        PlayerMatchItem other = (PlayerMatchItem) obj;
        if (female == null) {
            if (other.female != null)
                return false;
        } else if (!female.equals(other.female))
            return false;
        if (male == null) {
            if (other.male != null)
                return false;
        } else if (!male.equals(other.male))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "PlayerMatchItem{" +
                "female='" + female + '\'' +
                ", male='" + male + '\'' +
                '}';
    }
}
