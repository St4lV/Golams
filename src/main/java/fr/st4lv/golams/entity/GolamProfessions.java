package fr.st4lv.golams.entity;

import java.util.Arrays;
import java.util.Comparator;

public enum GolamProfessions {
    UNASSIGNED(0, "unassigned"),
    BLACKSMITH(1, "blacksmith"),
    CARTOGRAPHER(2, "cartographer"),
    DELIVERER(3, "deliverer"),
    GUARD(4, "guard")/*,
    GATHERER(4,"gatherer")*/
    ;
    private static final GolamProfessions[] BY_ID = Arrays.stream(values())
            .sorted(Comparator.comparingInt(GolamProfessions::getId))
            .toArray(GolamProfessions[]::new);

    private final int id;
    private final String professionName;

    GolamProfessions(int id, String professionName) {
        this.id = id;
        this.professionName = professionName;
    }
    public static int getIndex(String professionName){
        return byName(professionName).getId();
    };
    public int getId() {
        return id;
    }

    public String getProfessionName() {
        return professionName;
    }

    public static GolamProfessions byId(int id) {
        return BY_ID[id % BY_ID.length];
    }

    public static GolamProfessions byName(String name) {
        return Arrays.stream(values())
                .filter(p -> p.professionName.equalsIgnoreCase(name))
                .findFirst()
                .orElse(UNASSIGNED);
    }
}
