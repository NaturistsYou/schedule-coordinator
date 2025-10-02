package NaturistsYou.coordinator.enums;

public enum ResponseType {
    OK("参加可能", "○"),
    MAYBE("微妙", "△"),
    NG("参加不可", "×");

    private final String description;
    private final String symbol;

    ResponseType(String description, String symbol) {
        this.description = description;
        this.symbol = symbol;
    }

    public String getDescription() {
        return description;
    }

    public String getSymbol() {
        return symbol;
    }

    // 文字列から変換するためのメソッド
    public static ResponseType fromString(String value) {
        for (ResponseType type : ResponseType.values()) {
            if (type.name().equalsIgnoreCase(value) ||
                    type.getSymbol().equals(value)) {
                return type;
            }
        }
        return NG; // デフォルト値
    }
}
