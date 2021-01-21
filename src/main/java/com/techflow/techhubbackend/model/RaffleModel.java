package com.techflow.techhubbackend.model;

import com.google.cloud.Timestamp;

import java.util.*;

public class RaffleModel {

    private String id;
    private Long prize;
    private List<String> entries;
    private Timestamp createTime;
    private Timestamp drawTime;
    private String winner;
    private Boolean isActive;

    public RaffleModel() {
    }

    public RaffleModel(String id, Long prize, List<String> entries, Timestamp createTime, Timestamp drawTime, String winner, Boolean isActive) {
        this.id = id;
        this.prize = prize;
        this.entries = entries;
        this.createTime = createTime;
        this.drawTime = drawTime;
        this.winner = winner;
        this.isActive = isActive;
    }

    public RaffleModel(RaffleModel raffleModel) {
        this.id = raffleModel.id;
        this.prize = raffleModel.prize;
        this.entries = new ArrayList<>(raffleModel.entries);
        this.createTime = raffleModel.createTime;
        this.drawTime = raffleModel.drawTime;
        this.winner = raffleModel.winner;
        this.isActive = raffleModel.isActive;
    }

    public RaffleModel(Map<String, Object> map) {
        this.id = (String) map.getOrDefault("id", "no id");
        this.prize = (Long) map.getOrDefault("prize", 0);
        this.entries = (List<String>) map.getOrDefault("entries", null);
        this.createTime = (Timestamp) map.getOrDefault("createTime", null);
        this.drawTime = (Timestamp) map.getOrDefault("drawTime", null);
        this.winner = (String) map.getOrDefault("winner", "no winner");
        this.isActive = (Boolean) map.getOrDefault("isActive", false);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Long getPrize() {
        return prize;
    }

    public void setPrize(Long prize) {
        this.prize = prize;
    }

    public List<String> getEntries() {
        return entries;
    }

    public void setEntries(List<String> entries) {
        this.entries = entries;
    }

    public Timestamp getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Timestamp createTime) {
        this.createTime = createTime;
    }

    public Timestamp getDrawTime() {
        return drawTime;
    }

    public void setDrawTime(Timestamp drawTime) {
        this.drawTime = drawTime;
    }

    public String getWinner() {
        return winner;
    }

    public void setWinner(String winner) {
        this.winner = winner;
    }

    public Boolean getActive() {
        return isActive;
    }

    public void setActive(Boolean active) {
        isActive = active;
    }

    @Override
    public String toString() {
        return "RaffleModel{" +
                "id='" + id + "'" +
                ", prize='" + prize + "'" +
                ", entries='" + entries + "'" +
                ", createTime='" + createTime + "'" +
                ", drawTime='" + drawTime + "'" +
                ", winner='" + winner + "'" +
                ", isActive='" + isActive + "'" +
                "}";
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, prize, entries, createTime, drawTime, winner, isActive);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null || getClass() != obj.getClass())
            return false;

        RaffleModel raffleModel = (RaffleModel) obj;

        return id.equals(raffleModel.id) &&
                prize.equals(raffleModel.prize) &&
                entries.equals(raffleModel.entries) &&
                createTime.equals(raffleModel.createTime) &&
                drawTime.equals(raffleModel.drawTime) &&
                winner.equals(raffleModel.winner) &&
                isActive.equals(raffleModel.isActive);
    }

    public Map<String, Object> generateMap() {
        HashMap<String, Object> map = new HashMap<>();

        map.put("id", id);
        map.put("prize", prize);
        map.put("entries", entries);
        map.put("createTime", createTime);
        map.put("drawTime", drawTime);
        map.put("winner", winner);
        map.put("isActive", isActive);

        return map;
    }

    public Map<String, Object> generateMap(boolean includeEmptyFields) {
        HashMap<String, Object> map = new HashMap<>();

        if (id != null || includeEmptyFields)
            map.put("id", id);
        if (prize != null || includeEmptyFields)
            map.put("prize", prize);
        if (entries != null || includeEmptyFields)
            map.put("entries", entries);
        if (createTime != null || includeEmptyFields)
            map.put("createTime", createTime);
        if (drawTime != null || includeEmptyFields)
            map.put("drawTime", drawTime);
        if (winner != null || includeEmptyFields)
            map.put("winner", winner);
        if (isActive != null || includeEmptyFields)
            map.put("isActive", isActive);

        return map;
    }
}
