package dto;

import static org.junit.Assert.assertEquals;

import java.util.Collections;
import java.util.List;

import sql.DatenquellenFactory;

public class hmr_Diagnosegruppe_daoTest {

    public static void main(String[] args) {
      List<Hmr_diagnosegruppe> alle = new hmr_Diagnosegruppe_dao(new DatenquellenFactory("123456789")).all();
      assertEquals(Collections.emptyList(), alle);
    }

}
