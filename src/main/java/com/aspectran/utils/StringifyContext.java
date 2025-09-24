/*
 * Copyright (c) 2008-present The Aspectran Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.aspectran.utils;

import com.aspectran.utils.annotation.jsr305.NonNull;
import com.aspectran.utils.annotation.jsr305.Nullable;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * A context that holds options and helpers for converting values to and from strings.
 * <p>This class encapsulates formatting settings such as pretty-printing, indentation,
 * null handling, date/time formats, and {@code Locale}. It provides a consistent
 * way to manage serialization and deserialization options across different components.</p>
 *
 * <p>Created: 2024. 11. 30.</p>
 */
public class StringifyContext implements Cloneable {

    private Boolean prettyPrint;

    private Integer indentSize;

    private Boolean indentTab;

    private Boolean nullWritable;

    private String dateTimeFormat;

    private String dateFormat;

    private String timeFormat;

    private SimpleDateFormat simpleDateFormat;

    private Locale locale;

    /**
     * Instantiates a new Stringify context.
     */
    public StringifyContext() {
    }

    /**
     * Returns whether pretty-printing is enabled.
     * @return true if pretty-printing is set, false otherwise
     */
    public boolean hasPrettyPrint() {
        return (prettyPrint != null);
    }

    /**
     * Returns whether to format the output for readability.
     * @return true if pretty-printing is enabled, false otherwise
     */
    public boolean isPrettyPrint() {
        return BooleanUtils.toBoolean(prettyPrint);
    }

    /**
     * Sets whether to format the output for readability.
     * @param prettyPrint true to enable pretty-printing, false otherwise
     */
    public void setPrettyPrint(Boolean prettyPrint) {
        this.prettyPrint = prettyPrint;
    }

    /**
     * Returns whether the indent size is set.
     * @return true if the indent size is set, false otherwise
     */
    public boolean hasIndentSize() {
        return (indentSize != null);
    }

    /**
     * Returns the number of spaces to use for indentation.
     * @return the indent size, or 0 if not set
     */
    public int getIndentSize() {
        return (indentSize != null ? indentSize : 0);
    }

    /**
     * Sets the number of spaces to use for indentation.
     * @param indentSize the number of spaces
     */
    public void setIndentSize(int indentSize) {
        this.indentSize = indentSize;
    }

    /**
     * Returns whether to use a tab character for indentation.
     * @return true to use a tab, false otherwise
     */
    public boolean isIndentTab() {
        return BooleanUtils.toBoolean(indentTab);
    }

    /**
     * Sets whether to use a tab character for indentation.
     * @param indentTab true to use a tab, false to use spaces
     */
    public void setIndentTab(boolean indentTab) {
        this.indentTab = indentTab;
    }

    /**
     * Returns the indentation string based on the current settings (tab or spaces).
     * @return the indentation string, or {@code null} if no indentation is set
     */
    @Nullable
    public String getIndentString() {
        if (indentTab != null && indentTab) {
            return "\t";
        } else if (getIndentSize() == 1) {
            return " ";
        } else if (getIndentSize() > 0) {
            return StringUtils.repeat(' ', indentSize);
        } else {
            return null;
        }
    }

    /**
     * Returns whether null values should be written.
     * @return true if nulls are writable, false otherwise
     */
    public boolean hasNullWritable() {
        return (nullWritable != null);
    }

    /**
     * Returns whether null values should be written during serialization.
     * @return true if nulls should be written, false otherwise
     */
    public boolean isNullWritable() {
        return BooleanUtils.toBoolean(nullWritable);
    }

    /**
     * Sets whether null values should be written during serialization.
     * @param nullWritable true to write nulls, false to omit them
     */
    public void setNullWritable(Boolean nullWritable) {
        this.nullWritable = nullWritable;
    }

    /**
     * Returns the format string for date-time values.
     * @return the date-time format string
     */
    public String getDateTimeFormat() {
        return dateTimeFormat;
    }

    /**
     * Sets the format string for date-time values.
     * @param dateTimeFormat the date-time format string
     */
    public void setDateTimeFormat(String dateTimeFormat) {
        this.dateTimeFormat = dateTimeFormat;
    }

    /**
     * Returns the format string for date values.
     * @return the date format string
     */
    public String getDateFormat() {
        return dateFormat;
    }

    /**
     * Sets the format string for date values.
     * @param dateFormat the date format string
     */
    public void setDateFormat(String dateFormat) {
        this.dateFormat = dateFormat;
    }

    /**
     * Returns the format string for time values.
     * @return the time format string
     */
    public String getTimeFormat() {
        return timeFormat;
    }

    /**
     * Sets the format string for time values.
     * @param timeFormat the time format string
     */
    public void setTimeFormat(String timeFormat) {
        this.timeFormat = timeFormat;
    }

    /**
     * Sets the {@link SimpleDateFormat} for {@link Date} objects.
     * @param simpleDateFormat the simple date format
     */
    public void setSimpleDateFormat(SimpleDateFormat simpleDateFormat) {
        this.simpleDateFormat = simpleDateFormat;
    }

    /**
     * Returns the locale for formatting.
     * @return the locale
     */
    public Locale getLocale() {
        return locale;
    }

    /**
     * Sets the locale for formatting.
     * This will reset any cached formatters.
     * @param locale the locale
     */
    public void setLocale(Locale locale) {
        this.locale = locale;
    }

    /**
     * Converts a {@link Date} to a string using the configured format.
     * @param date the {@link Date} to format
     * @return the formatted string
     */
    public String toString(Date date) {
        return toString(date, null);
    }

    /**
     * Converts a {@link Date} to a string using the specified format.
     * @param date the {@link Date} to format
     * @param format the format string to use
     * @return the formatted string
     */
    public String toString(Date date, String format) {
        Assert.notNull(date, "date must not be null");
        SimpleDateFormat sdf = touchSimpleDateFormat(format);
        if (sdf != null) {
            return sdf.format(date);
        } else {
            return date.toString();
        }
    }

    /**
     * Converts a string to a {@link Date} using the configured format.
     * @param date the string to parse
     * @return the parsed {@link Date}
     * @throws ParseException if the string cannot be parsed
     */
    public Date toDate(String date) throws ParseException {
        return toDate(date, null);
    }

    /**
     * Converts a string to a {@link Date} using the specified format.
     * @param date the string to parse
     * @param format the format string to use
     * @return the parsed {@link Date}
     * @throws ParseException if the string cannot be parsed
     */
    public Date toDate(String date, String format) throws ParseException {
        Assert.notNull(date, "date must not be null");
        SimpleDateFormat simpleDateFormat = touchSimpleDateFormat(format);
        return simpleDateFormat.parse(date);
    }

    /**
     * Returns a cached or new {@link SimpleDateFormat} for the given format.
     * @param format the format string
     * @return the simple date format
     */
    private SimpleDateFormat touchSimpleDateFormat(String format) {
        if (format != null) {
            return createSimpleDateFormat(format, locale);
        } else if (dateTimeFormat != null && simpleDateFormat == null) {
            simpleDateFormat = createSimpleDateFormat(dateTimeFormat, locale);
        }
        return simpleDateFormat;
    }

    /**
     * Creates a new {@link SimpleDateFormat} for the given format and locale.
     * @param format the format string
     * @param locale the locale
     * @return a new simple date format
     */
    @NonNull
    private SimpleDateFormat createSimpleDateFormat(String format, Locale locale) {
        Assert.notNull(format, "format must not be null");
        if (locale != null) {
            return new SimpleDateFormat(format, locale);
        } else {
            return new SimpleDateFormat(format);
        }
    }

    /**
     * Merges settings from another {@code StringifyContext} into this one.
     * Settings from the other context will only be applied if they are not already
     * set in this context (i.e., non-destructive merge).
     * @param from the context to merge settings from
     */
    public void merge(StringifyContext from) {
        if (prettyPrint == null && from.prettyPrint != null) {
            prettyPrint = from.prettyPrint;
        }
        if (indentSize == null && from.indentSize != null) {
            indentSize = from.indentSize;
        }
        if (indentTab == null && from.indentTab != null) {
            indentTab = from.indentTab;
        }
        if (nullWritable == null && from.nullWritable != null) {
            nullWritable = from.nullWritable;
        }
        if (dateTimeFormat == null && from.dateTimeFormat != null) {
            dateTimeFormat = from.dateTimeFormat;
        }
        if (dateFormat == null && from.dateFormat != null) {
            dateFormat = from.dateFormat;
        }
        if (timeFormat == null && from.timeFormat != null) {
            timeFormat = from.timeFormat;
        }
        if (simpleDateFormat == null && from.simpleDateFormat != null) {
            simpleDateFormat = from.simpleDateFormat;
        }
        if (locale == null && from.locale != null) {
            locale = from.locale;
        }
    }

    @Override
    public StringifyContext clone() {
        try {
            return (StringifyContext)super.clone();
        } catch (CloneNotSupportedException e) {
            // This should never happen, as we are Cloneable
            throw new RuntimeException(e);
        }
    }

    @Override
    public String toString() {
        ToStringBuilder tsb = new ToStringBuilder();
        tsb.append("pretty", prettyPrint);
        tsb.append("indentSize", indentSize);
        tsb.append("indentTab", indentTab);
        tsb.append("nullWritable", nullWritable);
        tsb.append("dateTimeFormat", dateTimeFormat);
        tsb.append("dateFormat", dateFormat);
        tsb.append("timeFormat", timeFormat);
        tsb.append("locale", locale);
        return tsb.toString();
    }

}
