package hmv;

import static org.junit.Assert.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.junit.Test;
import org.therapi.hmv.dao.DiagnosegruppeDao;
import org.therapi.hmv.entities.Diagnosegruppe;

import core.Disziplin;
import mandant.IK;
import sql.DatenquellenFactory;

public class Hmv13Test {

	@Test
	public void groupdiagnosegruppenbydiszi() {
		
		List<Diagnosegruppe> alle = new DiagnosegruppeDao(new IK("123456789")).all();
		
	Map<Disziplin, List<Diagnosegruppe>> diszi_diagnose = alle.stream().collect(Collectors.groupingBy(Diagnosegruppe::getDiszi));
	}

}
