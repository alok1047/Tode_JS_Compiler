package thunderjs.runtime;

import java.util.UUID;

public class JSSymbol {
    private final String description;
    private final String id;

    public JSSymbol(String description) {
        this.description = description;
        this.id = UUID.randomUUID().toString();
    }

    public String getDescription() {
        return description;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        JSSymbol other = (JSSymbol) obj;
        return this.id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return "Symbol(" + (description == null ? "" : description) + ")";
    }
}
