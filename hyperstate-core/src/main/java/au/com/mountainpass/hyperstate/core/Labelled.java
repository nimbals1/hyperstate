package au.com.mountainpass.hyperstate.core;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.Nullable;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import au.com.mountainpass.hyperstate.server.serialization.MessageSourceAwareSerializer;

abstract public class Labelled {

    @Nullable
    String label = null;

    private Set<String> natures = new HashSet<>();

    public Labelled() {
    }

    public Labelled(final Labelled labelled) {
        this();
        this.label = labelled.label;
        this.natures = labelled.natures;
    }

    public Labelled(final String label, final Set<String> natures) {
        this.label = label;
        this.natures = natures;
    }

    public Labelled(final String label, final String... natures) {
        this.label = label;
        this.natures.addAll(Arrays.asList(natures));
    }

    /**
     * @return the label
     */
    // TODO: use mixin here
    @JsonSerialize(using = MessageSourceAwareSerializer.class)
    @JsonProperty("title")
    public String getLabel() {
        return label;
    }

    /**
     * @return the natures
     */
    @JsonProperty("class")
    public Set<String> getNatures() {
        return natures;
    }

    public boolean hasNature(final String nature) {
        return this.getNatures().contains(nature);
    }

    private String interpolate(final String value, final String... args) {
        if (args.length == 0) {
            return value;
        } else {
            final Pattern patt = Pattern.compile("\\{(.*?)\\}");
            final Matcher m = patt.matcher(value);
            final StringBuffer sb = new StringBuffer(value.length());
            for (int i = 0; m.find(); ++i) {
                final String code = m.group(1);
                m.appendReplacement(sb, Matcher.quoteReplacement(args[i]));
            }
            m.appendTail(sb);
            return sb.toString();
        }
    }

    void setLabel(final String template, final String... args) {
        label = interpolate(template, args);
    }

    /**
     * @param natures
     *            the natures to set
     */
    @JsonProperty("class")
    public void setNatures(final Set<String> natures) {
        this.natures = natures;
    }

    /**
     * @param title
     *            the label to set
     */
    public void setTitle(final String title) {
        this.label = title;
    }

}
