package com.noahtnt2009.gallifreyan_chronicles.client.watermark;

import java.util.HashMap;
import java.util.Map;

public final class WatermarkFont {
    public static final int GLYPH_WIDTH = 5;
    public static final int GLYPH_HEIGHT = 7;
    public static final int ADVANCE = GLYPH_WIDTH + 1;

    private static final String SOURCE = """
            0
            .###.
            #...#
            #..##
            #.#.#
            ##..#
            #...#
            .###.

            1
            ..#..
            .##..
            ..#..
            ..#..
            ..#..
            ..#..
            .###.

            2
            .###.
            #...#
            ....#
            ...#.
            ..#..
            .#...
            #####

            3
            #####
            ...#.
            ..#..
            ...#.
            ....#
            #...#
            .###.

            4
            ...#.
            ..##.
            .#.#.
            #..#.
            #####
            ...#.
            ...#.

            5
            #####
            #....
            ####.
            ....#
            ....#
            #...#
            .###.

            6
            ..##.
            .#...
            #....
            ####.
            #...#
            #...#
            .###.

            7
            #####
            ....#
            ...#.
            ..#..
            .#...
            .#...
            .#...

            8
            .###.
            #...#
            #...#
            .###.
            #...#
            #...#
            .###.

            9
            .###.
            #...#
            #...#
            .####
            ....#
            ...#.
            .##..

            A
            .###.
            #...#
            #...#
            #####
            #...#
            #...#
            #...#

            B
            ####.
            #...#
            #...#
            ####.
            #...#
            #...#
            ####.

            C
            .###.
            #...#
            #....
            #....
            #....
            #...#
            .###.

            D
            ###..
            #..#.
            #...#
            #...#
            #...#
            #..#.
            ###..

            E
            #####
            #....
            #....
            ####.
            #....
            #....
            #####

            F
            #####
            #....
            #....
            ####.
            #....
            #....
            #....

            G
            .###.
            #...#
            #....
            #.###
            #...#
            #...#
            .####

            H
            #...#
            #...#
            #...#
            #####
            #...#
            #...#
            #...#

            I
            .###.
            ..#..
            ..#..
            ..#..
            ..#..
            ..#..
            .###.

            J
            ..###
            ...#.
            ...#.
            ...#.
            ...#.
            #..#.
            .##..

            K
            #...#
            #..#.
            #.#..
            ##...
            #.#..
            #..#.
            #...#

            L
            #....
            #....
            #....
            #....
            #....
            #....
            #####

            M
            #...#
            ##.##
            #.#.#
            #.#.#
            #...#
            #...#
            #...#

            N
            #...#
            ##..#
            #.#.#
            #..##
            #...#
            #...#
            #...#

            O
            .###.
            #...#
            #...#
            #...#
            #...#
            #...#
            .###.

            P
            ####.
            #...#
            #...#
            ####.
            #....
            #....
            #....

            Q
            .###.
            #...#
            #...#
            #...#
            #.#.#
            #..#.
            .##.#

            R
            ####.
            #...#
            #...#
            ####.
            #.#..
            #..#.
            #...#

            S
            .####
            #....
            #....
            .###.
            ....#
            ....#
            ####.

            T
            #####
            ..#..
            ..#..
            ..#..
            ..#..
            ..#..
            ..#..

            U
            #...#
            #...#
            #...#
            #...#
            #...#
            #...#
            .###.

            V
            #...#
            #...#
            #...#
            #...#
            #...#
            .#.#.
            ..#..

            W
            #...#
            #...#
            #...#
            #.#.#
            #.#.#
            ##.##
            #...#

            X
            #...#
            #...#
            .#.#.
            ..#..
            .#.#.
            #...#
            #...#

            Y
            #...#
            #...#
            .#.#.
            ..#..
            ..#..
            ..#..
            ..#..

            Z
            #####
            ....#
            ...#.
            ..#..
            .#...
            #....
            #####

            _
            .....
            .....
            .....
            .....
            .....
            .....
            #####

            -
            .....
            .....
            .....
            #####
            .....
            .....
            .....

            .
            .....
            .....
            .....
            .....
            .....
            .##..
            .##..

            :
            .....
            .##..
            .##..
            .....
            .##..
            .##..
            .....

            ?
            .###.
            #...#
            ....#
            ...#.
            ..#..
            .....
            ..#..
            """;

    private static final Map<Character, int[]> GLYPHS = parse();
    private static final int[] BLANK = new int[GLYPH_HEIGHT];

    private WatermarkFont() {
    }

    private static Map<Character, int[]> parse() {
        Map<Character, int[]> glyphs = new HashMap<>();
        for (String block : SOURCE.strip().split("\n\n")) {
            String[] lines = block.strip().split("\n");
            int[] rows = new int[GLYPH_HEIGHT];
            for (int r = 0; r < GLYPH_HEIGHT; r++) {
                String line = lines[r + 1].strip();
                for (int c = 0; c < GLYPH_WIDTH; c++) {
                    if (line.charAt(c) == '#') rows[r] |= 1 << (GLYPH_WIDTH - 1 - c);
                }
            }
            glyphs.put(lines[0].strip().charAt(0), rows);
        }
        return glyphs;
    }

    public static int[] glyph(char c) {
        if (c == ' ') return BLANK;
        int[] rows = GLYPHS.get(c);
        return rows != null ? rows : GLYPHS.get('?');
    }
}
