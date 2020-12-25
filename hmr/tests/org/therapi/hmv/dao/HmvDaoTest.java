package org.therapi.hmv.dao;

import static org.junit.Assert.*;

import org.junit.Test;

import core.Disziplin;
import core.Patient;
import hmv.Context;
import hmv.Hmv;
import hmv.User;
import mandant.IK;
import mandant.Mandant;

public class HmvDaoTest {

	@Test
	public void test() {
		Context context = new Context(new Mandant("123456789", "testmandant"), new User("bob"), new Patient());
		Hmv hmv = new Hmv(context);
		assertTrue(hmv.isNew());
		assertTrue(new HmvDao(new IK("123456789")).save(hmv));
		assertFalse(hmv.isNew());
	}

}
