package l04_null_safety.lesson06_platform;

/**
 * A tiny "legacy" Java class used by lesson 04.6.
 *
 * Java has no nullable types: nothing in this signature tells Kotlin that
 * {@link #findEmail(int)} may return null. Kotlin therefore sees the return
 * type as a "platform type" written {@code String!}.
 */
public class LegacyUserDirectory {

    /** Returns the email of the user, or null when the id is unknown. */
    public String findEmail(int id) {
        if (id == 1) {
            return "linus@example.com";
        }
        return null;
    }
}
