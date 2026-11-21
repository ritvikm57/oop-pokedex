/*
 * Gotta Class 'Em All — OOP through Pokémon
 * CS3101 OOPS Illustration Assignment
 *
 * Every OOP idea on the website maps to real code in this file.
 * Compile & run:   javac PokedexOOP.java && java PokedexOOP
 */

import java.util.ArrayList;
import java.util.List;

// ─────────────────────────────────────────────────────────────
// 1. ABSTRACTION — "Pokemon" is an idea, not a creature.
//    You can never meet a plain Pokemon in the wild:
//    new Pokemon(...)  →  compile error: Pokemon is abstract
// ─────────────────────────────────────────────────────────────
abstract class Pokemon {

    // 2. ENCAPSULATION — state is private; the outside world
    //    goes through methods that protect the rules.
    private String owner;                  // null while wild
    private final int maxHp;
    private int hp;
    private int level;

    // 3. STATIC — one counter shared by the whole species tree,
    //    not owned by any single Pokémon. This is the Pokédex.
    private static int seenCount = 0;

    protected Pokemon(int level, int maxHp) {
        this.level = level;                // 'this' = the object being built
        this.maxHp = maxHp;
        this.hp = maxHp;
        seenCount++;
    }

    // Abstract methods: every species MUST say how it does these.
    public abstract String species();
    public abstract String type();
    protected abstract String signatureMove();
    protected abstract int basePower();

    // 4. POLYMORPHISM (runtime) — attack() is written once here,
    //    but signatureMove()/basePower()/effectivenessAgainst()
    //    are resolved by the ACTUAL object at runtime.
    public void attack(Pokemon target) {
        double mult = effectivenessAgainst(target);
        int dmg = (int) Math.round(basePower() * mult * (1 + level / 50.0));
        System.out.printf("  %s used %s! ", getName(), signatureMove());
        if (mult > 1) System.out.print("It's super effective! ");
        if (mult < 1) System.out.print("It's not very effective... ");
        target.takeDamage(dmg);
        System.out.printf("(%s: %d/%d HP)%n", target.getName(), target.getHp(), target.maxHp);
    }

    // Default: neutral. Subclasses OVERRIDE to change behaviour.
    protected double effectivenessAgainst(Pokemon target) {
        return 1.0;
    }

    // The ONLY way HP goes down — and it can never go below 0.
    public void takeDamage(int amount) {
        if (amount < 0) throw new IllegalArgumentException("Damage can't be negative");
        hp = Math.max(0, hp - amount);
    }

    // 5. OVERLOADING (compile-time polymorphism) — same name,
    //    different parameter lists. The compiler picks one.
    public void heal() {                       // Pokémon Center: full heal
        hp = maxHp;
    }
    public void heal(int amount) {             // a Potion
        hp = Math.min(maxHp, hp + amount);
    }
    public void heal(Berry berry) {            // a Berry (object parameter)
        heal(berry.restoreAmount());
    }

    public boolean isFainted() { return hp == 0; }

    // Getters only — no setHp(). Read-only from outside.
    // "Wild Pikachu" until caught, then "Ritvik's Pikachu"
    public String getName() {
        return (owner == null ? "Wild " : owner + "'s ") + species();
    }
    public boolean isWild()     { return owner == null; }

    // package-private: only Trainer (same package) can claim a Pokémon,
    // and a caught Pokémon can't be stolen.
    boolean claimBy(String trainerName) {
        if (owner != null) return false;
        owner = trainerName;
        return true;
    }
    public int getHp()          { return hp; }
    public int getLevel()       { return level; }
    public static int getSeenCount() { return seenCount; }

    public void levelUp() { level++; }

    @Override
    public String toString() {
        return String.format("%s [%s] Lv.%d  HP %d/%d",
                getName(), type(), level, hp, maxHp);
    }
}

// ─────────────────────────────────────────────────────────────
// 6. INHERITANCE — type classes sit between Pokemon and species.
//    FireType IS-A Pokemon. Charmander IS-A FireType IS-A Pokemon.
// ─────────────────────────────────────────────────────────────
abstract class FireType extends Pokemon {
    protected FireType(int lvl, int hp) { super(lvl, hp); }
    @Override public String type() { return "Fire"; }
    @Override protected double effectivenessAgainst(Pokemon t) {
        if (t instanceof WaterType)  return 0.5;
        if (t instanceof DragonType) return 0.5;
        return 1.0;
    }
}

abstract class WaterType extends Pokemon {
    protected WaterType(int lvl, int hp) { super(lvl, hp); }
    @Override public String type() { return "Water"; }
    @Override protected double effectivenessAgainst(Pokemon t) {
        if (t instanceof FireType)   return 2.0;
        if (t instanceof DragonType) return 0.5;
        return 1.0;
    }
}

abstract class DragonType extends Pokemon {
    protected DragonType(int lvl, int hp) { super(lvl, hp); }
    @Override public String type() { return "Dragon"; }
    @Override protected double effectivenessAgainst(Pokemon t) {
        if (t instanceof DragonType) return 2.0;
        return 1.0;
    }
}

abstract class ElectricType extends Pokemon {
    protected ElectricType(int lvl, int hp) { super(lvl, hp); }
    @Override public String type() { return "Electric"; }
    @Override protected double effectivenessAgainst(Pokemon t) {
        if (t instanceof WaterType)  return 2.0;
        if (t instanceof DragonType) return 0.5;
        // NOTE: we don't check "instanceof Flyable" here. Being able to
        // learn Fly is an ability, not a type: Gyarados is Flying-type
        // but can't learn Fly. Type and ability are separate concepts.
        return 1.0;
    }
}

// ─────────────────────────────────────────────────────────────
// 7. INTERFACES — abilities that cut ACROSS the type tree.
//    A class extends ONE parent but can implement MANY interfaces.
// ─────────────────────────────────────────────────────────────
interface Flyable {
    String fly(String destination);
}

interface Surfable {
    String surf();
    default String describeRide() {           // default method (Java 8+)
        return "a smooth ride across the water";
    }
}

// ─── Concrete species (final leaves of the tree) ─────────────
class Pikachu extends ElectricType {
    Pikachu(int level) { super(level, 35); }
    @Override public String species()         { return "Pikachu"; }
    @Override protected String signatureMove(){ return "Thunderbolt"; }
    @Override protected int basePower()       { return 12; }
}

class Charmander extends FireType {
    Charmander(int level) { super(level, 39); }
    @Override public String species()         { return "Charmander"; }
    @Override protected String signatureMove(){ return "Ember"; }
    @Override protected int basePower()       { return 10; }
}

class Charmeleon extends FireType {
    Charmeleon(int level) { super(level, 58); }
    @Override public String species()         { return "Charmeleon"; }
    @Override protected String signatureMove(){ return "Fire Fang"; }
    @Override protected int basePower()       { return 14; }
}

class Charizard extends FireType implements Flyable {
    Charizard(int level) { super(level, 78); }
    @Override public String species()         { return "Charizard"; }
    @Override protected String signatureMove(){ return "Flamethrower"; }
    @Override protected int basePower()       { return 18; }
    @Override public String fly(String dest)  { return getName() + " flies you to " + dest + "!"; }
}

class Squirtle extends WaterType {
    Squirtle(int level) { super(level, 44); }
    @Override public String species()         { return "Squirtle"; }
    @Override protected String signatureMove(){ return "Water Gun"; }
    @Override protected int basePower()       { return 10; }
}

// Gyarados can Surf, but (despite being part Flying-type) it can't learn Fly.
class Gyarados extends WaterType implements Surfable {
    Gyarados(int level) { super(level, 95); }
    @Override public String species()         { return "Gyarados"; }
    @Override protected String signatureMove(){ return "Hydro Pump"; }
    @Override protected int basePower()       { return 20; }
    @Override public String surf()            { return getName() + " carries you on " + describeRide() + "."; }
}

// Dragonite: ONE parent class, TWO interfaces.
class Dragonite extends DragonType implements Flyable, Surfable {
    Dragonite(int level) { super(level, 91); }
    @Override public String species()         { return "Dragonite"; }
    @Override protected String signatureMove(){ return "Dragon Claw"; }
    @Override protected int basePower()       { return 20; }
    @Override public String fly(String dest)  { return getName() + " flies you to " + dest + "!"; }
    @Override public String surf()            { return getName() + " carries you on " + describeRide() + "."; }
}

// A plain helper object passed into an overloaded method.
class Berry {
    private final String name;
    private final int restore;
    Berry(String name, int restore) { this.name = name; this.restore = restore; }
    int restoreAmount() { return restore; }
    @Override public String toString() { return name + " Berry"; }
}

// ─────────────────────────────────────────────────────────────
// 8. COMPOSITION (HAS-A) — a Trainer is NOT a Pokémon.
//    A Trainer HAS a party of Pokémon.
// ─────────────────────────────────────────────────────────────
class Trainer {
    private final String name;
    private final List<Pokemon> party = new ArrayList<>();   // has-a
    private static final int MAX_PARTY = 6;

    Trainer(String name) { this.name = name; }

    public boolean catchPokemon(Pokemon p) {
        if (party.size() >= MAX_PARTY) return false;          // rule enforced in one place
        if (!p.claimBy(name)) return false;                   // already someone else's
        party.add(p);
        return true;
    }

    // 9. EVOLUTION is NOT inheritance. A Java object can never change its
    //    class, so evolving builds a NEW object and swaps it into the slot.
    //    Charmander, Charmeleon and Charizard are siblings under FireType.
    public Pokemon evolve(int slot) {
        Pokemon old = party.get(slot);
        Pokemon next;
        if (old instanceof Charmander)      next = new Charmeleon(old.getLevel());
        else if (old instanceof Charmeleon) next = new Charizard(old.getLevel());
        else return old;                                      // final stage
        next.claimBy(name);                                   // still yours
        party.set(slot, next);
        return next;
    }

    public List<Pokemon> getParty() { return party; }
    public String getName() { return name; }
}

// ─────────────────────────────────────────────────────────────
//                          DEMO
// ─────────────────────────────────────────────────────────────
public class PokedexOOP {
    public static void main(String[] args) {
        System.out.println("=== 1. Objects from classes ===");
        Trainer ritvik = new Trainer("Ritvik");
        Pokemon pika = new Pikachu(12);
        System.out.println("  before catch: " + pika.getName());
        ritvik.catchPokemon(pika);
        System.out.println("  after catch:  " + pika.getName());
        ritvik.catchPokemon(new Charizard(36));
        ritvik.catchPokemon(new Gyarados(30));
        ritvik.catchPokemon(new Dragonite(55));
        for (Pokemon p : ritvik.getParty()) System.out.println("  " + p);
        // Pokemon ghost = new Pokemon("??", 1, 1);  // ✗ Pokemon is abstract

        System.out.println("\n=== 2. Static: one Pokédex \"seen\" counter for everyone ===");
        System.out.println("  Pokemon.getSeenCount() = " + Pokemon.getSeenCount());

        System.out.println("\n=== 3. Runtime polymorphism: same call, different behaviour ===");
        Pokemon wild = new Squirtle(15);
        for (Pokemon p : ritvik.getParty()) {        // declared type: Pokemon
            if (!wild.isFainted()) p.attack(wild); // actual type decides
        }

        System.out.println("\n=== 4. Encapsulation: HP is protected ===");
        // wild.hp = 9999;            // ✗ hp has private access in Pokemon
        System.out.println("  " + wild.getName() + " HP = " + wild.getHp() + "  (never below 0)");

        System.out.println("\n=== 5. Overloading: heal(), heal(int), heal(Berry) ===");
        wild.heal(10);
        System.out.println("  after Potion(10):     " + wild.getHp());
        wild.heal(new Berry("Oran", 10));
        System.out.println("  after Oran Berry:     " + wild.getHp());
        wild.heal();
        System.out.println("  after Pokémon Center: " + wild.getHp());

        System.out.println("\n=== 6. Interfaces: abilities across the type tree ===");
        for (Pokemon p : ritvik.getParty()) {
            if (p instanceof Flyable f)  System.out.println("  [Flyable]  " + f.fly("Cerulean City"));
            if (p instanceof Surfable s) System.out.println("  [Surfable] " + s.surf());
        }

        System.out.println("\n=== 7. Upcasting & instanceof ===");
        Pokemon p = new Charizard(40);
        System.out.println("  p instanceof Pokemon  → " + (p instanceof Pokemon));
        System.out.println("  p instanceof FireType → " + (p instanceof FireType));
        System.out.println("  p instanceof Flyable  → " + (p instanceof Flyable));
        System.out.println("  p instanceof WaterType→ " + (p instanceof WaterType));
        System.out.println("  Pokédex seen so far: " + Pokemon.getSeenCount());

        System.out.println("\n=== 8. Evolution: a NEW object at every stage ===");
        ritvik.catchPokemon(new Charmander(16));
        int slot = ritvik.getParty().size() - 1;
        Pokemon first = ritvik.getParty().get(slot);
        System.out.println("  " + first);
        Pokemon mid = ritvik.evolve(slot);
        System.out.println("  → " + mid);
        Pokemon last = ritvik.evolve(slot);
        System.out.println("  → " + last);
        System.out.println("  first == last ? " + (first == last) + "  (different objects, same owner & level)");
    }
}
