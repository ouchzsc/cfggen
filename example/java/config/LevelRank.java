package config;

public class LevelRank {
    private int level;
    private int rank;
    private config.equip.Rank RefRank;

    private LevelRank() {
    }

    public LevelRank(int level, int rank) {
        this.level = level;
        this.rank = rank;
    }

    public static LevelRank _create(configgen.genjava.ConfigInput input) {
        LevelRank self = new LevelRank();
        self.level = input.readInt();
        self.rank = input.readInt();
        return self;
    }

    /**
     * 等级
     */
    public int getLevel() {
        return level;
    }

    /**
     * 品质
     */
    public int getRank() {
        return rank;
    }

    public config.equip.Rank refRank() {
        return RefRank;
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(level, rank);
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof LevelRank))
            return false;
        LevelRank o = (LevelRank) other;
        return level == o.level && rank == o.rank;
    }

    @Override
    public String toString() {
        return "(" + level + "," + rank + ")";
    }

    public void _resolveDirect(config.ConfigMgr mgr) {
        RefRank = config.equip.Rank.get(rank);
        java.util.Objects.requireNonNull(RefRank);
    }

    public void _resolve(config.ConfigMgr mgr) {
        _resolveDirect(mgr);
    }

}
