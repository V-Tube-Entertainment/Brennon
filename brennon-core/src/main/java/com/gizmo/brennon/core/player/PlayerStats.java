package com.gizmo.brennon.core.player;

public class PlayerStats {
    private int kills;
    private int deaths;
    private int wins;
    private int losses;
    private int gamesPlayed;
    private double currency;
    private int tokens;

    public PlayerStats() {
        this.kills = 0;
        this.deaths = 0;
        this.wins = 0;
        this.losses = 0;
        this.gamesPlayed = 0;
        this.currency = 0.0;
        this.tokens = 0;
    }

    public void copyFrom(PlayerStats other) {
        this.kills = other.kills;
        this.deaths = other.deaths;
        this.wins = other.wins;
        this.losses = other.losses;
        this.gamesPlayed = other.gamesPlayed;
        this.currency = other.currency;
        this.tokens = other.tokens;
    }

    // Getters and Setters
    public int getKills() { return kills; }
    public void setKills(int kills) { this.kills = kills; }
    public void incrementKills() { this.kills++; }

    public int getDeaths() { return deaths; }
    public void setDeaths(int deaths) { this.deaths = deaths; }
    public void incrementDeaths() { this.deaths++; }

    public int getWins() { return wins; }
    public void setWins(int wins) { this.wins = wins; }
    public void incrementWins() { this.wins++; }

    public int getLosses() { return losses; }
    public void setLosses(int losses) { this.losses = losses; }
    public void incrementLosses() { this.losses++; }

    public int getGamesPlayed() { return gamesPlayed; }
    public void setGamesPlayed(int gamesPlayed) { this.gamesPlayed = gamesPlayed; }
    public void incrementGamesPlayed() { this.gamesPlayed++; }

    public double getCurrency() { return currency; }
    public void setCurrency(double currency) { this.currency = currency; }
    public void addCurrency(double amount) { this.currency += amount; }
    public void removeCurrency(double amount) { this.currency = Math.max(0, this.currency - amount); }

    public int getTokens() { return tokens; }
    public void setTokens(int tokens) { this.tokens = tokens; }
    public void addTokens(int amount) { this.tokens += amount; }
    public void removeTokens(int amount) { this.tokens = Math.max(0, this.tokens - amount); }

    public double getKDRatio() {
        return deaths == 0 ? kills : (double) kills / deaths;
    }

    public double getWLRatio() {
        return losses == 0 ? wins : (double) wins / losses;
    }
}