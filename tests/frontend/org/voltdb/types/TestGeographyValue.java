/* This file is part of VoltDB.
 * Copyright (C) 2008-2015 VoltDB Inc.
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS BE LIABLE FOR ANY CLAIM, DAMAGES OR
 * OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
 * ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 */

package org.voltdb.types;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;

import junit.framework.TestCase;

public class TestGeographyValue extends TestCase {

    public void testBasic() {
        GeographyValue geog;
        // The Bermuda Triangle
        List<PointType> outerLoop = Arrays.asList(
                new PointType(32.305, -64.751),
                new PointType(25.244, -80.437),
                new PointType(18.476, -66.371),
                new PointType(32.305, -64.751));

        // A triangular hole
        List<PointType> innerLoop = Arrays.asList(
                new PointType(28.066, -68.874),
                new PointType(25.361, -68.855),
                new PointType(28.376, -73.381),
                new PointType(28.066, -68.874));

        geog = new GeographyValue(Arrays.asList(outerLoop, innerLoop));
        assertEquals("POLYGON((32.305 -64.751, 25.244 -80.437, 18.476 -66.371, 32.305 -64.751), "
                + "(28.066 -68.874, 25.361 -68.855, 28.376 -73.381, 28.066 -68.874))",
                geog.toString());

        // round trip
        geog = new GeographyValue("POLYGON((32.305 -64.751, 25.244 -80.437, 18.476 -66.371, 32.305 -64.751), "
                + "(28.066 -68.874, 25.361 -68.855, 28.376 -73.381, 28.066 -68.874))");
        assertEquals("POLYGON((32.305 -64.751, 25.244 -80.437, 18.476 -66.371, 32.305 -64.751), "
                + "(28.066 -68.874, 25.361 -68.855, 28.376 -73.381, 28.066 -68.874))",
                geog.toString());

        ByteBuffer buf = ByteBuffer.allocate(geog.getLengthInBytes());
        geog.flattenToBuffer(buf);
        assertEquals(270, buf.position());

        buf.position(0);
        GeographyValue newGeog = GeographyValue.unflattenFromBuffer(buf);
        assertEquals("POLYGON((32.305 -64.751, 25.244 -80.437, 18.476 -66.371, 32.305 -64.751), "
                + "(28.066 -68.874, 25.361 -68.855, 28.376 -73.381, 28.066 -68.874))",
                newGeog.toString());
        assertEquals(270, buf.position());

        // Try the absolute version of unflattening
        buf.position(77);
        newGeog = GeographyValue.unflattenFromBuffer(buf, 0);
        assertEquals("POLYGON((32.305 -64.751, 25.244 -80.437, 18.476 -66.371, 32.305 -64.751), "
                + "(28.066 -68.874, 25.361 -68.855, 28.376 -73.381, 28.066 -68.874))",
                newGeog.toString());
        assertEquals(77, buf.position());
    }

    private static String canonicalizeWkt(String wkt) {
        return (new GeographyValue(wkt)).toString();
    }

    public void testWktParsingPositive() {

        // Parsing is case-insensitive
        String expected = "POLYGON((32.305 -64.751, 25.244 -80.437, 18.476 -66.371, 32.305 -64.751))";
        assertEquals(expected, canonicalizeWkt("Polygon((32.305 -64.751, 25.244 -80.437, 18.476 -66.371, 32.305 -64.751))"));
        assertEquals(expected, canonicalizeWkt("polygon((32.305 -64.751, 25.244 -80.437, 18.476 -66.371, 32.305 -64.751))"));
        assertEquals(expected, canonicalizeWkt("PoLyGoN((32.305 -64.751, 25.244 -80.437, 18.476 -66.371, 32.305 -64.751))"));

        // Parsing is whitespace-insensitive
        assertEquals(expected, canonicalizeWkt("  POLYGON  (  (  32.305  -64.751  ,  25.244  -80.437  ,  18.476  -66.371  ,  32.305   -64.751  )  ) "));
        assertEquals(expected, canonicalizeWkt("\nPOLYGON\n(\n(\n32.305\n-64.751\n,\n25.244\n-80.437\n,\n18.476\n-66.371\n,32.305\n-64.751\n)\n)\n"));
        assertEquals(expected, canonicalizeWkt("\tPOLYGON\t(\t(\t32.305\t-64.751\t,\t25.244\t-80.437\t,\t18.476\t-66.371\t,\t32.305\t-64.751\t)\t)\t"));

        // Parsing with more than one loop should work the same.
        expected = "POLYGON((32.305 -64.751, 25.244 -80.437, 18.476 -66.371, 32.305 -64.751), "
                + "(28.066 -68.874, 25.361 -68.855, 28.376 -73.381, 28.066 -68.874))";
        assertEquals(expected, canonicalizeWkt("PoLyGoN\t(  (\n32.305\n-64.751   ,    25.244\t-80.437\n,18.476-66.371,32.305\t\t\t-64.751   ),\t "
                + "(\n28.066-68.874,\t25.361    -68.855\n,28.376      -73.381,28.066\n\n-68.874\t)\n)\t"));
    }

    private void assertWktParseError(String error, String wkt) {
        try {
            new GeographyValue(wkt);
            fail("Expected an expection parsing WKT, but it didn't happen");
        }
        catch (IllegalArgumentException iae) {
            assertTrue("Did not find \n"
                    + "  \"" + error + "\"\n"
                    + "in exception message\n"
                    + "  \"" + iae.getMessage() + "\"\n",
                    iae.getMessage().contains(error));
        }
    }

    public void testWktParsingNegative() {
        assertWktParseError("expected WKT to start with POLYGON", "NOT_A_POLYGON(...)");
        assertWktParseError("expected left parenthesis after POLYGON", "POLYGON []");
        assertWktParseError("missing opening parenthesis", "POLYGON(3 3, 4 4, 5 5, 3 3)");
        assertWktParseError("missing longitude", "POLYGON ((80 80, 60, 70 70, 90 90))");
        assertWktParseError("missing comma", "POLYGON ((80 80 60 60, 70 70, 90 90))");
        assertWktParseError("premature end of input", "POLYGON ((80 80, 60 60, 70 70,");
        assertWktParseError("missing closing parenthesis", "POLYGON ((80 80, 60 60, 70 70, (30 15, 15 30, 15 45)))");
        assertWktParseError("unrecognized token", "POLYGON ((80 80, 60 60, 70 70, 80 80)z)");
        assertWktParseError("unrecognized input after WKT", "POLYGON ((80 80, 60 60, 70 70, 90 90))blahblah");
    }
}