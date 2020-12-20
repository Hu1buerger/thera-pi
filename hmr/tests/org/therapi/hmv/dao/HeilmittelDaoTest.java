package org.therapi.hmv.dao;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Test;

import hmv.Heilmittel;
import mandant.IK;

public class HeilmittelDaoTest {

	@Test
	public void test() {
		
		List<Heilmittel> hm = new HeilmittelDao(new IK("123456789")).all();
		assertEquals(963 ,hm.size());
	}

}
