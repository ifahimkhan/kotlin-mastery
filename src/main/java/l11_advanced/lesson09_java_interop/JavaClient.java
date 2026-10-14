package l11_advanced.lesson09_java_interop;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import l11_advanced.lesson09_java_interop.api.Thermostat;
import l11_advanced.lesson09_java_interop.api.Thermostats;

/**
 * Plain Java code that uses the Kotlin API in api/Thermostats.kt.
 * Lesson 11.9 calls {@link #run()} and prints what Java saw.
 */
public final class JavaClient {
    private JavaClient() {
    }

    public static List<String> run() {
        List<String> lines = new ArrayList<>();

        // @file:JvmName("Thermostats") -> a static method on Thermostats
        lines.add("Java: Thermostats.toFahrenheit(100) = " + Thermostats.toFahrenheit(100.0));
        // @JvmName("describeRoom") -> Java uses the new name
        lines.add("Java: Thermostats.describeRoom(\"lab\") = " + Thermostats.describeRoom("lab"));

        // @JvmOverloads -> three constructors exist for Java
        lines.add("Java: new Thermostat(\"hall\") = " + new Thermostat("hall"));
        lines.add("Java: new Thermostat(\"hall\", 19) = " + new Thermostat("hall", 19));

        // @JvmStatic and @JvmField on the companion object
        lines.add("Java: Thermostat.forOffice() = " + Thermostat.forOffice());
        lines.add("Java: Thermostat.MAX_TARGET = " + Thermostat.MAX_TARGET);

        // A Kotlin String? is just a String in Java: Java must check for null itself.
        String room = Thermostats.findRoom(3);
        lines.add("Java: findRoom(3) = " + (room == null ? "null, Java had to check" : room));

        // @Throws(IOException.class) -> a checked exception Java must catch
        try {
            Thermostats.loadProfile("missing");
        } catch (IOException e) {
            lines.add("Java: caught IOException: " + e.getMessage());
        }
        return lines;
    }
}
