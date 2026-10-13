package l05_oop.lesson10_nested_value_java;

import java.util.Objects;

/**
 * The Java way to write a simple "data holder" (a POJO).
 * Compare it with the ONE line in NestedValueAndJava.kt:
 *
 *     data class Book(val title: String, val pages: Int)
 *
 * (Modern Java has `record`, which is closer to a Kotlin data class,
 * but you will still meet classes like this one in older code.)
 */
public final class JavaBook {
    private final String title;
    private final int pages;

    public JavaBook(String title, int pages) {
        this.title = title;
        this.pages = pages;
    }

    public String getTitle() {
        return title;
    }

    public int getPages() {
        return pages;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof JavaBook)) return false;
        JavaBook book = (JavaBook) other;
        return pages == book.pages && Objects.equals(title, book.title);
    }

    @Override
    public int hashCode() {
        return Objects.hash(title, pages);
    }

    @Override
    public String toString() {
        return "JavaBook{title='" + title + "', pages=" + pages + "}";
    }
}
