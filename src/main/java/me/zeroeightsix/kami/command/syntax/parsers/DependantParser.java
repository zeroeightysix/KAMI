package me.zeroeightsix.kami.command.syntax.parsers;

import me.zeroeightsix.kami.command.syntax.SyntaxChunk;

public class DependantParser extends AbstractParser {

    int dependantIndex;
    private Dependency dependancy;

    public DependantParser(int dependantIndex, Dependency dependancy) {
        this.dependantIndex = dependantIndex;
        this.dependancy = dependancy;
    }

    protected String getDefaultChunk(SyntaxChunk chunk) {
        return dependancy.getEscape();
    }

    @Override
    public String getChunk(SyntaxChunk[] chunks, SyntaxChunk thisChunk, String[] values, String chunkValue) {
        if (chunkValue != null && !chunkValue.equals("")) return "";
        if (values.length<=dependantIndex) return getDefaultChunk(thisChunk);
        if (values[dependantIndex] == null || values[dependantIndex].equals("")){
            return "";
        }

        return dependancy.feed(values[dependantIndex]);
    }

    public static class Dependency {
        String[][] map = new String[][]{};
        String escape;

        public Dependency(String[][] map, String escape) {
            this.map = map;
            this.escape = escape;
        }

        private String[] containsKey(String[][] map, String key){
            for (String[] s : map){
                if (s[0].equals(key))
                        return s;
            }
            return null;
        }

        public String feed(String food){
            String[] entry = containsKey(map, food);
            if (entry != null)
                return entry[1];
            return getEscape();
        }

        public String[][] getMap() {
            return map;
        }

        public String getEscape() {
            return escape;
        }
    }
}
