package de.lynda.subtitle.match.utils;

import junit.framework.TestCase;

public class CheckSameNameTest extends TestCase {

	public void testIsEaual() throws Exception {
		String name = "Keynote '09 Essential Training";
		String match = "keynote 09";
		boolean eaual = CheckSameName.isMatch(name, match);
		assertEquals(eaual, true);
	}
}